package com.example.common.criteria;

import com.example.tech.jhipster.service.Criteria;
import com.example.tech.jhipster.service.filter.BooleanFilter;
import com.example.tech.jhipster.service.filter.InstantFilter;
import com.example.tech.jhipster.service.filter.LongFilter;
import com.example.tech.jhipster.service.filter.StringFilter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode
public abstract class BaseCriteria implements Criteria {
    protected LongFilter id;
    protected LongFilter version;
    protected BooleanFilter isActive;
    protected StringFilter createdBy;
    protected InstantFilter createdAt;
    protected StringFilter lastModifiedBy;
    protected InstantFilter lastModifiedAt;

    @Override
    public Criteria copy() {
        var copied = doCopy();
        copied.setId(this.id == null ? null : this.id.copy());
        copied.setVersion(this.version == null ? null : this.version.copy());
        copied.setIsActive(this.isActive == null ? null : this.isActive.copy());
        copied.setCreatedBy(this.createdBy == null ? null : this.createdBy.copy());
        copied.setCreatedAt(this.createdAt == null ? null : this.createdAt.copy());
        copied.setLastModifiedBy(this.lastModifiedBy == null ? null : this.lastModifiedBy.copy());
        copied.setLastModifiedAt(this.lastModifiedAt == null ? null : this.lastModifiedAt.copy());
        return copied;
    }

    protected abstract BaseCriteria doCopy();
}
