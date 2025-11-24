package com.sentinovo.carbuildervin.exception;

import java.util.UUID;

public class UnauthorizedException extends BusinessException {
    
    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message);
    }
    
    public UnauthorizedException(String action, String resourceType) {
        super("UNAUTHORIZED", String.format("Not authorized to %s %s", action, resourceType));
    }
    
    public UnauthorizedException(String action, String resourceType, UUID resourceId) {
        super("UNAUTHORIZED", String.format("Not authorized to %s %s with id %s", action, resourceType, resourceId));
    }
}