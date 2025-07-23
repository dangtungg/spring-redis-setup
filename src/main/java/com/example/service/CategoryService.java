package com.example.service;

import com.example.common.exception.ResourceNotFoundException;
import com.example.common.repository.BaseRepository;
import com.example.common.service.BaseQueryService;
import com.example.common.service.BaseServiceImpl;
import com.example.config.CacheConfig;
import com.example.entity.Category;
import com.example.mapper.CategoryMapper;
import com.example.model.criteria.CategoryCriteria;
import com.example.model.dto.CategoryDTO;
import com.example.repository.CategoryRepository;
import com.example.service.query.CategoryQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService extends BaseServiceImpl<Category, CategoryDTO, CategoryCriteria> {

    private final CategoryRepository repository;
    private final CategoryQueryService queryService;
    private final CategoryMapper mapper;

    @Override
    protected BaseRepository<Category> getRepository() {
        return repository;
    }

    @Override
    protected BaseQueryService<Category, CategoryCriteria> getQueryService() {
        return queryService;
    }

    @Override
    protected Category toEntity(CategoryDTO dto) {
        return mapper.toEntity(dto);
    }

    @Override
    protected CategoryDTO toDTO(Category entity) {
        return mapper.toDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CacheNames.CATEGORY, key = "'dto_' + #id")
    public CategoryDTO findById(Long id) {
        return super.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CacheNames.ALL_CATEGORIES, key = "'dto_' + (#criteria != null ? #criteria.hashCode() : 'all')")
    public List<CategoryDTO> findByCriteria(CategoryCriteria criteria) {
        return super.findByCriteria(criteria);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryDTO> findByCriteria(CategoryCriteria criteria, Pageable pageable) {
        return super.findByCriteria(criteria, pageable);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CacheNames.CATEGORY_BY_NAME, key = "'entity_' + #name")
    public Category getByName(String name) {
        return repository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with name: " + name));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CacheNames.CATEGORY_BY_NAME, key = "'entity_' + #name")
    public Category getByNameNullable(String name) {
        return repository.findByName(name).orElse(null);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CacheNames.CATEGORY_BY_NAME, key = "'dto_' + #name")
    public CategoryDTO getDTOByName(String name) {
        return repository.findByName(name)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with name: " + name));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CacheNames.CATEGORY_BY_PATH, key = "'entity_' + #path")
    public Category getByPath(String path) {
        return repository.findByPath(path)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with path: " + path));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CacheNames.CATEGORY_BY_PATH, key = "'entity_' + #path")
    public Category getByPathNullable(String path) {
        return repository.findByPath(path).orElse(null);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CacheNames.CATEGORY_BY_PATH, key = "'dto_' + #path")
    public CategoryDTO getDTOByPath(String path) {
        return repository.findByPath(path)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with path: " + path));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CacheNames.CATEGORY, key = "'dto_' + #result.id"),
            @CacheEvict(value = CacheConfig.CacheNames.CATEGORY_BY_NAME, key = "'entity_' + #result.name"),
            @CacheEvict(value = CacheConfig.CacheNames.CATEGORY_BY_NAME, key = "'dto_' + #result.name"),
            @CacheEvict(value = CacheConfig.CacheNames.CATEGORY_BY_PATH, key = "'entity_' + #result.path"),
            @CacheEvict(value = CacheConfig.CacheNames.CATEGORY_BY_PATH, key = "'dto_' + #result.path"),
            @CacheEvict(value = CacheConfig.CacheNames.ALL_CATEGORIES, allEntries = true)
    })
    public CategoryDTO create(CategoryDTO dto) {
        return super.create(dto);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CacheNames.CATEGORY, key = "'dto_' + #result.id"),
            @CacheEvict(value = CacheConfig.CacheNames.CATEGORY_BY_NAME, key = "'entity_' + #result.name"),
            @CacheEvict(value = CacheConfig.CacheNames.CATEGORY_BY_NAME, key = "'dto_' + #result.name"),
            @CacheEvict(value = CacheConfig.CacheNames.CATEGORY_BY_PATH, key = "'entity_' + #result.path"),
            @CacheEvict(value = CacheConfig.CacheNames.CATEGORY_BY_PATH, key = "'dto_' + #result.path"),
            @CacheEvict(value = CacheConfig.CacheNames.ALL_CATEGORIES, allEntries = true)
    })
    public CategoryDTO update(CategoryDTO dto) {
        return super.update(dto);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CacheNames.CATEGORY, key = "'dto_' + #id"),
            @CacheEvict(value = CacheConfig.CacheNames.CATEGORY_BY_NAME, allEntries = true),
            @CacheEvict(value = CacheConfig.CacheNames.CATEGORY_BY_PATH, allEntries = true),
            @CacheEvict(value = CacheConfig.CacheNames.ALL_CATEGORIES, allEntries = true)
    })
    public void delete(Long id) {
        super.delete(id);
    }
}
