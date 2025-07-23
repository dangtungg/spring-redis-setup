package com.example.service.query;

import com.example.common.service.BaseQueryService;
import com.example.entity.Category;
import com.example.entity.Category_;
import com.example.model.criteria.CategoryCriteria;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CategoryQueryService extends BaseQueryService<Category, CategoryCriteria> {

    @Override
    protected Specification<Category> doCreateSpecification(CategoryCriteria criteria) {
        Specification<Category> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getName(), Category_.name));
            }
            if (criteria.getPath() != null) {
                specification = specification.and(buildStringSpecification(criteria.getPath(), Category_.path));
            }
            if (criteria.getStatus() != null) {
                specification = specification.and(buildSpecification(criteria.getStatus(), Category_.status));
            }
        }
        return specification;
    }

}
