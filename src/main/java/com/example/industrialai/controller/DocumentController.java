package com.example.industrialai.controller;

import com.example.industrialai.entity.IndustrialDocument;
import com.example.industrialai.service.DocumentService;
import com.example.industrialai.service.RagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/document")
@RequiredArgsConstructor
@CrossOrigin
public class DocumentController {

    private final DocumentService documentService;

    // 注入RagService
    private final RagService ragService;

    /**
     * 上传工业文档接口
     */
    @PostMapping("/upload")
    public IndustrialDocument upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("deviceType") String deviceType
    ) throws IOException {
        IndustrialDocument document = documentService.uploadDocument(file, deviceType);
        // 上传文档后，只把这一份增量加进索引（原来调 initKnowledgeBase 会全量重建）
        ragService.updateKnowledgeBase(document);
        return document;
    }

    /**
     * 获取所有工业文档
     */
    @GetMapping("/all")
    public List<IndustrialDocument> getAllDocuments() {
        return documentService.getAllDocuments();
    }

    /**
     * 根据设备类型获取文档
     */
    @GetMapping("/device/{deviceType}")
    public List<IndustrialDocument> getByDeviceType(@PathVariable String deviceType) {
        return documentService.getDocumentsByDeviceType(deviceType);
    }

    /**
     * 删除文档
     */
    @GetMapping("/delete")
    public void deleteDocument(String id) {
        documentService.deleteDocument(id);
    }

    /**
     * 删除文档
     */
    @DeleteMapping("/documents/{id}")
    public void deleteDocument1(@PathVariable String id) {
        documentService.deleteDocument(id);
    }
}