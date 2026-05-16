package com.aditya.leetcode_analyzer.io;

public record ImportedListSummaryResponse(
        String id,
        String name,
        String slug,
        String sourceUrl,
        int totalProblems
) {}
