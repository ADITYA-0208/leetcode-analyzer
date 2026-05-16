package com.aditya.leetcode_analyzer.io;

public record BadgeResponse(
        String id,
        String name,
        String description,
        String icon,
        boolean earned,
        String tier
) {}
