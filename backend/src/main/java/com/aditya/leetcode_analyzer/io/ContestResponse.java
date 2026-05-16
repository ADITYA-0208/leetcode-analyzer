package com.aditya.leetcode_analyzer.io;

import java.util.List;

public record ContestResponse(
        String contestId,
        int durationMinutes,
        int totalQuestions,
        List<ContestProblemResponse> problems
) {}
