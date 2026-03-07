package com.aihoneypot.xdetector.exception;

import com.aihoneypot.core.exception.AIHoneypotException;

/**
 * Exception thrown when data collection from X fails.
 */
public class XCollectorException extends AIHoneypotException {

    public XCollectorException(String message) {
        super(message);
    }

    public XCollectorException(String message, Throwable cause) {
        super(message, cause);
    }
}

