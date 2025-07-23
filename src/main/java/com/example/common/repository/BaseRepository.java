package com.example.common.repository;

import com.example.entity.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.time.Instant;
import java.util.List;

@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity> extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {

    // Methods liên quan đến audit
    List<T> findByCreatedBy(String createdBy);

    List<T> findByLastModifiedBy(String lastModifiedBy);

    List<T> findByCreatedByAndIsActiveTrue(String createdBy);

    // Có thể thêm các methods liên quan đến timestamp
    List<T> findByCreatedAtBetween(Instant start, Instant end);

    // Methods kết hợp audit và timestamp
    List<T> findByCreatedByAndCreatedAtBetween(String createdBy, Instant start, Instant end);

    List<T> findByLastModifiedByAndLastModifiedAtBetween(String lastModifiedBy, Instant start, Instant end);

}
