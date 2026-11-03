package com.example.industrialai.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

// 这个注解表示这个类对应MongoDB中的industrial_documents集合
@Document(collection = "industrial_documents")
@Data // Lombok注解，自动生成getter、setter、toString等方法
@NoArgsConstructor
@AllArgsConstructor
public class IndustrialDocument {
    // 主键ID
    @Id
    private String id;
    // 文档名称，比如"风机运维手册.pdf"
    private String fileName;
    // 文档的文本内容，解析之后的纯文本
    private String content;
    // 文件类型
    private String fileType;
    // 设备类型，比如风机、水泵、电机，用来区分不同设备的文档
    private String deviceType;
    // 上传时间
    private LocalDateTime uploadTime;
}