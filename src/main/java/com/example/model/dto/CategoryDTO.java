package com.example.model.dto;

import com.example.common.dto.BaseDTO;
import com.example.entity.Article;
import com.example.model.enumeration.CategoryStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO extends BaseDTO {
    private String name;
    private String path;
    private List<Article> articles;
    private CategoryStatus status;
}