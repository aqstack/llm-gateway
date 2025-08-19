package com.llmgateway.repository;

import com.llmgateway.model.ConversationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationHistoryRepository extends JpaRepository<ConversationHistory, Long> {

    List<ConversationHistory> findByConversationIdOrderByTimestampAsc(String conversationId);

    List<ConversationHistory> findByApiKeyIdOrderByTimestampDesc(Long apiKeyId);

    @Query("SELECT ch FROM ConversationHistory ch WHERE ch.conversationId = :conversationId ORDER BY ch.timestamp DESC LIMIT :limit")
    List<ConversationHistory> findRecentByConversationId(
            @Param("conversationId") String conversationId,
            @Param("limit") int limit);

    void deleteByConversationId(String conversationId);

    @Query("SELECT DISTINCT ch.conversationId FROM ConversationHistory ch WHERE ch.apiKey.id = :apiKeyId")
    List<String> findConversationIdsByApiKeyId(@Param("apiKeyId") Long apiKeyId);
}
