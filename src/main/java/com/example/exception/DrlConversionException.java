package com.example.exception;

/**
 * DRL转换异常
 */
public class DrlConversionException extends RuntimeException {
    
    public DrlConversionException(String message) {
        super(message);
    }
    
    public DrlConversionException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public DrlConversionException(Throwable cause) {
        super(cause);
    }
} 