package com.llmgateway.controller;

import com.llmgateway.model.PromptTemplate;
import com.llmgateway.service.PromptTemplateService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
public class PromptTemplateController {

    private final PromptTemplateService templateService;

    public record CreateTemplateRequest(
            @NotBlank String name,
            @NotBlank String template,
            String description,
            @NotBlank String owner
    ) {}

    public record RenderRequest(
            Map<String, String> variables
    ) {}

    @PostMapping
    public ResponseEntity<PromptTemplate> create(@Valid @RequestBody CreateTemplateRequest request) {
        PromptTemplate template = templateService.create(
                request.name(),
                request.template(),
                request.description(),
                request.owner()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(template);
    }

    @GetMapping("/{name}")
    public ResponseEntity<PromptTemplate> getByName(@PathVariable String name) {
        return templateService.findByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<PromptTemplate>> listByOwner(@RequestParam String owner) {
        return ResponseEntity.ok(templateService.findByOwner(owner));
    }

    @PostMapping("/{name}/render")
    public ResponseEntity<Map<String, String>> render(
            @PathVariable String name,
            @RequestBody RenderRequest request) {
        String rendered = templateService.render(name, request.variables());
        return ResponseEntity.ok(Map.of("rendered", rendered));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        templateService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
