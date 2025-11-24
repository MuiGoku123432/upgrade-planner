package com.sentinovo.carbuildervin.exception;

public class InvalidStateException extends BusinessException {
    
    public InvalidStateException(String message) {
        super("INVALID_STATE", message);
    }
    
    public InvalidStateException(String entity, String currentState, String attemptedAction) {
        super("INVALID_STATE", String.format("Cannot %s %s in state '%s'", attemptedAction, entity, currentState));
    }
    
    public InvalidStateException(String entity, String field, Object currentValue, Object expectedValue) {
        super("INVALID_STATE", String.format("%s %s is '%s', expected '%s'", entity, field, currentValue, expectedValue));
    }
}