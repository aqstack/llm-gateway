package com.llmgateway.service;

import com.llmgateway.model.PromptTemplate;
import com.llmgateway.repository.PromptTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PromptTemplateService {

    private final PromptTemplateRepository repository;

    public PromptTemplate create(String name, String template, String description, String owner) {
        if (repository.existsByName(name)) {
            throw new IllegalArgumentException("Template with name already exists: " + name);
        }

        PromptTemplate promptTemplate = PromptTemplate.builder()
                .name(name)
                .template(template)
                .description(description)
                .owner(owner)
                .build();

        return repository.save(promptTemplate);
    }

    public Optional<PromptTemplate> findByName(String name) {
        return repository.findByName(name);
    }

    public List<PromptTemplate> findByOwner(String owner) {
        return repository.findByOwner(owner);
    }

    public String render(String name, Map<String, String> variables) {
        PromptTemplate template = repository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + name));

        return template.render(variables);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
