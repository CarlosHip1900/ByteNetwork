package com.carloship.craft.sdk.exception;

public class MongoDatabaseException extends RuntimeException {

    public MongoDatabaseException(String message) {
        super(message);
    }

    public MongoDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
