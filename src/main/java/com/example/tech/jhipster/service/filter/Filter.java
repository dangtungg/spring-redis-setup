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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Base class for the various attribute filters. It can be added to a criteria class as a member, to support the
 * following query parameters:
 * <pre>
 *      fieldName.equals='something'
 *      fieldName.notEquals='somethingElse'
 *      fieldName.specified=true
 *      fieldName.specified=false
 *      fieldName.in='something','other'
 *      fieldName.notIn='something','other'
 * </pre>
 */
public class Filter<FIELD_TYPE> implements Serializable {

    private static final long serialVersionUID = 1L;
    private FIELD_TYPE equals;
    private FIELD_TYPE notEquals;
    private Boolean specified;
    private List<FIELD_TYPE> in;
    private List<FIELD_TYPE> notIn;

    /**
     * <p>Constructor for Filter.</p>
     */
    public Filter() {
    }

    /**
     * <p>Constructor for Filter.</p>
     *
     * @param filter a {@link Filter} object.
     */
    public Filter(Filter<FIELD_TYPE> filter) {
        equals = filter.equals;
        notEquals = filter.notEquals;
        specified = filter.specified;
        in = filter.in == null ? null : new ArrayList<>(filter.in);
        notIn = filter.notIn == null ? null : new ArrayList<>(filter.notIn);
    }

    /**
     * <p>copy.</p>
     *
     * @return a {@link Filter} object.
     */
    public Filter<FIELD_TYPE> copy() {
        return new Filter<>(this);
    }

    /**
     * <p>Getter for the field <code>equals</code>.</p>
     *
     * @return a FIELD_TYPE object.
     */
    public FIELD_TYPE getEquals() {
        return equals;
    }

    /**
     * <p>Setter for the field <code>equals</code>.</p>
     *
     * @param equals a FIELD_TYPE object.
     * @return a {@link Filter} object.
     */
    public Filter<FIELD_TYPE> setEquals(FIELD_TYPE equals) {
        this.equals = equals;
        return this;
    }

    /**
     * <p>Getter for the field <code>notEquals</code>.</p>
     *
     * @return a FIELD_TYPE object.
     */
    public FIELD_TYPE getNotEquals() {
        return notEquals;
    }

    /**
     * <p>Setter for the field <code>notEquals</code>.</p>
     *
     * @param notEquals a FIELD_TYPE object.
     * @return a {@link Filter} object.
     */
    public Filter<FIELD_TYPE> setNotEquals(FIELD_TYPE notEquals) {
        this.notEquals = notEquals;
        return this;
    }

    /**
     * <p>Getter for the field <code>specified</code>.</p>
     *
     * @return a {@link Boolean} object.
     */
    public Boolean getSpecified() {
        return specified;
    }

    /**
     * <p>Setter for the field <code>specified</code>.</p>
     *
     * @param specified a {@link Boolean} object.
     * @return a {@link Filter} object.
     */
    public Filter<FIELD_TYPE> setSpecified(Boolean specified) {
        this.specified = specified;
        return this;
    }

    /**
     * <p>Getter for the field <code>in</code>.</p>
     *
     * @return a {@link List} object.
     */
    public List<FIELD_TYPE> getIn() {
        return in;
    }

    /**
     * <p>Setter for the field <code>in</code>.</p>
     *
     * @param in a {@link List} object.
     * @return a {@link Filter} object.
     */
    public Filter<FIELD_TYPE> setIn(List<FIELD_TYPE> in) {
        this.in = in;
        return this;
    }

    /**
     * <p>Getter for the field <code>notIn</code>.</p>
     *
     * @return a {@link List} object.
     */
    public List<FIELD_TYPE> getNotIn() {
        return notIn;
    }

    /**
     * <p>Setter for the field <code>notIn</code>.</p>
     *
     * @param notIn a {@link List} object.
     * @return a {@link Filter} object.
     */
    public Filter<FIELD_TYPE> setNotIn(List<FIELD_TYPE> notIn) {
        this.notIn = notIn;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Filter<?> filter = (Filter<?>) o;
        return (
                Objects.equals(equals, filter.equals) &&
                        Objects.equals(notEquals, filter.notEquals) &&
                        Objects.equals(specified, filter.specified) &&
                        Objects.equals(in, filter.in) &&
                        Objects.equals(notIn, filter.notIn)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(equals, notEquals, specified, in, notIn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return (
                getFilterName() +
                        " [" +
                        (getEquals() != null ? "equals=" + getEquals() + ", " : "") +
                        (getNotEquals() != null ? "notEquals=" + getNotEquals() + ", " : "") +
                        (getSpecified() != null ? "specified=" + getSpecified() + ", " : "") +
                        (getIn() != null ? "in=" + getIn() + ", " : "") +
                        (getNotIn() != null ? "notIn=" + getNotIn() : "") +
                        "]"
        );
    }

    /**
     * <p>getFilterName.</p>
     *
     * @return a {@link String} object.
     */
    protected String getFilterName() {
        return getClass().getSimpleName();
    }
}
