package com.aditya.leetcode_analyzer.io;

public record ContestProblemResponse(
        int number,
        String title,
        String titleSlug,
        String difficulty,
        String leetcodeUrl
) {}
