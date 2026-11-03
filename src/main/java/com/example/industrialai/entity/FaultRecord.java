package com.example.industrialai.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 故障记录实体类
 */
@Document(collection = "fault_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FaultRecord {
    @Id
    private String id;
    // 设备ID
    private String deviceId;
    // 设备类型
    private String deviceType;
    // 故障现象
    private String faultPhenomenon;
    // 诊断结果
    private String diagnoseResult;
    // 创建时间
    private LocalDateTime createTime;
}