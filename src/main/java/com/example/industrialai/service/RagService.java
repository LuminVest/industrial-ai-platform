package com.example.industrialai.service;

import com.example.industrialai.entity.IndustrialDocument;
import com.example.industrialai.repository.IndustrialDocumentRepository;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RagService {
    private final IndustrialDocumentRepository documentRepository;
    // 词嵌入模型
    private final EmbeddingModel embeddingModel;
    // 词嵌入存储器
    private InMemoryEmbeddingStore<TextSegment> embeddingStore;
    // 内容检索器
    private EmbeddingStoreContentRetriever retriever;
    // 文档提取器
    private  EmbeddingStoreIngestor ingestor;
    /**
     * 项目启动的时候，自动加载所有的工业文档，构建知识库
     */
    @PostConstruct // 对象创建完成后，自动执行这个方法
    public void initKnowledgeBase() {
        // 1. 初始化内存的嵌入存储
        embeddingStore = new InMemoryEmbeddingStore<>();
        // 2. 文档分块器：把长文档切成小块，每块500个token，重叠50个token，避免切断语义
        var spliter = DocumentSplitters.recursive(500, 50);
        // 3. 摄取器：把文档分快、转成向量、存入到嵌入存储里
        ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(spliter)
                .embeddingModel(embeddingModel) // 嵌入模型
                .embeddingStore(embeddingStore) // 嵌入存储
                .build();

        // 4. 加载所有的工业文档
        List<IndustrialDocument> documents =documentRepository.findAll();
        for (IndustrialDocument doc : documents) {
            // 把我们的工业文档转成LangChain4j的Document
            Document document = Document.from(doc.getContent());
            // 摄取文档
            ingestor.ingest(document);
        }

        // 5. 创建内容检索器，最多返回5个最相关的文档片段
        retriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore) // 嵌入存储
                .embeddingModel(embeddingModel) // 嵌入模型
                .maxResults(5) // 最多返回5个最相关的文档片段
                .minScore(0.3) // 相似度最低0.3，低于这个的就不要了
                .build();

    }
    /**
     * 检索相关的文档片段
     */
    public String retrieveRelevantContent(String query) {
        // 如果提问信息为null或空的内容 直接返回空字符串 代表有没检索到文档对应的片段
        if (query == null || query.isEmpty()) {
            return "";
        }
        // 用检索器检索相关的片段
        // Query.from(query)基于提问信息创建查询对象 用于检索器进行文档片段检索
        List<Content> segments = retriever.retrieve(Query.from(query));
        // 如果简述结果为空列表 直接返回空字符串 代表有没检索到文档对应的片段
        if (segments.isEmpty()) {
            return "";
        }
        // 把这些片段拼起来，作为上下文
        StringBuilder context = new StringBuilder();
        for (Content segment : segments){
            context.append(segment.textSegment().text()).append("\n\n");
        }
        return context.toString();
    }

    /**
     * 当上传新的文档的时候，更新知识库
     */
    public void updateKnowledgeBase(IndustrialDocument newDocument) {
        // 重新初始化知识库，简单粗暴，适合新手，生产环境可以增量更新
        //        initKnowledgeBase();

        //使用文档提取器 将新上传的文档进行分块存储
        Document document = Document.from(newDocument.getContent());
        // 摄取文档
        ingestor.ingest(document);
    }
}