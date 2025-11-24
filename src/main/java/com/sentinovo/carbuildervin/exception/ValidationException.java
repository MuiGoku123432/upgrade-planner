package com.sentinovo.carbuildervin.exception;

import java.util.Map;

public class ValidationException extends BusinessException {
    
    private final Map<String, String> fieldErrors;
    
    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
        this.fieldErrors = null;
    }
    
    public ValidationException(String message, Map<String, String> fieldErrors) {
        super("VALIDATION_ERROR", message);
        this.fieldErrors = fieldErrors;
    }
    
    public ValidationException(String field, String error) {
        super("VALIDATION_ERROR", String.format("Validation error for field '%s': %s", field, error));
        this.fieldErrors = Map.of(field, error);
    }
    
    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
    
    public boolean hasFieldErrors() {
        return fieldErrors != null && !fieldErrors.isEmpty();
    }
}