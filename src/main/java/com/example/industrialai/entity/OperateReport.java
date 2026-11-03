package com.example.industrialai.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 运维报告实体类
 */
@Document(collection = "operate_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperateReport {
    @Id
    private String id;
    // 对应的设备ID
    private String deviceId;
    // 设备类型
    private String deviceType;
    // 报告类型：运维报告、故障报告
    private String reportType;
    // 报告内容
    private String content;
    // 创建时间
    private LocalDateTime createTime;
}