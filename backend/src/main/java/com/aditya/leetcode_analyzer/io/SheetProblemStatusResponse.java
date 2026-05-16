package com.aditya.leetcode_analyzer.io;

public record SheetProblemStatusResponse(
        String title,
        String titleSlug,
        String difficulty,
        String topic,
        int order,
        boolean solved,
        String leetcodeUrl
) {}
