package com.carloship.craft.sdk.exception;

public class MissingDependencyException extends ModuleException {

    public MissingDependencyException(Class<?> owner, Class<?> dependency) {
        super(owner.getSimpleName() + " requires dependency " + dependency.getSimpleName());
    }
}