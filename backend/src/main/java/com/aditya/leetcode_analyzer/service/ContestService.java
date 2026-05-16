package com.aditya.leetcode_analyzer.service;

import com.aditya.leetcode_analyzer.entity.CatalogProblem;
import com.aditya.leetcode_analyzer.entity.SheetProblem;
import com.aditya.leetcode_analyzer.io.ContestGenerateRequest;
import com.aditya.leetcode_analyzer.io.ContestProblemResponse;
import com.aditya.leetcode_analyzer.io.ContestResponse;
import com.aditya.leetcode_analyzer.repository.ProblemCatalogRepository;
import com.aditya.leetcode_analyzer.repository.SheetRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ContestService {

    private static final String PROBLEM_URL = "https://leetcode.com/problems/";

    private final LeetcodeService leetcodeService;
    private final ProblemCatalogRepository catalogRepository;
    private final SheetRepository sheetRepository;
    private final ProblemListService problemListService;

    public ContestService(
            LeetcodeService leetcodeService,
            ProblemCatalogRepository catalogRepository,
            SheetRepository sheetRepository,
            ProblemListService problemListService
    ) {
        this.leetcodeService = leetcodeService;
        this.catalogRepository = catalogRepository;
        this.sheetRepository = sheetRepository;
        this.problemListService = problemListService;
    }

    public Mono<ContestResponse> generateContest(ContestGenerateRequest request) {
        return resolvePool(request)
                .map(pool -> buildContest(request, pool));
    }

    private Mono<List<PoolProblem>> resolvePool(ContestGenerateRequest request) {
        return switch (request.pool()) {
            case SOLVED -> leetcodeService.getSolvedProblems(request.username())
                    .map(solved -> solved.stream()
                            .map(s -> new PoolProblem(s.title(), s.titleSlug(), resolveDifficulty(s.titleSlug())))
                            .toList());
            case CATALOG -> Mono.just(catalogRepository.findAll().stream()
                    .map(c -> new PoolProblem(c.title(), c.titleSlug(), c.difficulty()))
                    .toList());
            case SHEET -> {
                String sheetId = request.sheetId() != null ? request.sheetId() : "striver-sde";
                var sheet = sheetRepository.findById(sheetId)
                        .orElseThrow(() -> new SheetComparisonService.SheetNotFoundException("Sheet not found: " + sheetId));
                yield Mono.just(sheet.problems().stream()
                        .map(p -> new PoolProblem(p.title(), p.titleSlug(), p.difficulty()))
                        .toList());
            }
            case CUSTOM_LIST -> {
                String listId = request.listId();
                if (listId == null || listId.isBlank()) {
                    yield Mono.error(new IllegalArgumentException("listId is required for custom list contests"));
                }
                var list = problemListService.requireList(listId);
                yield Mono.just(list.problems().stream()
                        .map(p -> new PoolProblem(p.title(), p.titleSlug(), p.difficulty()))
                        .toList());
            }
        };
    }

    private ContestResponse buildContest(ContestGenerateRequest request, List<PoolProblem> pool) {
        if (pool.isEmpty()) {
            throw new IllegalArgumentException("No problems available for the selected pool");
        }

        List<PoolProblem> easy = filterByDifficulty(pool, "Easy");
        List<PoolProblem> medium = filterByDifficulty(pool, "Medium");
        List<PoolProblem> hard = filterByDifficulty(pool, "Hard");

        int requestedTotal = request.totalQuestions();
        int requestedEasy = request.easyCount();
        int requestedMedium = request.mediumCount();
        int requestedHard = request.hardCount();
        int explicitSum = requestedEasy + requestedMedium + requestedHard;

        List<PoolProblem> selected = new ArrayList<>();
        if (explicitSum > 0) {
            selected.addAll(pickRandom(easy, requestedEasy));
            selected.addAll(pickRandom(medium, requestedMedium));
            selected.addAll(pickRandom(hard, requestedHard));
        } else {
            selected.addAll(pickRandom(pool, requestedTotal));
        }

        if (selected.size() < requestedTotal) {
            List<PoolProblem> remaining = new ArrayList<>(pool);
            remaining.removeAll(selected);
            Collections.shuffle(remaining);
            for (PoolProblem problem : remaining) {
                if (selected.size() >= requestedTotal) {
                    break;
                }
                if (!selected.contains(problem)) {
                    selected.add(problem);
                }
            }
        }

        if (selected.size() > requestedTotal) {
            selected = selected.subList(0, requestedTotal);
        }

        Collections.shuffle(selected);

        List<ContestProblemResponse> problems = new ArrayList<>();
        for (int i = 0; i < selected.size(); i++) {
            PoolProblem problem = selected.get(i);
            problems.add(new ContestProblemResponse(
                    i + 1,
                    problem.title(),
                    problem.titleSlug(),
                    problem.difficulty(),
                    PROBLEM_URL + problem.titleSlug() + "/"
            ));
        }

        return new ContestResponse(
                UUID.randomUUID().toString(),
                request.durationMinutes(),
                problems.size(),
                problems
        );
    }

    private List<PoolProblem> filterByDifficulty(List<PoolProblem> pool, String difficulty) {
        return pool.stream()
                .filter(p -> p.difficulty().equalsIgnoreCase(difficulty))
                .collect(Collectors.toList());
    }

    private List<PoolProblem> pickRandom(List<PoolProblem> source, int count) {
        if (count <= 0 || source.isEmpty()) {
            return List.of();
        }
        List<PoolProblem> copy = new ArrayList<>(source);
        Collections.shuffle(copy);
        return copy.subList(0, Math.min(count, copy.size()));
    }

    private String resolveDifficulty(String titleSlug) {
        return catalogRepository.findAll().stream()
                .filter(p -> p.titleSlug().equals(titleSlug))
                .map(CatalogProblem::difficulty)
                .findFirst()
                .orElse("Medium");
    }

    private record PoolProblem(String title, String titleSlug, String difficulty) {}
}
