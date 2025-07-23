/*
 * Copyright 2016-2024 the original author or authors from the JHipster project.
 *
 * This file is part of the JHipster project, see https://www.jhipster.tech/
 * for more information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.tech.jhipster.service.filter;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Filter class for {@link LocalDateTime} type attributes.
 *
 * @see RangeFilter
 */
public class LocalDateTimeFilter extends RangeFilter<LocalDateTime> {

    private static final long serialVersionUID = 1L;

    /**
     * <p>Constructor for LocalDateTimeFilter.</p>
     */
    public LocalDateTimeFilter() {
    }

    /**
     * <p>Constructor for LocalDateTimeFilter.</p>
     *
     * @param filter a {@link LocalDateTimeFilter} object.
     */
    public LocalDateTimeFilter(LocalDateTimeFilter filter) {
        super(filter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalDateTimeFilter copy() {
        return new LocalDateTimeFilter(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @DateTimeFormat(iso = ISO.DATE_TIME)
    public LocalDateTimeFilter setEquals(LocalDateTime equals) {
        super.setEquals(equals);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @DateTimeFormat(iso = ISO.DATE_TIME)
    public LocalDateTimeFilter setNotEquals(LocalDateTime equals) {
        super.setNotEquals(equals);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @DateTimeFormat(iso = ISO.DATE_TIME)
    public LocalDateTimeFilter setIn(List<LocalDateTime> in) {
        super.setIn(in);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @DateTimeFormat(iso = ISO.DATE_TIME)
    public LocalDateTimeFilter setNotIn(List<LocalDateTime> notIn) {
        super.setNotIn(notIn);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @DateTimeFormat(iso = ISO.DATE_TIME)
    public LocalDateTimeFilter setGreaterThan(LocalDateTime equals) {
        super.setGreaterThan(equals);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @DateTimeFormat(iso = ISO.DATE_TIME)
    public LocalDateTimeFilter setLessThan(LocalDateTime equals) {
        super.setLessThan(equals);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @DateTimeFormat(iso = ISO.DATE_TIME)
    public LocalDateTimeFilter setGreaterThanOrEqual(LocalDateTime equals) {
        super.setGreaterThanOrEqual(equals);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @DateTimeFormat(iso = ISO.DATE_TIME)
    public LocalDateTimeFilter setLessThanOrEqual(LocalDateTime equals) {
        super.setLessThanOrEqual(equals);
        return this;
    }
}
