package com.aditya.leetcode_analyzer.entity;

import java.util.List;

public record ImportedList(
        String id,
        String name,
        String slug,
        String sourceUrl,
        List<ImportedListProblem> problems
) {}
