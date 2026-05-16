package com.aditya.leetcode_analyzer.service;

import com.aditya.leetcode_analyzer.io.SolvedProblemResponse;
import com.aditya.leetcode_analyzer.io.UserProfileResponse;
import tools.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LeetcodeService {

    private static final String PROBLEM_URL = "https://leetcode.com/problems/";
    private static final int SUBMISSION_FETCH_LIMIT = 2000;

    private final LeetcodeClient leetcodeClient;

    public LeetcodeService(LeetcodeClient leetcodeClient) {
        this.leetcodeClient = leetcodeClient;
    }

    public Mono<UserProfileResponse> getUserProfile(String username) {
        return Mono.zip(
                leetcodeClient.fetchUserProfile(username),
                leetcodeClient.fetchRecentAcSubmissions(username, SUBMISSION_FETCH_LIMIT)
        ).map(tuple -> buildProfile(username, tuple.getT1(), tuple.getT2()));
    }

    public Mono<List<SolvedProblemResponse>> getSolvedProblems(String username) {
        return leetcodeClient.fetchRecentAcSubmissions(username, SUBMISSION_FETCH_LIMIT)
                .map(node -> extractSolvedProblems(node));
    }

    public Mono<Set<String>> getSolvedSlugs(String username) {
        return getSolvedProblems(username)
                .map(problems -> problems.stream()
                        .map(SolvedProblemResponse::titleSlug)
                        .collect(Collectors.toSet()));
    }

    private UserProfileResponse buildProfile(String username, JsonNode profileNode, JsonNode submissionsNode) {
        JsonNode matchedUser = profileNode.path("data").path("matchedUser");
        if (matchedUser.isMissingNode() || matchedUser.isNull()) {
            throw new UserNotFoundException("LeetCode user not found: " + username);
        }

        JsonNode errors = profileNode.path("errors");
        if (errors.isArray() && !errors.isEmpty()) {
            throw new UserNotFoundException(errors.get(0).path("message").asText("User not found"));
        }

        String resolvedUsername = matchedUser.path("username").asText(username);
        String realName = matchedUser.path("profile").path("realName").asText("");
        String avatar = matchedUser.path("profile").path("userAvatar").asText("");

        int total = 0;
        int easy = 0;
        int medium = 0;
        int hard = 0;

        for (JsonNode stat : matchedUser.path("submitStats").path("acSubmissionNum")) {
            String difficulty = stat.path("difficulty").asText();
            int count = stat.path("count").asInt();
            switch (difficulty) {
                case "All" -> total = count;
                case "Easy" -> easy = count;
                case "Medium" -> medium = count;
                case "Hard" -> hard = count;
                default -> { }
            }
        }

        List<SolvedProblemResponse> solved = extractSolvedProblems(submissionsNode);

        return new UserProfileResponse(
                resolvedUsername,
                realName,
                avatar,
                total,
                easy,
                medium,
                hard,
                solved
        );
    }

    private List<SolvedProblemResponse> extractSolvedProblems(JsonNode node) {
        Map<String, SolvedProblemResponse> uniqueBySlug = new LinkedHashMap<>();

        for (JsonNode submission : node.path("data").path("recentAcSubmissionList")) {
            String slug = submission.path("titleSlug").asText();
            if (slug.isBlank()) {
                continue;
            }
            long timestamp = parseTimestamp(submission.path("timestamp"));
            SolvedProblemResponse candidate = new SolvedProblemResponse(
                    submission.path("title").asText(),
                    slug,
                    timestamp,
                    PROBLEM_URL + slug + "/"
            );
            uniqueBySlug.merge(slug, candidate, (existing, incoming) ->
                    incoming.timestamp() > existing.timestamp() ? incoming : existing);
        }

        return uniqueBySlug.values().stream()
                .sorted(Comparator.comparingLong(SolvedProblemResponse::timestamp).reversed())
                .toList();
    }

    private long parseTimestamp(JsonNode node) {
        if (node.isNumber()) {
            return node.asLong();
        }
        if (node.isTextual()) {
            try {
                return Long.parseLong(node.asText());
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }
}
