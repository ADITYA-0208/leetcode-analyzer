package com.aditya.leetcode_analyzer.entity;

import java.util.List;

public record LearningSheet(
        String id,
        String name,
        String description,
        List<SheetProblem> problems
) {}
