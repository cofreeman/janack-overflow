package com.example.janackoverflow.community.repository;

import com.example.janackoverflow.community.entity.Likes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikesRepository extends JpaRepository<Likes, Long> {
    public long countLikesByIssueId(Long issueId);

}
