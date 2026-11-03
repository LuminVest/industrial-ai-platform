package com.example.industrialai.repository;

import com.example.industrialai.entity.FaultRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FaultRecordRepository extends MongoRepository<FaultRecord, String> {
    List<FaultRecord> findByDeviceIdOrderByCreateTimeDesc(String deviceId);
}