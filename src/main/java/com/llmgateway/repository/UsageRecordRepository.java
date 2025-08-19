package com.llmgateway.repository;

import com.llmgateway.model.UsageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Repository
public interface UsageRecordRepository extends JpaRepository<UsageRecord, Long> {

    List<UsageRecord> findByApiKeyIdOrderByTimestampDesc(Long apiKeyId);

    List<UsageRecord> findByApiKeyIdAndTimestampBetween(Long apiKeyId, Instant start, Instant end);

    @Query("SELECT SUM(u.cost) FROM UsageRecord u WHERE u.apiKey.id = :apiKeyId")
    BigDecimal getTotalCostByApiKeyId(@Param("apiKeyId") Long apiKeyId);

    @Query("SELECT SUM(u.cost) FROM UsageRecord u WHERE u.apiKey.id = :apiKeyId AND u.timestamp >= :since")
    BigDecimal getCostSince(@Param("apiKeyId") Long apiKeyId, @Param("since") Instant since);

    @Query("SELECT SUM(u.totalTokens) FROM UsageRecord u WHERE u.apiKey.id = :apiKeyId")
    Long getTotalTokensByApiKeyId(@Param("apiKeyId") Long apiKeyId);

    @Query("SELECT u.provider, SUM(u.cost) FROM UsageRecord u WHERE u.apiKey.id = :apiKeyId GROUP BY u.provider")
    List<Object[]> getCostByProvider(@Param("apiKeyId") Long apiKeyId);
}
