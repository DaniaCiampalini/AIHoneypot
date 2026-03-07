package com.aihoneypot.core.exception;

/**
 * Exception thrown when classification fails.
 */
public class ClassificationException extends AIHoneypotException {

    public ClassificationException(String message) {
        super(message);
    }

    public ClassificationException(String message, Throwable cause) {
        super(message, cause);
    }
}

