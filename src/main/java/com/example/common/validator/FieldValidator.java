package com.example.common.validator;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Utility class for field-specific validation.
 * Allows for registering and running validators for specific fields.
 */
public class FieldValidator<T> {
    private final Map<String, Predicate<T>> validators = new HashMap<>();
    private final Map<String, String> errorMessages = new HashMap<>();

    /**
     * Register a validator for a specific field.
     *
     * @param fieldName    Name of the field to validate
     * @param validator    Predicate that returns true if validation passes
     * @param errorMessage Error message to use if validation fails
     * @return This validator instance for chaining
     */
    public FieldValidator<T> forField(String fieldName, Predicate<T> validator, String errorMessage) {
        validators.put(fieldName, validator);
        errorMessages.put(fieldName, errorMessage);
        return this;
    }

    /**
     * Validate a specific field value.
     *
     * @param fieldName Name of the field to validate
     * @param value     Value to validate
     * @return True if validation passes, false if it fails
     */
    public boolean validateField(String fieldName, T value) {
        if (!validators.containsKey(fieldName)) {
            return true; // No validator for this field
        }
        return validators.get(fieldName).test(value);
    }

    /**
     * Get the error message for a field.
     *
     * @param fieldName Name of the field
     * @return Error message or null if no validator exists for the field
     */
    public String getErrorMessage(String fieldName) {
        return errorMessages.get(fieldName);
    }

    /**
     * Validate multiple fields with their values.
     *
     * @param fieldValues Map of field names to values
     * @return Map of failed field names to error messages, empty if all validations pass
     */
    public Map<String, String> validateFields(Map<String, T> fieldValues) {
        Map<String, String> errors = new HashMap<>();

        for (Map.Entry<String, T> entry : fieldValues.entrySet()) {
            String fieldName = entry.getKey();
            T value = entry.getValue();

            if (validators.containsKey(fieldName) && !validators.get(fieldName).test(value)) {
                errors.put(fieldName, errorMessages.get(fieldName));
            }
        }

        return errors;
    }
} 