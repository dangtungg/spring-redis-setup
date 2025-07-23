package com.example.common.service;

import com.example.common.annotation.PartialUpdateable;
import com.example.common.criteria.BaseCriteria;
import com.example.common.dto.BaseDTO;
import com.example.common.exception.BusinessProcessingException;
import com.example.common.exception.BusinessValidationException;
import com.example.common.exception.OptimisticLockException;
import com.example.common.exception.ResourceNotFoundException;
import com.example.common.repository.BaseRepository;
import com.example.common.util.JsonUtils;
import com.example.common.validator.FieldValidator;
import com.example.entity.BaseEntity;
import com.example.model.dto.PartialUpdateDTO;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.StaleObjectStateException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@SuppressWarnings("all")
public abstract class BaseServiceImpl<E extends BaseEntity, D extends BaseDTO, C extends BaseCriteria>
        implements BaseService<E, D, C> {

    // Maximum number of retry attempts for optimistic locking conflicts
    private static final int MAX_RETRY_ATTEMPTS = 3;

    // Date formats supported for conversion
    private static final List<String> DATE_FORMATS = Arrays.asList(
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd",
            "dd/MM/yyyy",
            "MM/dd/yyyy"
    );

    protected abstract BaseRepository<E> getRepository();

    protected abstract BaseQueryService<E, C> getQueryService();

    protected abstract E toEntity(D dto);

    protected abstract D toDTO(E entity);

    /**
     * Get field validators for partial updates.
     * Override this method to provide field-specific validators.
     *
     * @return A FieldValidator instance for validating field values
     */
    protected FieldValidator<Object> getFieldValidators() {
        return new FieldValidator<>();
    }

    @Override
    public D findById(Long id) {
        return getRepository()
                .findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Entity not found with id: " + id));
    }

    @Override
    @Transactional
    public D create(D dto) {
        validateBeforeCreate(dto);
        E entity = toEntity(dto);
        prepareForCreate(entity, dto);
        entity = getRepository().save(entity);
        afterCreate(entity, dto);
        return toDTO(entity);
    }

    @Override
    @Transactional
    public D update(D dto) {
        validateBeforeUpdate(dto);
        E existingEntity = getRepository().findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Entity not found with id: " + dto.getId()));

        // Convert DTO to entity but maintain the version from the database
        E entity = toEntity(dto);
        if (entity.getVersion() == null) {
            entity.setVersion(existingEntity.getVersion());
        }

        prepareForUpdate(entity, existingEntity);

        try {
            entity = getRepository().save(entity);
            afterUpdate(entity, dto);
            return toDTO(entity);
        } catch (OptimisticLockingFailureException | StaleObjectStateException e) {
            throw new OptimisticLockException(
                    "Entity has been modified by another user",
                    entity.getId(),
                    null,
                    entity.getVersion(),
                    entity.getClass()
            );
        }
    }

    @Override
    @Transactional
    public D partialUpdate(Long id, PartialUpdateDTO<D> partialDTO) {
        AtomicInteger retryCount = new AtomicInteger(0);

        while (retryCount.get() < MAX_RETRY_ATTEMPTS) {
            try {
                return doPartialUpdate(id, partialDTO);
            } catch (OptimisticLockException e) {
                if (retryCount.incrementAndGet() >= MAX_RETRY_ATTEMPTS) {
                    log.error("Failed to update entity after {} retry attempts", MAX_RETRY_ATTEMPTS);
                    throw e;
                }
                log.warn("Optimistic lock conflict detected. Retrying update attempt {}/{}", retryCount.get(), MAX_RETRY_ATTEMPTS);

                // Update version before retry
                if (partialDTO.getVersion() != null) {
                    partialDTO.setVersion(e.getCurrentVersion());
                }
            }
        }

        // Should never reach here due to the exception in the loop
        throw new BusinessProcessingException("Unexpected error in partial update retry logic");
    }

    /**
     * Performs the actual partial update operation.
     *
     * @param id         Entity ID
     * @param partialDTO Partial update data
     * @return Updated DTO
     * @throws OptimisticLockException If a version conflict occurs
     */
    private D doPartialUpdate(Long id, PartialUpdateDTO<D> partialDTO) throws OptimisticLockException {
        E currentEntity = getRepository().findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entity not found with id: " + id));

        if (partialDTO.getVersion() != null && !partialDTO.getVersion().equals(currentEntity.getVersion())) {
            throw new OptimisticLockException(
                    "Entity has been modified by another user",
                    currentEntity,
                    partialDTO.getVersion()
            );
        }

        D currentDTO = toDTO(currentEntity);

        // Validate each field before applying updates
        validateFieldsBeforePartialUpdate(partialDTO.getFields());

        // Apply updates
        applyPartialUpdate(currentDTO, partialDTO.getFields());

        // Validate the entire object after all updates are applied
        validateBeforePartialUpdate(currentDTO, partialDTO.getFields());

        E updatedEntity = toEntity(currentDTO);
        try {
            updatedEntity = getRepository().save(updatedEntity);
            afterPartialUpdate(updatedEntity, partialDTO);
            return toDTO(updatedEntity);
        } catch (OptimisticLockingFailureException | StaleObjectStateException e) {
            throw new OptimisticLockException(
                    "Entity has been modified by another user during save",
                    currentEntity,
                    partialDTO.getVersion()
            );
        }
    }

    protected void applyPartialUpdate(D targetDTO, Map<String, Object> fields) {
        fields.forEach((fieldName, value) -> {
            Field field = ReflectionUtils.findField(targetDTO.getClass(), fieldName);
            if (field == null) {
                throw new BusinessValidationException("Unknown field: " + fieldName);
            }

            if (!field.isAnnotationPresent(PartialUpdateable.class)) {
                throw new BusinessValidationException("Field '%s' does not support partial update".formatted(fieldName));
            }

            field.setAccessible(true);
            try {
                Object convertedValue = convertValueToFieldType(value, field.getType());
                field.set(targetDTO, convertedValue);
            } catch (IllegalAccessException | IllegalArgumentException e) {
                throw new BusinessProcessingException("Cannot update field '%s': %s".formatted(fieldName, e.getMessage()));
            }
        });
    }

    /**
     * Enhanced converter that handles various type conversions commonly needed in APIs.
     *
     * @param value      Value to convert
     * @param targetType Target Java type
     * @return Converted value
     */
    protected Object convertValueToFieldType(Object value, Class<?> targetType) {
        if (value == null) return null;

        // Handle primitive types and their wrappers
        if (targetType.equals(String.class)) {
            return value.toString();
        } else if (targetType.equals(Integer.class) || targetType.equals(int.class)) {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            } else if (value instanceof String) {
                return Integer.parseInt((String) value);
            }
        } else if (targetType.equals(Long.class) || targetType.equals(long.class)) {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            } else if (value instanceof String) {
                return Long.parseLong((String) value);
            }
        } else if (targetType.equals(Double.class) || targetType.equals(double.class)) {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            } else if (value instanceof String) {
                return Double.parseDouble((String) value);
            }
        } else if (targetType.equals(Float.class) || targetType.equals(float.class)) {
            if (value instanceof Number) {
                return ((Number) value).floatValue();
            } else if (value instanceof String) {
                return Float.parseFloat((String) value);
            }
        } else if (targetType.equals(Boolean.class) || targetType.equals(boolean.class)) {
            if (value instanceof Boolean) {
                return value;
            } else if (value instanceof String) {
                String strValue = ((String) value).toLowerCase();
                return strValue.equals("true") || strValue.equals("yes") || strValue.equals("1");
            } else if (value instanceof Number) {
                return ((Number) value).intValue() != 0;
            }
        } else if (targetType.equals(BigDecimal.class)) {
            if (value instanceof Number) {
                return new BigDecimal(value.toString());
            } else if (value instanceof String) {
                return new BigDecimal((String) value);
            }
        } else if (targetType.isEnum() && value instanceof String) {
            return Enum.valueOf((Class<? extends Enum>) targetType, (String) value);
        }
        // Handle date/time types
        else if (targetType.equals(Date.class)) {
            return convertToDate(value);
        } else if (targetType.equals(LocalDate.class)) {
            return convertToLocalDate(value);
        } else if (targetType.equals(LocalDateTime.class)) {
            return convertToLocalDateTime(value);
        } else if (targetType.equals(Instant.class)) {
            return convertToInstant(value);
        }

        // Default fallback
        return value;
    }

    /**
     * Convert a value to Date using multiple format attempts.
     */
    private Date convertToDate(Object value) {
        if (value instanceof Date) {
            return (Date) value;
        } else if (value instanceof String) {
            String strValue = (String) value;
            for (String format : DATE_FORMATS) {
                try {
                    return new SimpleDateFormat(format).parse(strValue);
                } catch (ParseException e) {
                    // Try next format
                }
            }
            throw new BusinessProcessingException("Cannot parse date: " + value);
        } else if (value instanceof Number) {
            return new Date(((Number) value).longValue());
        }
        throw new BusinessProcessingException("Cannot convert to Date: " + value);
    }

    /**
     * Convert a value to LocalDate.
     */
    private LocalDate convertToLocalDate(Object value) {
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        } else if (value instanceof String) {
            String strValue = (String) value;
            try {
                return LocalDate.parse(strValue);
            } catch (DateTimeParseException e) {
                try {
                    return LocalDate.parse(strValue, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                } catch (DateTimeParseException e2) {
                    try {
                        return LocalDate.parse(strValue, DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                    } catch (DateTimeParseException e3) {
                        throw new BusinessProcessingException("Cannot parse LocalDate: " + value);
                    }
                }
            }
        } else if (value instanceof Date) {
            return ((Date) value).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        throw new BusinessProcessingException("Cannot convert to LocalDate: " + value);
    }

    /**
     * Convert a value to LocalDateTime.
     */
    private LocalDateTime convertToLocalDateTime(Object value) {
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        } else if (value instanceof String) {
            String strValue = (String) value;
            try {
                return LocalDateTime.parse(strValue);
            } catch (DateTimeParseException e) {
                try {
                    return LocalDateTime.parse(strValue, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
                } catch (DateTimeParseException e2) {
                    throw new BusinessProcessingException("Cannot parse LocalDateTime: " + value);
                }
            }
        } else if (value instanceof Date) {
            return ((Date) value).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        throw new BusinessProcessingException("Cannot convert to LocalDateTime: " + value);
    }

    /**
     * Convert a value to Instant.
     */
    private Instant convertToInstant(Object value) {
        if (value instanceof Instant) {
            return (Instant) value;
        } else if (value instanceof String) {
            try {
                return Instant.parse((String) value);
            } catch (DateTimeParseException e) {
                // Try to parse as LocalDateTime first, then convert to Instant
                try {
                    LocalDateTime ldt = convertToLocalDateTime(value);
                    return ldt.atZone(ZoneId.systemDefault()).toInstant();
                } catch (Exception e2) {
                    throw new BusinessProcessingException("Cannot parse Instant: " + value);
                }
            }
        } else if (value instanceof Date) {
            return ((Date) value).toInstant();
        } else if (value instanceof Number) {
            return Instant.ofEpochMilli(((Number) value).longValue());
        }
        throw new BusinessProcessingException("Cannot convert to Instant: " + value);
    }

    @Override
    public void delete(Long id) {
        E entity = getRepository().findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entity not found with id: " + id));
        getRepository().delete(entity);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        E entity = getRepository().findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entity not found with id: " + id));
        entity.setIsActive(false);
        getRepository().save(entity);
    }

    @Override
    @Transactional
    public void activate(Long id) {
        E entity = getRepository().findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entity not found with id: " + id));
        entity.setIsActive(true);
        getRepository().save(entity);
    }

    @Override
    public List<D> findByCreatedBy(String createdBy) {
        return getRepository()
                .findByCreatedBy(createdBy)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<D> findByCriteria(C criteria) {
        log.debug("find list by criteria : {}", criteria);
        Specification<E> spec = getQueryService().createSpecification(criteria);
        return getRepository().findAll(spec).stream().map(this::toDTO).toList();
    }

    @Override
    public List<D> findByDateRange(Instant start, Instant end) {
        return getRepository()
                .findByCreatedAtBetween(start, end)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<D> findByCriteria(C criteria, Pageable pageable) {
        log.debug("find paginated list by criteria : {}", JsonUtils.toJson(criteria));
        Specification<E> spec = getQueryService().createSpecification(criteria);
        return getRepository().findAll(spec, pageable).map(this::toDTO);
    }

    @Override
    public void validateBeforeCreate(D dto) {
        // Default implementation does nothing
        // Subclasses can override to provide specific validation logic
    }

    @Override
    public void validateBeforeUpdate(D dto) {
        // Default implementation does nothing
        // Subclasses can override to provide specific validation logic
        if (dto.getId() == null) {
            throw new BusinessValidationException("ID must not be null for update");
        }
    }

    /**
     * Validates individual fields before applying any updates.
     * Uses field-specific validators to validate each field.
     *
     * @param fields Map of field names to values
     * @throws BusinessValidationException If any field validation fails
     */
    @Override
    public void validateFieldsBeforePartialUpdate(Map<String, Object> fields) {
        FieldValidator<Object> validator = getFieldValidators();
        Map<String, String> validationErrors = validator.validateFields(fields);

        if (!validationErrors.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("Field validation failed:");
            validationErrors.forEach((field, error) ->
                    errorMessage.append("\n- ").append(field).append(": ").append(error));

            throw new BusinessValidationException(errorMessage.toString());
        }
    }

    @Override
    public void validateBeforePartialUpdate(D dto, Map<String, Object> updatedFields) {
        // Default implementation does nothing
        validateBeforeUpdate(dto);
    }

    @Override
    public void prepareForCreate(E entity, D dto) {
        // Default implementation does nothing
        // Subclasses can override to provide specific preparation logic
    }

    @Override
    public void prepareForUpdate(E entity, E existingEntity) {
        // Default implementation does nothing
        // Subclasses can override to provide specific preparation logic
    }

    @Override
    public void afterCreate(E entity, D dto) {
        // Default implementation does nothing
        // Subclasses can override to provide specific post-processing logic
    }

    @Override
    public void afterUpdate(E entity, D dto) {
        // Default implementation does nothing
        // Subclasses can override to provide specific post-processing logic
    }

    @Override
    public void afterPartialUpdate(E entity, PartialUpdateDTO<D> partialDTO) {
        // Default implementation does nothing
        // Subclasses can override to provide specific post-processing logic
    }
}
