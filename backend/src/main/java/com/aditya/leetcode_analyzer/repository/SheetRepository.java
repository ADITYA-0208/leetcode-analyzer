package com.aditya.leetcode_analyzer.repository;

import com.aditya.leetcode_analyzer.entity.LearningSheet;
import com.aditya.leetcode_analyzer.entity.SheetProblem;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class SheetRepository {

    private final Map<String, LearningSheet> sheetsById;

    public SheetRepository(ObjectMapper objectMapper) throws IOException {
        this.sheetsById = loadSheets(objectMapper);
    }

    public List<LearningSheet> findAll() {
        return List.copyOf(sheetsById.values());
    }

    public Optional<LearningSheet> findById(String id) {
        return Optional.ofNullable(sheetsById.get(id));
    }

    private Map<String, LearningSheet> loadSheets(ObjectMapper objectMapper) throws IOException {
        JsonNode root = objectMapper.readTree(new ClassPathResource("data/sheets.json").getInputStream());
        Map<String, LearningSheet> loaded = new LinkedHashMap<>();

        for (JsonNode sheetNode : root) {
            String id = sheetNode.get("id").asText();
            String name = sheetNode.get("name").asText();
            String description = sheetNode.path("description").asText("");
            List<SheetProblem> problems = new ArrayList<>();

            for (JsonNode problemNode : sheetNode.get("problems")) {
                problems.add(new SheetProblem(
                        problemNode.get("title").asText(),
                        problemNode.get("titleSlug").asText(),
                        problemNode.get("difficulty").asText(),
                        problemNode.path("topic").asText("General"),
                        problemNode.path("order").asInt(problems.size() + 1)
                ));
            }

            loaded.put(id, new LearningSheet(id, name, description, List.copyOf(problems)));
        }

        return Collections.unmodifiableMap(loaded);
    }
}
