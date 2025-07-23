package com.example.service;

import com.example.common.exception.ResourceNotFoundException;
import com.example.common.repository.BaseRepository;
import com.example.common.service.BaseQueryService;
import com.example.common.service.BaseServiceImpl;
import com.example.config.CacheConfig;
import com.example.entity.Article;
import com.example.mapper.ArticleMapper;
import com.example.model.criteria.ArticleCriteria;
import com.example.model.dto.ArticleDTO;
import com.example.repository.ArticleRepository;
import com.example.service.query.ArticleQueryService;
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
public class ArticleService extends BaseServiceImpl<Article, ArticleDTO, ArticleCriteria> {

    private final ArticleRepository repository;
    private final ArticleQueryService queryService;
    private final ArticleMapper mapper;

    @Override
    protected BaseRepository<Article> getRepository() {
        return repository;
    }

    @Override
    protected BaseQueryService<Article, ArticleCriteria> getQueryService() {
        return queryService;
    }

    @Override
    protected Article toEntity(ArticleDTO dto) {
        return mapper.toEntity(dto);
    }

    @Override
    protected ArticleDTO toDTO(Article entity) {
        return mapper.toDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CacheNames.ARTICLE, key = "'dto_' + #id")
    public ArticleDTO findById(Long id) {
        return super.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CacheNames.ALL_ARTICLES, key = "'dto_' + (#criteria != null ? #criteria.hashCode() : 'all')")
    public List<ArticleDTO> findByCriteria(ArticleCriteria criteria) {
        return super.findByCriteria(criteria);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ArticleDTO> findByCriteria(ArticleCriteria criteria, Pageable pageable) {
        return super.findByCriteria(criteria, pageable);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CacheNames.ARTICLE_BY_NAME, key = "'entity_' + #name")
    public Article getByName(String name) {
        return repository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found with name: " + name));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CacheNames.ARTICLE_BY_NAME, key = "'entity_' + #name")
    public Article getByNameNullable(String name) {
        return repository.findByName(name).orElse(null);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CacheNames.ARTICLE_BY_NAME, key = "'dto_' + #name")
    public ArticleDTO getDTOByName(String name) {
        return repository.findByName(name)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found with name: " + name));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CacheNames.ARTICLE_BY_PATH, key = "'entity_' + #path")
    public Article getByPath(String path) {
        return repository.findByPath(path)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found with path: " + path));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CacheNames.ARTICLE_BY_PATH, key = "'entity_' + #path")
    public Article getByPathNullable(String path) {
        return repository.findByPath(path).orElse(null);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CacheNames.ARTICLE_BY_PATH, key = "'dto_' + #path")
    public ArticleDTO getDTOByPath(String path) {
        return repository.findByPath(path)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found with path: " + path));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CacheNames.ARTICLE, key = "'dto_' + #result.id"),
            @CacheEvict(value = CacheConfig.CacheNames.ARTICLE_BY_NAME, key = "'entity_' + #result.name"),
            @CacheEvict(value = CacheConfig.CacheNames.ARTICLE_BY_NAME, key = "'dto_' + #result.name"),
            @CacheEvict(value = CacheConfig.CacheNames.ARTICLE_BY_PATH, key = "'entity_' + #result.path"),
            @CacheEvict(value = CacheConfig.CacheNames.ARTICLE_BY_PATH, key = "'dto_' + #result.path"),
            @CacheEvict(value = CacheConfig.CacheNames.ALL_ARTICLES, allEntries = true)
    })
    public ArticleDTO create(ArticleDTO dto) {
        return super.create(dto);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CacheNames.ARTICLE, key = "'dto_' + #result.id"),
            @CacheEvict(value = CacheConfig.CacheNames.ARTICLE_BY_NAME, key = "'entity_' + #result.name"),
            @CacheEvict(value = CacheConfig.CacheNames.ARTICLE_BY_NAME, key = "'dto_' + #result.name"),
            @CacheEvict(value = CacheConfig.CacheNames.ARTICLE_BY_PATH, key = "'entity_' + #result.path"),
            @CacheEvict(value = CacheConfig.CacheNames.ARTICLE_BY_PATH, key = "'dto_' + #result.path"),
            @CacheEvict(value = CacheConfig.CacheNames.ALL_ARTICLES, allEntries = true)
    })
    public ArticleDTO update(ArticleDTO dto) {
        return super.update(dto);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CacheNames.ARTICLE, key = "'dto_' + #id"),
            @CacheEvict(value = CacheConfig.CacheNames.ARTICLE_BY_NAME, allEntries = true),
            @CacheEvict(value = CacheConfig.CacheNames.ARTICLE_BY_PATH, allEntries = true),
            @CacheEvict(value = CacheConfig.CacheNames.ALL_ARTICLES, allEntries = true)
    })
    public void delete(Long id) {
        super.delete(id);
    }
}
