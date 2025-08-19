package com.llmgateway.controller;

import com.llmgateway.model.ApiKey;
import com.llmgateway.service.UsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usage")
@RequiredArgsConstructor
public class UsageController {

    private final UsageService usageService;

    @GetMapping
    public ResponseEntity<UsageService.UsageSummary> getUsage() {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getDetails() instanceof ApiKey apiKey) {
            return ResponseEntity.ok(usageService.getUsageSummary(apiKey.getId()));
        }

        return ResponseEntity.badRequest().build();
    }
}
