package com.example.common.exception;

import com.example.entity.BaseEntity;
import lombok.Getter;

/**
 * Exception thrown when optimistic locking conflicts occur during entity updates.
 * Contains information about the conflicting entity and current version.
 */
@Getter
public class OptimisticLockException extends BaseException {
    private final Long entityId;
    private final Long currentVersion;
    private final Long expectedVersion;
    private final Class<? extends BaseEntity> entityClass;

    public OptimisticLockException(String message, Long entityId, Long currentVersion, Long expectedVersion,
                                   Class<? extends BaseEntity> entityClass) {
        super("OPTIMISTIC_LOCK_ERROR", message);
        this.entityId = entityId;
        this.currentVersion = currentVersion;
        this.expectedVersion = expectedVersion;
        this.entityClass = entityClass;
    }

    public OptimisticLockException(String message, BaseEntity entity, Long expectedVersion) {
        this(message, entity.getId(), entity.getVersion(), expectedVersion, entity.getClass());
    }
} 