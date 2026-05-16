package com.aditya.leetcode_analyzer.controller;

import com.aditya.leetcode_analyzer.io.ImportListRequest;
import com.aditya.leetcode_analyzer.io.ImportedListProgressResponse;
import com.aditya.leetcode_analyzer.io.ImportedListResponse;
import com.aditya.leetcode_analyzer.io.ImportedListSummaryResponse;
import com.aditya.leetcode_analyzer.service.ProblemListService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/lists")
public class ProblemListController {

    private final ProblemListService problemListService;

    public ProblemListController(ProblemListService problemListService) {
        this.problemListService = problemListService;
    }

    @PostMapping("/import")
    public Mono<ImportedListResponse> importList(@Valid @RequestBody ImportListRequest request) {
        return problemListService.importFromUrl(request.url());
    }

    @GetMapping
    public List<ImportedListSummaryResponse> listImported() {
        return problemListService.listImported();
    }

    @GetMapping("/{listId}")
    public ImportedListResponse getList(@PathVariable String listId) {
        return problemListService.getById(listId);
    }

    @GetMapping("/{listId}/progress/{username}")
    public Mono<ImportedListProgressResponse> getProgress(
            @PathVariable String listId,
            @PathVariable String username
    ) {
        return problemListService.getProgress(listId, username);
    }

    @DeleteMapping("/{listId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteList(@PathVariable String listId) {
        problemListService.delete(listId);
    }
}
