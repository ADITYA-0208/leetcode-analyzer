package com.aditya.leetcode_analyzer.repository;

import com.aditya.leetcode_analyzer.entity.BadgeDefinition;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BadgeDefinitionRepository {

    private final List<BadgeDefinition> definitions = List.of(
            new BadgeDefinition("first-look", "First Look", "Analyzed your LeetCode profile for the first time", "🔍", "bronze"),
            new BadgeDefinition("solver-50", "Rising Solver", "Solved 50+ problems on LeetCode", "📈", "bronze"),
            new BadgeDefinition("solver-100", "Century Club", "Solved 100+ problems on LeetCode", "💯", "silver"),
            new BadgeDefinition("solver-250", "Problem Crusher", "Solved 250+ problems on LeetCode", "🔥", "gold"),
            new BadgeDefinition("easy-master", "Easy Street", "Solved 50+ Easy problems", "🟢", "bronze"),
            new BadgeDefinition("medium-warrior", "Medium Warrior", "Solved 50+ Medium problems", "🟡", "silver"),
            new BadgeDefinition("hard-hunter", "Hard Hunter", "Solved 25+ Hard problems", "🔴", "gold"),
            new BadgeDefinition("striver-25", "Striver Apprentice", "Completed 25% of a Striver sheet", "📘", "bronze"),
            new BadgeDefinition("striver-50", "Striver Scholar", "Completed 50% of a Striver sheet", "📗", "silver"),
            new BadgeDefinition("striver-100", "Striver Master", "Completed 100% of a Striver sheet", "📕", "gold"),
            new BadgeDefinition("balanced", "Well Rounded", "At least 30 Easy, 30 Medium, and 10 Hard solved", "⚖️", "gold")
    );

    public List<BadgeDefinition> findAll() {
        return definitions;
    }
}
