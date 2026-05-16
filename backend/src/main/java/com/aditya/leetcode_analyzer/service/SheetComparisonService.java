package com.aditya.leetcode_analyzer.service;

import com.aditya.leetcode_analyzer.entity.LearningSheet;
import com.aditya.leetcode_analyzer.entity.SheetProblem;
import com.aditya.leetcode_analyzer.io.SheetProblemStatusResponse;
import com.aditya.leetcode_analyzer.io.SheetProgressResponse;
import com.aditya.leetcode_analyzer.repository.SheetRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

@Service
public class SheetComparisonService {

    private static final String PROBLEM_URL = "https://leetcode.com/problems/";

    private final SheetRepository sheetRepository;
    private final LeetcodeService leetcodeService;

    public SheetComparisonService(SheetRepository sheetRepository, LeetcodeService leetcodeService) {
        this.sheetRepository = sheetRepository;
        this.leetcodeService = leetcodeService;
    }

    public List<LearningSheet> listSheets() {
        return sheetRepository.findAll();
    }

    public Mono<SheetProgressResponse> compareSheet(String username, String sheetId) {
        LearningSheet sheet = sheetRepository.findById(sheetId)
                .orElseThrow(() -> new SheetNotFoundException("Sheet not found: " + sheetId));

        return leetcodeService.getSolvedSlugs(username)
                .map(solvedSlugs -> buildProgress(sheet, solvedSlugs));
    }

    public Mono<List<SheetProgressResponse>> compareAllSheets(String username) {
        return leetcodeService.getSolvedSlugs(username)
                .map(solvedSlugs -> sheetRepository.findAll().stream()
                        .map(sheet -> buildProgress(sheet, solvedSlugs))
                        .toList());
    }

    private SheetProgressResponse buildProgress(LearningSheet sheet, Set<String> solvedSlugs) {
        List<SheetProblemStatusResponse> problems = sheet.problems().stream()
                .map(problem -> toStatus(problem, solvedSlugs.contains(problem.titleSlug())))
                .toList();

        int solvedCount = (int) problems.stream().filter(SheetProblemStatusResponse::solved).count();
        int total = problems.size();
        double percent = total == 0 ? 0 : (solvedCount * 100.0) / total;

        return new SheetProgressResponse(
                sheet.id(),
                sheet.name(),
                total,
                solvedCount,
                Math.round(percent * 10.0) / 10.0,
                problems
        );
    }

    private SheetProblemStatusResponse toStatus(SheetProblem problem, boolean solved) {
        return new SheetProblemStatusResponse(
                problem.title(),
                problem.titleSlug(),
                problem.difficulty(),
                problem.topic(),
                problem.order(),
                solved,
                PROBLEM_URL + problem.titleSlug() + "/"
        );
    }

    public static class SheetNotFoundException extends RuntimeException {
        public SheetNotFoundException(String message) {
            super(message);
        }
    }
}
