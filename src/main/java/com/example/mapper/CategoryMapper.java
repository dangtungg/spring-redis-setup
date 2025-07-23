package com.example.mapper;

import com.example.common.mapper.EntityMapper;
import com.example.entity.Category;
import com.example.model.dto.CategoryDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ArticleMapper.class})
public interface CategoryMapper extends EntityMapper<CategoryDTO, Category> {

    @Mapping(target = "articles", source = "articles")
    CategoryDTO toDTO(Category entity);

    @Mapping(target = "articles", source = "articles")
    Category toEntity(CategoryDTO dto);
}
