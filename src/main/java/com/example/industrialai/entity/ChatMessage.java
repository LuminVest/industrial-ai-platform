package com.example.industrialai.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "chat_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    @Id
    private String id;
    // 会话ID，用来区分不同的运维人员对话
    private String sessionId;
    // 消息角色：user是运维人员的消息，ai是大模型的回复
    private String role;
    // 消息内容
    private String content;
    // 发送时间
    private LocalDateTime createTime;
}