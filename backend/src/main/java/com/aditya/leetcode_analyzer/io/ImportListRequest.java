package com.aditya.leetcode_analyzer.io;

import jakarta.validation.constraints.NotBlank;

public record ImportListRequest(@NotBlank String url) {}
