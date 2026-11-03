package com.example.industrialai.repository;

import com.example.industrialai.entity.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    // 根据会话ID查询所有的对话历史，按时间排序
    List<ChatMessage> findBySessionIdOrderByCreateTimeAsc(String sessionId);
    List<ChatMessage> findTop20BySessionIdOrderByCreateTimeDesc(String sessionId);
}