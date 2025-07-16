package com.llmgateway.config;

import com.llmgateway.model.ApiKey;
import com.llmgateway.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getDetails() instanceof ApiKey apiKey) {
            if (!rateLimitService.isAllowed(apiKey)) {
                var info = rateLimitService.getRateLimitInfo(apiKey);
                response.setHeader("X-RateLimit-Limit", String.valueOf(info.limit()));
                response.setHeader("X-RateLimit-Remaining", "0");
                response.setHeader("X-RateLimit-Reset", String.valueOf(info.resetInSeconds()));
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Rate limit exceeded\",\"retry_after\":" + info.resetInSeconds() + "}");
                return false;
            }

            var info = rateLimitService.getRateLimitInfo(apiKey);
            response.setHeader("X-RateLimit-Limit", String.valueOf(info.limit()));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(info.remaining()));
            response.setHeader("X-RateLimit-Reset", String.valueOf(info.resetInSeconds()));
        }

        return true;
    }
}
