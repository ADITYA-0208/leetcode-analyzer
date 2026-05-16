package com.aditya.leetcode_analyzer.entity;

public record SheetProblem(
        String title,
        String titleSlug,
        String difficulty,
        String topic,
        int order
) {}
