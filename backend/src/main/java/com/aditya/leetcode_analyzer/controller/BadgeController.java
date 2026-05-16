package com.aditya.leetcode_analyzer.controller;

import com.aditya.leetcode_analyzer.io.BadgeResponse;
import com.aditya.leetcode_analyzer.service.BadgeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/badges")
public class BadgeController {

    private final BadgeService badgeService;

    public BadgeController(BadgeService badgeService) {
        this.badgeService = badgeService;
    }

    @GetMapping("/{username}")
    public Mono<List<BadgeResponse>> getBadges(@PathVariable String username) {
        return badgeService.getBadges(username);
    }
}
