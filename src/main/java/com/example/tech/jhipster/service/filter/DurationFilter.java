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

import java.time.Duration;

/**
 * Filter class for {@link Duration} type attributes.
 *
 * @see Filter
 */
public class DurationFilter extends RangeFilter<Duration> {

    private static final long serialVersionUID = 1L;

    /**
     * <p>Constructor for DurationFilter.</p>
     */
    public DurationFilter() {
    }

    /**
     * <p>Constructor for DurationFilter.</p>
     *
     * @param filter a {@link DurationFilter} object.
     */
    public DurationFilter(DurationFilter filter) {
        super(filter);
    }

    /**
     * <p>copy.</p>
     *
     * @return a {@link DurationFilter} object.
     */
    public DurationFilter copy() {
        return new DurationFilter(this);
    }
}
