package com.example.common.controller;

import com.example.common.criteria.BaseCriteria;
import com.example.common.dto.BaseDTO;
import com.example.common.exception.OperationNotSupportException;
import com.example.common.response.BaseResponse;
import com.example.common.response.PaginatedResponse;
import com.example.common.service.BaseService;
import com.example.common.util.JsonUtils;
import com.example.entity.BaseEntity;
import com.example.model.dto.PartialUpdateDTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@Slf4j
public abstract class BaseController<E extends BaseEntity, D extends BaseDTO, C extends BaseCriteria> {

    protected abstract BaseService<E, D, C> getService();

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<D>> getById(@PathVariable Long id) {
        D dto = getService().findById(id);
        return ResponseEntity.ok(BaseResponse.success(dto));
    }

    @GetMapping("/search")
    public ResponseEntity<PaginatedResponse<D>> search(C criteria, Pageable pageable) {
        Page<D> page = getService().findByCriteria(criteria, pageable);
        return ResponseEntity.ok(PaginatedResponse.of(page));
    }

    @GetMapping("/search/by-creator")
    public ResponseEntity<BaseResponse<List<D>>> findByCreator(@RequestParam String createdBy) {
        List<D> lstDto = getService().findByCreatedBy(createdBy);
        return ResponseEntity.ok(BaseResponse.success(lstDto));
    }

    @GetMapping("/search/by-date-range")
    public ResponseEntity<BaseResponse<List<D>>> findByDateRange(@RequestParam Instant start, @RequestParam Instant end) {
        List<D> lstDto = getService().findByDateRange(start, end);
        return ResponseEntity.ok(BaseResponse.success(lstDto));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<D>>> getAll(C criteria) {
        throw new OperationNotSupportException("This method is not implemented. Please override it in the subclass if needed.");
    }

    @PostMapping
    public ResponseEntity<BaseResponse<D>> create(@Valid @RequestBody D dto) {
        log.debug("[CREATE] Received request to create entity: {}", JsonUtils.toJson(dto));
        D created = getService().create(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.success(created, "Entity created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<D>> update(
            @PathVariable Long id,
            @Valid @RequestBody D dto) {
        log.debug("[UPDATE] Received request to update entity with ID {}: {}", id, JsonUtils.toJson(dto));
        dto.setId(id);
        D updated = getService().update(dto);
        return ResponseEntity.ok(BaseResponse.success(updated, "Entity updated successfully"));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BaseResponse<D>> partialUpdate(
            @PathVariable Long id,
            @Valid @RequestBody PartialUpdateDTO<D> partialDTO) {
        log.debug("[PARTIAL UPDATE] Received request to partially update entity with ID {}: {}", id, JsonUtils.toJson(partialDTO));
        partialDTO.setId(id);
        D updated = getService().partialUpdate(id, partialDTO);
        return ResponseEntity.ok(BaseResponse.success(updated, "Entity updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.debug("[DELETE] Received request to delete entity with ID {}", id);
        getService().delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        log.debug("[DEACTIVATE] Received request to deactivate entity with ID {}", id);
        getService().deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long id) {
        log.debug("[ACTIVATE] Received request to activate entity with ID {}", id);
        getService().activate(id);
        return ResponseEntity.noContent().build();
    }

}
