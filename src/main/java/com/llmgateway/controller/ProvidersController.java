package com.llmgateway.controller;

import com.llmgateway.model.dto.ProviderStatus;
import com.llmgateway.service.ProviderHealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/providers")
@RequiredArgsConstructor
public class ProvidersController {

    private final ProviderHealthService healthService;

    @GetMapping
    public ResponseEntity<List<ProviderStatus>> listProviders() {
        return ResponseEntity.ok(healthService.getAllStatus());
    }

    @GetMapping("/{provider}")
    public ResponseEntity<ProviderStatus> getProvider(@PathVariable String provider) {
        return ResponseEntity.ok(healthService.getStatus(provider));
    }
}
