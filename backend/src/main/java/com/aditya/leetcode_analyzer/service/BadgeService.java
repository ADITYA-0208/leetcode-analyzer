package com.aditya.leetcode_analyzer.service;

import com.aditya.leetcode_analyzer.entity.BadgeDefinition;
import com.aditya.leetcode_analyzer.io.BadgeResponse;
import com.aditya.leetcode_analyzer.io.SheetProgressResponse;
import com.aditya.leetcode_analyzer.io.UserProfileResponse;
import com.aditya.leetcode_analyzer.repository.BadgeDefinitionRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Service
public class BadgeService {

    private final BadgeDefinitionRepository badgeDefinitionRepository;
    private final LeetcodeService leetcodeService;
    private final SheetComparisonService sheetComparisonService;

    public BadgeService(
            BadgeDefinitionRepository badgeDefinitionRepository,
            LeetcodeService leetcodeService,
            SheetComparisonService sheetComparisonService
    ) {
        this.badgeDefinitionRepository = badgeDefinitionRepository;
        this.leetcodeService = leetcodeService;
        this.sheetComparisonService = sheetComparisonService;
    }

    public Mono<List<BadgeResponse>> getBadges(String username) {
        return Mono.zip(
                leetcodeService.getUserProfile(username),
                sheetComparisonService.compareAllSheets(username)
        ).map(tuple -> evaluateBadges(tuple.getT1(), tuple.getT2()));
    }

    private List<BadgeResponse> evaluateBadges(UserProfileResponse profile, List<SheetProgressResponse> sheets) {
        double bestSheetProgress = sheets.stream()
                .mapToDouble(SheetProgressResponse::progressPercent)
                .max()
                .orElse(0);

        List<Predicate<String>> rules = List.of(
                id -> true,
                id -> profile.totalSolved() >= 50,
                id -> profile.totalSolved() >= 100,
                id -> profile.totalSolved() >= 250,
                id -> profile.easySolved() >= 50,
                id -> profile.mediumSolved() >= 50,
                id -> profile.hardSolved() >= 25,
                id -> bestSheetProgress >= 25,
                id -> bestSheetProgress >= 50,
                id -> bestSheetProgress >= 100,
                id -> profile.easySolved() >= 30 && profile.mediumSolved() >= 30 && profile.hardSolved() >= 10
        );

        List<BadgeDefinition> definitions = badgeDefinitionRepository.findAll();
        List<BadgeResponse> badges = new ArrayList<>();

        for (int i = 0; i < definitions.size(); i++) {
            BadgeDefinition definition = definitions.get(i);
            boolean earned = i < rules.size() && rules.get(i).test(definition.id());
            badges.add(new BadgeResponse(
                    definition.id(),
                    definition.name(),
                    definition.description(),
                    definition.icon(),
                    earned,
                    definition.tier()
            ));
        }

        return badges;
    }
}
