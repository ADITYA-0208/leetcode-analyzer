package com.aditya.leetcode_analyzer.io;

public record SolvedProblemResponse(
        String title,
        String titleSlug,
        long timestamp,
        String leetcodeUrl
) {}
