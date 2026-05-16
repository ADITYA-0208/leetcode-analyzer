package com.aditya.leetcode_analyzer.io;

public record ImportedListProblemStatusResponse(
        String title,
        String titleSlug,
        String difficulty,
        boolean solved,
        String leetcodeUrl
) {}
