package com.example.common.service;

import com.example.common.criteria.BaseCriteria;
import com.example.common.dto.BaseDTO;
import com.example.entity.BaseEntity;
import com.example.model.dto.PartialUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface BaseService<E extends BaseEntity, D extends BaseDTO, C extends BaseCriteria> {
    D findById(Long id);

    D create(D dto);

    D update(D dto);

    D partialUpdate(Long id, PartialUpdateDTO<D> partialDTO);

    void delete(Long id);

    void deactivate(Long id);

    void activate(Long id);

    List<D> findByCreatedBy(String createdBy);

    List<D> findByCriteria(C criteria);

    List<D> findByDateRange(Instant start, Instant end);

    Page<D> findByCriteria(C criteria, Pageable pageable);

    /**
     * Validate DTO before creation. Override this method to add custom validation logic.
     *
     * @param dto DTO to be validated
     */
    void validateBeforeCreate(D dto);

    /**
     * Validate DTO before update. Override this method to add custom validation logic.
     *
     * @param dto DTO to be validated
     */
    void validateBeforeUpdate(D dto);

    /**
     * Validate fields before a partial update. Override this method to add custom validation logic.
     *
     * @param fields Map of fields to be validated
     */
    void validateFieldsBeforePartialUpdate(Map<String, Object> fields);

    /**
     * Validate DTO before a partial update. Override this method to add custom validation logic.
     *
     * @param dto           DTO to be validated
     * @param updatedFields Map of updated fields
     */
    void validateBeforePartialUpdate(D dto, Map<String, Object> updatedFields);

    /**
     * Prepare entity for creation. Called after validation but before save.
     * Override this method to set default values or adjust fields before persisting.
     *
     * @param entity Entity to be prepared
     * @param dto    DTO with original input data
     */
    void prepareForCreate(E entity, D dto);

    /**
     * Prepare entity for update. Called after validation but before save.
     * Override this method to adjust fields or handle business logic before persisting.
     *
     * @param entity         Entity to be prepared
     * @param existingEntity Existing entity from the database
     */
    void prepareForUpdate(E entity, E existingEntity);

    /**
     * Post-processing after entity creation.
     * Override this method to perform additional operations after the entity is saved.
     *
     * @param entity Saved entity
     * @param dto    Original DTO
     */
    void afterCreate(E entity, D dto);

    /**
     * Post-processing after entity update.
     * Override this method to perform additional operations after the entity is updated.
     *
     * @param entity Updated entity
     * @param dto    DTO with updated data
     */
    void afterUpdate(E entity, D dto);

    /**
     * Post-processing after entity partial update.
     * Override this method to perform additional operations after the entity is partially updated.
     *
     * @param entity     Updated entity
     * @param partialDTO DTO with partial update data
     */
    void afterPartialUpdate(E entity, PartialUpdateDTO<D> partialDTO);
}
