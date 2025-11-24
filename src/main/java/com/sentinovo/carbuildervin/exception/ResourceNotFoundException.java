package com.sentinovo.carbuildervin.exception;

import java.util.UUID;

public class ResourceNotFoundException extends BusinessException {
    
    public ResourceNotFoundException(String resourceType, UUID id) {
        super("RESOURCE_NOT_FOUND", String.format("%s with id %s not found", resourceType, id));
    }
    
    public ResourceNotFoundException(String resourceType, String identifier) {
        super("RESOURCE_NOT_FOUND", String.format("%s with identifier '%s' not found", resourceType, identifier));
    }
    
    public ResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", message);
    }
}