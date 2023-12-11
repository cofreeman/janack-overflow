package com.example.janackoverflow.community.repository;

import com.example.janackoverflow.issue.entity.Issue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommunityRepository {
    public Page<Issue> findAllByKeyword(String keyword, String title, Pageable pageable);
    public Page<Issue> findAllByCategory(String title, List<String> category, Pageable pageable);
}