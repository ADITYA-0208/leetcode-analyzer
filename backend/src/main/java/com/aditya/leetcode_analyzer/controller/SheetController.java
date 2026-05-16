package com.aditya.leetcode_analyzer.controller;

import com.aditya.leetcode_analyzer.entity.LearningSheet;
import com.aditya.leetcode_analyzer.io.SheetProgressResponse;
import com.aditya.leetcode_analyzer.service.SheetComparisonService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/sheets")
public class SheetController {

    private final SheetComparisonService sheetComparisonService;

    public SheetController(SheetComparisonService sheetComparisonService) {
        this.sheetComparisonService = sheetComparisonService;
    }

    @GetMapping
    public List<LearningSheet> listSheets() {
        return sheetComparisonService.listSheets();
    }

    @GetMapping("/{sheetId}/progress/{username}")
    public Mono<SheetProgressResponse> getProgress(
            @PathVariable String sheetId,
            @PathVariable String username
    ) {
        return sheetComparisonService.compareSheet(username, sheetId);
    }

    @GetMapping("/progress/{username}")
    public Mono<List<SheetProgressResponse>> getAllProgress(@PathVariable String username) {
        return sheetComparisonService.compareAllSheets(username);
    }
}
