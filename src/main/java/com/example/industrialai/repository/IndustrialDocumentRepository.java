package com.example.industrialai.repository;

import com.example.industrialai.entity.IndustrialDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface IndustrialDocumentRepository extends MongoRepository<IndustrialDocument, String> {
    // 根据设备类型查询对应的文档，SpringDataMongoDB会自动实现这个方法
    List<IndustrialDocument> findByDeviceTypeOrderByUploadTimeDesc(String deviceType);
}