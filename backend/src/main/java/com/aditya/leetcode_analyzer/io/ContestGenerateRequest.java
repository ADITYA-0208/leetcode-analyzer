package com.aditya.leetcode_analyzer.io;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ContestGenerateRequest(
        @NotBlank String username,
        @NotNull PoolSource pool,
        String sheetId,
        String listId,
        @Min(0) int easyCount,
        @Min(0) int mediumCount,
        @Min(0) int hardCount,
        @Min(1) @Max(50) int totalQuestions,
        @Min(5) @Max(300) int durationMinutes
) {
    public enum PoolSource {
        SOLVED,
        CATALOG,
        SHEET,
        CUSTOM_LIST
    }
}
