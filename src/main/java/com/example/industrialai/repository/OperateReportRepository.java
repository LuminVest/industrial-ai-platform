package com.example.industrialai.repository;

import com.example.industrialai.entity.OperateReport;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OperateReportRepository extends MongoRepository<OperateReport, String> {
    // 根据设备ID查询所有的报告
    List<OperateReport> findByDeviceIdOrderByCreateTimeDesc(String deviceId);
}