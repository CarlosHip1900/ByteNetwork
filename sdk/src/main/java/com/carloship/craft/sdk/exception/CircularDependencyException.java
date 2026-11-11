package com.carloship.craft.sdk.exception;

public class CircularDependencyException extends ModuleException {

    public CircularDependencyException(String message) {
        super(message);
    }
}