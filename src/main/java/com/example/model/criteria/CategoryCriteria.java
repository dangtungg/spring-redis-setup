package com.example.model.criteria;

import com.example.common.criteria.BaseCriteria;
import com.example.model.enumeration.CategoryStatus;
import com.example.tech.jhipster.service.filter.Filter;
import com.example.tech.jhipster.service.filter.StringFilter;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CategoryCriteria extends BaseCriteria {
    private StringFilter name;
    private StringFilter path;
    private Filter<CategoryStatus> status;

    @Override
    protected CategoryCriteria doCopy() {
        var copy = new CategoryCriteria();
        copy.setName(name == null ? null : name.copy());
        copy.setPath(path == null ? null : path.copy());
        copy.setStatus(status == null ? null : status.copy());
        return copy;
    }
}
