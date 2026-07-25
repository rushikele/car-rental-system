package com.carrental.exception;

// ✅ CUSTOM EXCEPTIONS - shows clean error handling in interviews

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
