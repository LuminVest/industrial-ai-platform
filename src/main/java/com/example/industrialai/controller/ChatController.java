package com.example.industrialai.controller;

import com.example.industrialai.entity.ChatMessage;
import com.example.industrialai.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 对话接口
     * 参数：userMessage 用户的消息
     */
    @PostMapping("/t")
    public String chat(String userMessage) {
        return chatService.chat(userMessage);
    }
    @PostMapping
    public String chat(String sessionId,String userMessage) {
        return chatService.chat(sessionId,userMessage);
    }
//    @PostMapping("/rag")
//    public String ragChat(String sessionId,String deviceType,String userMessage) {
//        return chatService.ragChat(sessionId,userMessage,deviceType);
//    }

    /**
     * 获取对话历史
     */
    @GetMapping("/history/{sessionId}")
    public List<ChatMessage> getHistory(@PathVariable String sessionId) {
        return chatService.getChatHistory(sessionId);
    }

    /**
     * RAG智能运维问答接口
     */
    /**
     * RAG智能运维问答接口
     */
    @PostMapping("/rag")
    public String ragChat(@RequestBody Map<String, String> params) {
        String sessionId = params.get("sessionId");
        String userMessage = params.get("userMessage");
        String deviceType = params.get("deviceType");
        return chatService.ragChat(sessionId, userMessage, deviceType);
    }
    @PostMapping("/web")
    public String web(String userMessage){
        return chatService.web(userMessage);
    }
}