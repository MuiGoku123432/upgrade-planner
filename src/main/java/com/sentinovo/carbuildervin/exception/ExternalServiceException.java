package com.sentinovo.carbuildervin.exception;

public class ExternalServiceException extends BusinessException {
    
    private final String serviceName;
    private final int statusCode;
    
    public ExternalServiceException(String serviceName, String message) {
        super("EXTERNAL_SERVICE_ERROR", String.format("Error calling %s: %s", serviceName, message));
        this.serviceName = serviceName;
        this.statusCode = -1;
    }
    
    public ExternalServiceException(String serviceName, int statusCode, String message) {
        super("EXTERNAL_SERVICE_ERROR", String.format("Error calling %s (status %d): %s", serviceName, statusCode, message));
        this.serviceName = serviceName;
        this.statusCode = statusCode;
    }
    
    public ExternalServiceException(String serviceName, String message, Throwable cause) {
        super("EXTERNAL_SERVICE_ERROR", String.format("Error calling %s: %s", serviceName, message), cause);
        this.serviceName = serviceName;
        this.statusCode = -1;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public boolean hasStatusCode() {
        return statusCode > 0;
    }
}