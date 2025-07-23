package com.example.repository;

import com.example.common.repository.BaseRepository;
import com.example.entity.Article;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleRepository extends BaseRepository<Article> {

    Optional<Article> findByName(String name);

    Optional<Article> findByPath(String path);
}
