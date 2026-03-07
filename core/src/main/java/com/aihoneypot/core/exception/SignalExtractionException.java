package com.aihoneypot.core.exception;

/**
 * Exception thrown when signal extraction fails.
 */
public class SignalExtractionException extends AIHoneypotException {

    public SignalExtractionException(String message) {
        super(message);
    }

    public SignalExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}

