package com.aditya.leetcode_analyzer.io;

import java.util.List;

public record ImportedListResponse(
        String id,
        String name,
        String slug,
        String sourceUrl,
        int totalProblems,
        List<ImportedProblemResponse> problems
) {}
