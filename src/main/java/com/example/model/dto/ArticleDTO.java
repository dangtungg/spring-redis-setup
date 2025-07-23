package com.example.model.dto;

import com.example.common.dto.BaseDTO;
import com.example.entity.Category;
import com.example.model.enumeration.ArticleStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDTO extends BaseDTO {
    private String name;
    private String path;
    private String summary;
    private String content;
    private Category category;
    private ArticleStatus status;
}
