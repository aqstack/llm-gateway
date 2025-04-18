package com.llmgateway.controller;

import com.llmgateway.model.dto.ApiKeyRequest;
import com.llmgateway.model.dto.ApiKeyResponse;
import com.llmgateway.repository.ApiKeyRepository;
import com.llmgateway.service.ApiKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;
    private final ApiKeyRepository apiKeyRepository;

    @PostMapping
    public ResponseEntity<ApiKeyResponse> createKey(@Valid @RequestBody ApiKeyRequest request) {
        var result = apiKeyService.createApiKey(
            request.name(),
            request.owner(),
            request.requestsPerMinute() != null ? request.requestsPerMinute() : 60
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiKeyResponse.fromWithRawKey(result.apiKey(), result.rawKey()));
    }

    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> listKeys(@RequestParam(required = false) String owner) {
        var keys = owner != null
            ? apiKeyRepository.findByOwner(owner)
            : apiKeyRepository.findAll();

        return ResponseEntity.ok(
            keys.stream()
                .map(ApiKeyResponse::from)
                .toList()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateKey(@PathVariable Long id) {
        apiKeyService.deactivateKey(id);
        return ResponseEntity.noContent().build();
    }
}
