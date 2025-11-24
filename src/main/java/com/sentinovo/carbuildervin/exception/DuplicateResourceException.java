package com.sentinovo.carbuildervin.exception;

public class DuplicateResourceException extends BusinessException {
    
    public DuplicateResourceException(String resourceType, String field, String value) {
        super("DUPLICATE_RESOURCE", String.format("%s with %s '%s' already exists", resourceType, field, value));
    }
    
    public DuplicateResourceException(String message) {
        super("DUPLICATE_RESOURCE", message);
    }
}