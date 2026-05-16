package com.aditya.leetcode_analyzer.io;

import java.util.Map;

public record GraphQLRequest(String query, Map<String, Object> variables) {}
