package com.llmgateway.service;

import com.llmgateway.model.ApiKey;
import com.llmgateway.repository.ApiKeyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiKeyServiceTest {

    @Mock
    private ApiKeyRepository apiKeyRepository;

    private ApiKeyService apiKeyService;

    @BeforeEach
    void setUp() {
        apiKeyService = new ApiKeyService(apiKeyRepository);
    }

    @Test
    void createApiKey_shouldGenerateUniqueKey() {
        when(apiKeyRepository.save(any(ApiKey.class))).thenAnswer(invocation -> {
            ApiKey key = invocation.getArgument(0);
            key.setId(1L);
            return key;
        });

        var result = apiKeyService.createApiKey("test-key", "owner@test.com", 60);

        assertThat(result.rawKey()).startsWith("llmgw_");
        assertThat(result.apiKey().getName()).isEqualTo("test-key");
        assertThat(result.apiKey().getOwner()).isEqualTo("owner@test.com");
    }

    @Test
    void validateKey_shouldReturnEmptyForInactiveKey() {
        ApiKey inactiveKey = ApiKey.builder()
                .id(1L)
                .active(false)
                .build();

        when(apiKeyRepository.findByKeyHash(any())).thenReturn(Optional.of(inactiveKey));

        var result = apiKeyService.validateKey("some-key");

        assertThat(result).isEmpty();
    }
}
