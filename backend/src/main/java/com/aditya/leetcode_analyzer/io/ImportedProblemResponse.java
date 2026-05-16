package com.aditya.leetcode_analyzer.io;

public record ImportedProblemResponse(
        String title,
        String titleSlug,
        String difficulty,
        String leetcodeUrl
) {}
