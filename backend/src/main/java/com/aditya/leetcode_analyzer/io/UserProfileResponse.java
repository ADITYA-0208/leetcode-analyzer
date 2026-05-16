package com.aditya.leetcode_analyzer.io;

import java.util.List;

public record UserProfileResponse(
        String username,
        String realName,
        String avatar,
        int totalSolved,
        int easySolved,
        int mediumSolved,
        int hardSolved,
        List<SolvedProblemResponse> recentSolved
) {}
