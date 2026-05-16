package com.aditya.leetcode_analyzer.controller;

import com.aditya.leetcode_analyzer.io.ContestGenerateRequest;
import com.aditya.leetcode_analyzer.io.ContestResponse;
import com.aditya.leetcode_analyzer.service.ContestService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/contests")
public class ContestController {

    private final ContestService contestService;

    public ContestController(ContestService contestService) {
        this.contestService = contestService;
    }

    @PostMapping("/generate")
    public Mono<ContestResponse> generate(@Valid @RequestBody ContestGenerateRequest request) {
        return contestService.generateContest(request);
    }
}
