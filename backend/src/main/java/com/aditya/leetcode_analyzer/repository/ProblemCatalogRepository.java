package com.aditya.leetcode_analyzer.repository;

import com.aditya.leetcode_analyzer.entity.CatalogProblem;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class ProblemCatalogRepository {

    private final List<CatalogProblem> catalog;

    public ProblemCatalogRepository(ObjectMapper objectMapper) throws IOException {
        this.catalog = loadCatalog(objectMapper);
    }

    public List<CatalogProblem> findAll() {
        return catalog;
    }

    private List<CatalogProblem> loadCatalog(ObjectMapper objectMapper) throws IOException {
        JsonNode root = objectMapper.readTree(new ClassPathResource("data/problem-catalog.json").getInputStream());
        List<CatalogProblem> loaded = new ArrayList<>();

        for (JsonNode node : root) {
            loaded.add(new CatalogProblem(
                    node.get("title").asText(),
                    node.get("titleSlug").asText(),
                    node.get("difficulty").asText(),
                    node.path("topic").asText("General")
            ));
        }

        return Collections.unmodifiableList(loaded);
    }
}
