package com.carloship.craft.sdk.annotations;

import com.carloship.craft.sdk.module.ModulePriority;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SDKModuleInfo {

    String id();

    String name();

    ModulePriority priority() default ModulePriority.NORMAL;

    boolean createDefault() default true;

}
