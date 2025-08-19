package com.llmgateway.service;

import com.llmgateway.model.ApiKey;
import com.llmgateway.model.ConversationHistory;
import com.llmgateway.model.dto.ChatRequest;
import com.llmgateway.repository.ConversationHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationHistoryRepository repository;

    public String createConversation() {
        return UUID.randomUUID().toString();
    }

    @Transactional
    public void saveMessage(String conversationId, ApiKey apiKey, String role, String content,
                           String provider, String model, int tokenCount) {
        ConversationHistory history = ConversationHistory.builder()
                .conversationId(conversationId)
                .apiKey(apiKey)
                .role(role)
                .content(content)
                .provider(provider)
                .model(model)
                .tokenCount(tokenCount)
                .build();

        repository.save(history);
    }

    public List<ConversationHistory> getHistory(String conversationId) {
        return repository.findByConversationIdOrderByTimestampAsc(conversationId);
    }

    public List<ConversationHistory> getRecentHistory(String conversationId, int limit) {
        return repository.findRecentByConversationId(conversationId, limit);
    }

    public List<ChatRequest.Message> toMessages(List<ConversationHistory> history) {
        return history.stream()
                .map(h -> new ChatRequest.Message(h.getRole(), h.getContent()))
                .toList();
    }

    @Transactional
    public void deleteConversation(String conversationId) {
        repository.deleteByConversationId(conversationId);
    }

    public List<String> getConversationIds(Long apiKeyId) {
        return repository.findConversationIdsByApiKeyId(apiKeyId);
    }
}
