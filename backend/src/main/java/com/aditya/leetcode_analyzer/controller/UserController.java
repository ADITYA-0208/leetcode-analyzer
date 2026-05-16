package com.aditya.leetcode_analyzer.controller;

import com.aditya.leetcode_analyzer.io.SolvedProblemResponse;
import com.aditya.leetcode_analyzer.io.UserProfileResponse;
import com.aditya.leetcode_analyzer.service.LeetcodeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final LeetcodeService leetcodeService;

    public UserController(LeetcodeService leetcodeService) {
        this.leetcodeService = leetcodeService;
    }

    @GetMapping("/{username}/profile")
    public Mono<UserProfileResponse> getProfile(@PathVariable String username) {
        return leetcodeService.getUserProfile(username);
    }

    @GetMapping("/{username}/solved")
    public Mono<List<SolvedProblemResponse>> getSolved(@PathVariable String username) {
        return leetcodeService.getSolvedProblems(username);
    }
}
