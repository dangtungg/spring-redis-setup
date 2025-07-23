package com.example.service.query;

import com.example.common.service.BaseQueryService;
import com.example.entity.Article;
import com.example.entity.Article_;
import com.example.model.criteria.ArticleCriteria;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ArticleQueryService extends BaseQueryService<Article, ArticleCriteria> {

    @Override
    protected Specification<Article> doCreateSpecification(ArticleCriteria criteria) {
        Specification<Article> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getName(), Article_.name));
            }
            if (criteria.getPath() != null) {
                specification = specification.and(buildStringSpecification(criteria.getPath(), Article_.path));
            }
            if (criteria.getStatus() != null) {
                specification = specification.and(buildSpecification(criteria.getStatus(), Article_.status));
            }
        }
        return specification;
    }

}
