package com.aihoneypot.core.exception;

/**
 * Base exception for all AIHoneypot errors.
 */
public class AIHoneypotException extends RuntimeException {

    public AIHoneypotException(String message) {
        super(message);
    }

    public AIHoneypotException(String message, Throwable cause) {
        super(message, cause);
    }
}

