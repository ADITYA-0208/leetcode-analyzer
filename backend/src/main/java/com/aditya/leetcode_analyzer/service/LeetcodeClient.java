package com.aditya.leetcode_analyzer.service;

import com.aditya.leetcode_analyzer.io.GraphQLRequest;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class LeetcodeClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public LeetcodeClient(WebClient webClient, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    public Mono<JsonNode> fetchUserProfile(String username) {
        String query = """
                query getUserProfile($username: String!) {
                  matchedUser(username: $username) {
                    username
                    profile { realName userAvatar }
                    submitStats: submitStatsGlobal {
                      acSubmissionNum { difficulty count submissions }
                    }
                  }
                }
                """;
        return execute(query, Map.of("username", username), null);
    }

    public Mono<JsonNode> fetchFavoriteListDetail(String favoriteSlug) {
        String query = """
                query favoriteDetailV2($favoriteSlug: String!) {
                  favoriteDetailV2(favoriteSlug: $favoriteSlug) {
                    name
                    slug
                    isPublicFavorite
                    questionNumber
                  }
                }
                """;
        return execute(query, Map.of("favoriteSlug", favoriteSlug), favoriteSlug);
    }

    public Mono<JsonNode> fetchFavoriteQuestionList(String favoriteSlug, int skip, int limit) {
        String query = """
                query favoriteQuestionList($favoriteSlug: String!, $limit: Int, $skip: Int) {
                  favoriteQuestionList(favoriteSlug: $favoriteSlug, limit: $limit, skip: $skip) {
                    questions {
                      title
                      titleSlug
                      difficulty
                    }
                    totalLength
                    hasMore
                  }
                }
                """;
        return execute(query, Map.of("favoriteSlug", favoriteSlug, "skip", skip, "limit", limit), favoriteSlug);
    }

    public Mono<JsonNode> fetchRecentAcSubmissions(String username, int limit) {
        String query = """
                query getRecentAcSubmissions($username: String!, $limit: Int!) {
                  recentAcSubmissionList(username: $username, limit: $limit) {
                    id
                    title
                    titleSlug
                    timestamp
                  }
                }
                """;
        return execute(query, Map.of("username", username, "limit", limit), null);
    }

    private Mono<JsonNode> execute(String query, Map<String, Object> variables, String favoriteSlug) {
        GraphQLRequest request = new GraphQLRequest(query, variables);
        String referer = favoriteSlug != null
                ? "https://leetcode.com/problem-list/" + favoriteSlug + "/"
                : "https://leetcode.com/";
        return webClient.post()
                .uri("/graphql")
                .header("Referer", referer)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::parseJson);
    }

    private JsonNode parseJson(String body) {
        try {
            return objectMapper.readTree(body);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse LeetCode response", ex);
        }
    }
}
