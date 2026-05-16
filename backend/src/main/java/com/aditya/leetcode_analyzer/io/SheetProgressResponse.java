package com.aditya.leetcode_analyzer.io;

import java.util.List;

public record SheetProgressResponse(
        String sheetId,
        String sheetName,
        int totalProblems,
        int solvedCount,
        double progressPercent,
        List<SheetProblemStatusResponse> problems
) {}
