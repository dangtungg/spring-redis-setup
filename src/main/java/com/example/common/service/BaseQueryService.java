package com.example.common.service;

import com.example.common.criteria.BaseCriteria;
import com.example.entity.BaseEntity;
import com.example.entity.BaseEntity_;
import com.example.tech.jhipster.service.QueryService;
import org.springframework.data.jpa.domain.Specification;

public abstract class BaseQueryService<E extends BaseEntity, C extends BaseCriteria> extends QueryService<E> {

    public Specification<E> createSpecification(C criteria) {
        Specification<E> specification = doCreateSpecification(criteria);

        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), BaseEntity_.id));
            }
            if (criteria.getVersion() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getVersion(), BaseEntity_.version));
            }
            if (criteria.getIsActive() != null) {
                specification = specification.and(buildSpecification(criteria.getIsActive(), BaseEntity_.isActive));
            }
            if (criteria.getCreatedBy() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCreatedBy(), BaseEntity_.createdBy));
            }
            if (criteria.getCreatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreatedAt(), BaseEntity_.createdAt));
            }
            if (criteria.getLastModifiedBy() != null) {
                specification = specification.and(buildStringSpecification(criteria.getLastModifiedBy(), BaseEntity_.lastModifiedBy));
            }
            if (criteria.getLastModifiedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getLastModifiedAt(), BaseEntity_.lastModifiedAt));
            }
        }

        return specification;
    }

    protected abstract Specification<E> doCreateSpecification(C criteria);

}
