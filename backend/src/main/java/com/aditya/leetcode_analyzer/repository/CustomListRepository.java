package com.aditya.leetcode_analyzer.repository;

import com.aditya.leetcode_analyzer.entity.ImportedList;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class CustomListRepository {

    private final Map<String, ImportedList> listsById = new ConcurrentHashMap<>();

    public ImportedList save(ImportedList list) {
        listsById.put(list.id(), list);
        return list;
    }

    public Optional<ImportedList> findById(String id) {
        return Optional.ofNullable(listsById.get(id));
    }

    public List<ImportedList> findAll() {
        return listsById.values().stream()
                .sorted(Comparator.comparing(ImportedList::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public boolean deleteById(String id) {
        return listsById.remove(id) != null;
    }
}
