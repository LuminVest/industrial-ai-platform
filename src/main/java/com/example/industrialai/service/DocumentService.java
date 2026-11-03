package com.example.industrialai.service;

import com.example.industrialai.entity.IndustrialDocument;
import com.example.industrialai.repository.IndustrialDocumentRepository;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor // Lombok注解，自动给final的字段注入依赖
public class DocumentService {
    private final IndustrialDocumentRepository documentRepository;
    private final DocumentParser documentParser = new ApacheTikaDocumentParser();

    /**
     * 上传工业文档
     */
    public IndustrialDocument uploadDocument(MultipartFile file, String deviceType) throws IOException {
        Document document = documentParser.parse(file.getInputStream());
        String content = document.text();
        IndustrialDocument doc = new IndustrialDocument();
        doc.setFileName(file.getOriginalFilename());
        doc.setFileType(file.getContentType());
        doc.setContent(content);
        doc.setDeviceType(deviceType);
        doc.setUploadTime(LocalDateTime.now());
        return documentRepository.save(doc);
    }

    /**
     * 获取所有的工业文档
     */
    public List<IndustrialDocument> getAllDocuments() {
        return documentRepository.findAll();
    }

    /**
     * 根据设备类型获取文档
     */
    public List<IndustrialDocument> getDocumentsByDeviceType(String deviceType) {
        return documentRepository.findByDeviceTypeOrderByUploadTimeDesc(deviceType);
    }

    /**
     * 根据ID获取文档
     */
    public IndustrialDocument getDocumentById(String id) {
        return documentRepository.findById(id).orElse(null);
    }
    /**
     * 删除文档
     */
    public void deleteDocument(String id) {
        documentRepository.deleteById(id);
    }
}