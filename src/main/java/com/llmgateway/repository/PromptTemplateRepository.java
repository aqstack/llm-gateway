package com.llmgateway.repository;

import com.llmgateway.model.PromptTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, Long> {

    Optional<PromptTemplate> findByName(String name);

    List<PromptTemplate> findByOwner(String owner);

    boolean existsByName(String name);
}
