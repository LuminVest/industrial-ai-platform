package com.example.industrialai.service;

import com.example.industrialai.entity.ChatMessage;
import com.example.industrialai.entity.IndustrialDocument;
import com.example.industrialai.repository.ChatMessageRepository;
import com.example.industrialai.repository.IndustrialDocumentRepository;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final QwenChatModel qwenChatModel;
    private final ChatMessageRepository chatMessageRepository;
    private final IndustrialDocumentRepository documentRepository;
    private final RagService ragService;
    /**
     * 简单的一问一答对话
     */
    public String chat(String userMessage) {
        // 调用大模型对象对话方法将问题传入 给出回答
        return qwenChatModel.chat(userMessage);
    }
    public String chat(String sessionId, String userMessage) {
        // 调用service方法查询最近20条的历史记录
        List<ChatMessage> history = chatMessageRepository.findTop20BySessionIdOrderByCreateTimeDesc(sessionId);
        //拼接会话记忆的内容
        StringBuilder prompt=new StringBuilder("之前的对话:\n");
        for (ChatMessage chatMessage : history) {
            prompt.append(chatMessage.getRole()+":"+chatMessage.getContent()+"\n");
        }
        //继续拼接本次提问问题
        prompt.append("本次的问题是:").append(userMessage).append("\n");
        // 创建用户信息对象
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setRole("user");
        userMsg.setContent(userMessage);
        userMsg.setCreateTime(LocalDateTime.now());
        // 将用户信息对象存入MongoDB
        chatMessageRepository.save(userMsg);
        // 调用ai大模型对话函数 提交问题返回结果
        String aiReply = qwenChatModel.chat(prompt.toString());
        // 创建ai结果信息对象
        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setSessionId(sessionId);
        aiMsg.setRole("ai");
        aiMsg.setContent(aiReply);
        aiMsg.setCreateTime(LocalDateTime.now());
        // 将ai结果信息对象存入MongoDB
        chatMessageRepository.save(aiMsg);
        return aiReply;
    }

    public String ragChat1(String sessionId,String deviceType ,String userMessage) {
        //调用documentRepository获取指定类型的文档信息
        List<IndustrialDocument> documentList = documentRepository.findByDeviceTypeOrderByUploadTimeDesc(deviceType);

        String defaultprompt= """
                你是一个专业的运维人员,请基于参考文档内容回答的问题,不要生成文档中没有的内容,如果涉及到文档中没有的内容,
                请以"没有在参考文档中找到,请更新文档或联系相关管理人员!"回复
                """;
        StringBuilder prompt=new StringBuilder(defaultprompt);
        prompt.append("参考文档:\n");
        for (IndustrialDocument industrialDocument : documentList) {
            prompt.append(industrialDocument.getContent()+"\n");

        }
        // 调用service方法查询最近20条的历史记录
        List<ChatMessage> history = chatMessageRepository.findTop20BySessionIdOrderByCreateTimeDesc(sessionId);
        //拼接会话记忆的内容
        prompt.append("之前的对话:\n");
        for (ChatMessage chatMessage : history) {
            prompt.append(chatMessage.getRole()+":"+chatMessage.getContent()+"\n");
        }
        //继续拼接本次提问问题
        prompt.append("本次的问题是:").append(userMessage).append("\n");
        // 创建用户信息对象
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setRole("user");
        userMsg.setContent(userMessage);
        userMsg.setCreateTime(LocalDateTime.now());
        // 将用户信息对象存入MongoDB
        chatMessageRepository.save(userMsg);
        // 调用ai大模型对话函数 提交问题返回结果
        String aiReply = qwenChatModel.chat(prompt.toString());
        // 创建ai结果信息对象
        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setSessionId(sessionId);
        aiMsg.setRole("ai");
        aiMsg.setContent(aiReply);
        aiMsg.setCreateTime(LocalDateTime.now());
        // 将ai结果信息对象存入MongoDB
        chatMessageRepository.save(aiMsg);
        return aiReply;
    }




    // 新增RAG运维问答【修复多轮记忆版】
    public String ragChat(String sessionId, String userMessage, String deviceType) {
        // 1. 读取当前会话最近20条历史对话
        List<ChatMessage> historyList = chatMessageRepository.findTop20BySessionIdOrderByCreateTimeDesc(sessionId);
        // 反转，按时间正序
        StringBuilder historySb = new StringBuilder();
        if (!historyList.isEmpty()) {
            historySb.append("=== 本次对话历史记录 ===").append("\n");
            // 倒序取出来是最新在前，循环反转顺序
            for (int i = historyList.size() - 1; i >= 0; i--) {
                ChatMessage msg = historyList.get(i);
                if ("user".equals(msg.getRole())) {
                    historySb.append("运维人员：").append(msg.getContent()).append("\n");
                } else {
                    historySb.append("运维AI：").append(msg.getContent()).append("\n");
                }
            }
            historySb.append("\n");
        }

        // 2. RAG检索相关的私有设备文档
        String context = ragService.retrieveRelevantContent(userMessage);

        // 3. 完整Prompt：角色+历史对话+私有知识库+当前新问题
        String promptTemplate = """
        你是资深工业运维专家，拥有完整对话记忆，可以记住用户上一轮提问、支持连续追问。
        回答严格仅参考提供的工业设备文档，禁止编造不存在的设备参数/故障方案。
        文档无相关内容统一回复："抱歉，这个问题我暂时无法回答，请联系技术支持。"

        %s
        当前设备类型：{{deviceType}}
        私有运维参考文档：
        {{context}}

        用户最新提问：{{question}}
        """;
        String prompt = String.format(promptTemplate, historySb.toString())
                .replace("{{context}}", context)
                .replace("{{question}}", userMessage)
                .replace("{{deviceType}}", deviceType);

        // 4. 保存用户提问记录
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setRole("user");
        userMsg.setContent(userMessage);
        userMsg.setCreateTime(LocalDateTime.now());
        chatMessageRepository.save(userMsg);

        // 5. 调用大模型
        String aiReply = qwenChatModel.chat(prompt);

        // 6. 保存AI回复
        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setSessionId(sessionId);
        aiMsg.setRole("ai");
        aiMsg.setContent(aiReply);
        aiMsg.setCreateTime(LocalDateTime.now());
        chatMessageRepository.save(aiMsg);

        return aiReply;
    }

    private final  QwenChatModel qwenWebChatModel;

    public String web(String userMessage) {
        // 调用大模型对象对话方法将问题传入 给出回答
        return qwenWebChatModel.chat(userMessage);
    }
    /**
     * 获取某个会话的对话历史
     */
    public List<ChatMessage> getChatHistory(String sessionId) {
        return chatMessageRepository.findBySessionIdOrderByCreateTimeAsc(sessionId);
    }
}