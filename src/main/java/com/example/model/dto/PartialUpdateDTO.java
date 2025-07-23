package com.example.model.dto;

import com.example.common.dto.BaseDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class PartialUpdateDTO<D extends BaseDTO> {
    private Long id;
    private Map<String, Object> fields = new HashMap<>();

    /**
     * Để kiểm tra optimistic locking (optional)
     */
    private Long version;

    public boolean hasField(String fieldName) {
        return fields.containsKey(fieldName);
    }

    public Object getField(String fieldName) {
        return fields.get(fieldName);
    }

    public void setField(String fieldName, Object value) {
        fields.put(fieldName, value);
    }
}
