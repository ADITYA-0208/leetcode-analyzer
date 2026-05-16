package com.aditya.leetcode_analyzer.service;

import com.aditya.leetcode_analyzer.entity.ImportedList;
import com.aditya.leetcode_analyzer.entity.ImportedListProblem;
import com.aditya.leetcode_analyzer.io.ImportedListProblemStatusResponse;
import com.aditya.leetcode_analyzer.io.ImportedListProgressResponse;
import com.aditya.leetcode_analyzer.io.ImportedListResponse;
import com.aditya.leetcode_analyzer.io.ImportedListSummaryResponse;
import com.aditya.leetcode_analyzer.io.ImportedProblemResponse;
import com.aditya.leetcode_analyzer.repository.CustomListRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ProblemListService {

    private static final String PROBLEM_URL = "https://leetcode.com/problems/";
    private static final Pattern LIST_SLUG_PATTERN = Pattern.compile("problem-list/([a-zA-Z0-9_-]+)");
    private static final int PAGE_SIZE = 50;

    private final LeetcodeClient leetcodeClient;
    private final CustomListRepository customListRepository;
    private final LeetcodeService leetcodeService;

    public ProblemListService(
            LeetcodeClient leetcodeClient,
            CustomListRepository customListRepository,
            LeetcodeService leetcodeService
    ) {
        this.leetcodeClient = leetcodeClient;
        this.customListRepository = customListRepository;
        this.leetcodeService = leetcodeService;
    }

    public Mono<ImportedListResponse> importFromUrl(String urlOrSlug) {
        String slug = extractSlug(urlOrSlug);
        return Mono.zip(
                fetchAllProblems(slug),
                leetcodeClient.fetchFavoriteListDetail(slug)
        ).map(tuple -> {
            List<ImportedListProblem> problems = tuple.getT1();
            JsonNode detailNode = tuple.getT2().path("data").path("favoriteDetailV2");
            if (detailNode.isMissingNode() || detailNode.isNull()) {
                throw new ListImportException("List not found or is private. Check the URL and try a public list.");
            }
            String name = detailNode.path("name").asText(slug);
            String sourceUrl = "https://leetcode.com/problem-list/" + slug + "/";
            ImportedList list = new ImportedList(slug, name, slug, sourceUrl, problems);
            customListRepository.save(list);
            return toResponse(list);
        });
    }

    public List<ImportedListSummaryResponse> listImported() {
        return customListRepository.findAll().stream()
                .map(list -> new ImportedListSummaryResponse(
                        list.id(),
                        list.name(),
                        list.slug(),
                        list.sourceUrl(),
                        list.problems().size()
                ))
                .toList();
    }

    public ImportedListResponse getById(String id) {
        ImportedList list = customListRepository.findById(id)
                .orElseThrow(() -> new ListNotFoundException("Imported list not found: " + id));
        return toResponse(list);
    }

    public boolean delete(String id) {
        if (!customListRepository.deleteById(id)) {
            throw new ListNotFoundException("Imported list not found: " + id);
        }
        return true;
    }

    public Mono<ImportedListProgressResponse> getProgress(String listId, String username) {
        ImportedList list = customListRepository.findById(listId)
                .orElseThrow(() -> new ListNotFoundException("Imported list not found: " + listId));

        return leetcodeService.getSolvedSlugs(username)
                .map(solvedSlugs -> buildProgress(list, solvedSlugs));
    }

    public ImportedList requireList(String id) {
        return customListRepository.findById(id)
                .orElseThrow(() -> new ListNotFoundException("Imported list not found: " + id));
    }

    private Mono<List<ImportedListProblem>> fetchAllProblems(String slug) {
        return fetchPage(slug, 0, new ArrayList<>());
    }

    private Mono<List<ImportedListProblem>> fetchPage(String slug, int skip, List<ImportedListProblem> accumulated) {
        return leetcodeClient.fetchFavoriteQuestionList(slug, skip, PAGE_SIZE)
                .flatMap(node -> {
                    JsonNode listNode = node.path("data").path("favoriteQuestionList");
                    if (listNode.isMissingNode() || listNode.isNull()) {
                        JsonNode errors = node.path("errors");
                        if (errors.isArray() && !errors.isEmpty()) {
                            return Mono.error(new ListImportException(
                                    errors.get(0).path("message").asText("Failed to fetch list")));
                        }
                        return Mono.error(new ListImportException("List not found or is private."));
                    }

                    for (JsonNode question : listNode.path("questions")) {
                        String titleSlug = question.path("titleSlug").asText();
                        if (titleSlug.isBlank()) {
                            continue;
                        }
                        accumulated.add(new ImportedListProblem(
                                question.path("title").asText(),
                                titleSlug,
                                normalizeDifficulty(question.path("difficulty").asText())
                        ));
                    }

                    boolean hasMore = listNode.path("hasMore").asBoolean(false);
                    if (hasMore) {
                        return fetchPage(slug, skip + PAGE_SIZE, accumulated);
                    }

                    if (accumulated.isEmpty()) {
                        return Mono.error(new ListImportException("This list has no problems."));
                    }

                    return Mono.just(List.copyOf(accumulated));
                });
    }

    private ImportedListProgressResponse buildProgress(ImportedList list, Set<String> solvedSlugs) {
        List<ImportedListProblemStatusResponse> problems = list.problems().stream()
                .map(problem -> new ImportedListProblemStatusResponse(
                        problem.title(),
                        problem.titleSlug(),
                        problem.difficulty(),
                        solvedSlugs.contains(problem.titleSlug()),
                        PROBLEM_URL + problem.titleSlug() + "/"
                ))
                .toList();

        int solvedCount = (int) problems.stream().filter(ImportedListProblemStatusResponse::solved).count();
        int total = problems.size();
        double percent = total == 0 ? 0 : (solvedCount * 100.0) / total;

        return new ImportedListProgressResponse(
                list.id(),
                list.name(),
                total,
                solvedCount,
                Math.round(percent * 10.0) / 10.0,
                problems
        );
    }

    private ImportedListResponse toResponse(ImportedList list) {
        List<ImportedProblemResponse> problems = list.problems().stream()
                .map(problem -> new ImportedProblemResponse(
                        problem.title(),
                        problem.titleSlug(),
                        problem.difficulty(),
                        PROBLEM_URL + problem.titleSlug() + "/"
                ))
                .toList();

        return new ImportedListResponse(
                list.id(),
                list.name(),
                list.slug(),
                list.sourceUrl(),
                problems.size(),
                problems
        );
    }

    private String extractSlug(String urlOrSlug) {
        String trimmed = urlOrSlug.trim();
        if (trimmed.isEmpty()) {
            throw new ListImportException("Please provide a LeetCode list URL or slug.");
        }

        if (!trimmed.contains("/") && trimmed.matches("[a-zA-Z0-9_-]+")) {
            return trimmed;
        }

        Matcher matcher = LIST_SLUG_PATTERN.matcher(trimmed);
        if (matcher.find()) {
            return matcher.group(1);
        }

        throw new ListImportException(
                "Invalid URL. Use a link like https://leetcode.com/problem-list/your-list-id/");
    }

    private String normalizeDifficulty(String raw) {
        if (raw == null || raw.isBlank()) {
            return "Medium";
        }
        return switch (raw.toUpperCase()) {
            case "EASY" -> "Easy";
            case "MEDIUM" -> "Medium";
            case "HARD" -> "Hard";
            default -> raw.substring(0, 1).toUpperCase() + raw.substring(1).toLowerCase();
        };
    }

    public static class ListImportException extends RuntimeException {
        public ListImportException(String message) {
            super(message);
        }
    }

    public static class ListNotFoundException extends RuntimeException {
        public ListNotFoundException(String message) {
            super(message);
        }
    }
}
