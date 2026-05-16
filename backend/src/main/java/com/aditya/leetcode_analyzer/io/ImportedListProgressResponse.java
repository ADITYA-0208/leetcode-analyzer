package com.aditya.leetcode_analyzer.io;

import java.util.List;

public record ImportedListProgressResponse(
        String id,
        String name,
        int totalProblems,
        int solvedCount,
        double progressPercent,
        List<ImportedListProblemStatusResponse> problems
) {}
