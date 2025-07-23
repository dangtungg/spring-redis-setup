package com.example.repository;

import com.example.common.repository.BaseRepository;
import com.example.entity.Category;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends BaseRepository<Category> {

    Optional<Category> findByName(String name);

    Optional<Category> findByPath(String path);
}