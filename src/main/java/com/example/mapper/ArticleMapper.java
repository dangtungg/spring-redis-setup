package com.example.mapper;

import com.example.common.mapper.EntityMapper;
import com.example.entity.Article;
import com.example.model.dto.ArticleDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ArticleMapper extends EntityMapper<ArticleDTO, Article> {

    // Avoid circular reference
    @Mapping(target = "category", ignore = true)
    Article toEntity(ArticleDTO dto);

    @Mapping(target = "category", ignore = true)
    ArticleDTO toDTO(Article entity);
}
