package com.carloship.craft.sdk.exception;

public class ModuleStartupException extends ModuleException {

    public ModuleStartupException(Class<?> module, Throwable cause) {
        super("Failed starting module " + module.getName(), cause);
    }
}