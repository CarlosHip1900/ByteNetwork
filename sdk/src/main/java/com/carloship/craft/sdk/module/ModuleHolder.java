package com.carloship.craft.sdk.module;

import com.carloship.craft.sdk.annotations.SDKModuleInfo;
import com.carloship.craft.sdk.exception.CircularDependencyException;
import com.carloship.craft.sdk.exception.MissingDependencyException;
import com.carloship.craft.sdk.exception.ModuleException;
import com.carloship.craft.sdk.exception.ModuleStartupException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.*;

@Slf4j
public class ModuleHolder {

    private final Map<Class<? extends SDKModule>, SDKModule> modules = new LinkedHashMap<>();
    private final Map<Class<? extends SDKModule>, ModuleState> states = new HashMap<>();

    private final Set<Class<? extends SDKModule>> resolving = new HashSet<>();

    private final ModuleServiceRegistry services;

    public ModuleHolder(ModuleServiceRegistry services) {
        this.services = services;
    }

    public <T extends SDKModule> void registerModule(Class<T> moduleClass) {
        if (modules.containsKey(moduleClass)) {
            return;
        }

        registerRecursive(moduleClass);
    }

    @SuppressWarnings("unchecked")
    private void registerRecursive(Class<? extends SDKModule> moduleClass) {
        if (modules.containsKey(moduleClass)) return;

        if (!resolving.add(moduleClass)) {
            throw new CircularDependencyException("Circular dependency detected involving " + moduleClass.getSimpleName());
        }

        validateModule(moduleClass);

        for (Class<? extends SDKModule> dependency : getDependencies(moduleClass)) {
            registerRecursive(dependency);
        }

        SDKModule module = createModule(moduleClass);

        modules.put(moduleClass, module);
        states.put(moduleClass, ModuleState.CREATED);

        resolving.remove(moduleClass);

        SDKModuleInfo info = moduleClass.getAnnotation(SDKModuleInfo.class);

        if (info != null) {
            log.info("Registered module {} [{}]", moduleClass.getSimpleName(), info.id());
        } else {
            log.info("Registered module {}", moduleClass.getSimpleName());
        }
    }

    public void startModules() {
        List<SDKModule> started = new ArrayList<>();

        try {
            for (SDKModule module : modules.values()) {

                long start = System.nanoTime();

                states.put(module.getClass(), ModuleState.STARTING);

                module.createModule();

                long elapsed = (System.nanoTime() - start) / 1_000_000;

                states.put(module.getClass(), ModuleState.STARTED);

                started.add(module);

                log.info("Started {} in {}ms", module.getClass().getSimpleName(), elapsed);
            }
        } catch (Exception ex) {
            rollback(started);

            throw new ModuleStartupException(ex.getClass(), ex);
        }
    }

    public void stopModules() {
        List<SDKModule> list = new ArrayList<>(modules.values());

        Collections.reverse(list);

        for (SDKModule module : list) {

            try {
                states.put(module.getClass(), ModuleState.STOPPING);

                module.disableModule();

                states.put(module.getClass(), ModuleState.STOPPED);
            } catch (Exception e) {
                log.error("Failed stopping module {}", module.getClass().getName(), e);
            }
        }
    }

    private void rollback(List<SDKModule> started) {
        Collections.reverse(started);

        for (SDKModule module : started) {
            try {
                states.put(module.getClass(), ModuleState.STOPPING);

                module.disableModule();

                states.put(module.getClass(), ModuleState.STOPPED);
            } catch (Exception e) {
                log.error("Failed rollback for {}", module.getClass().getName(), e);
            }
        }
    }

    private SDKModule createModule(Class<? extends SDKModule> moduleClass) {
        try {
            Constructor<?> ctor = resolveConstructor(moduleClass);

            List<Object> args = new ArrayList<>();

            for (Class<?> param : ctor.getParameterTypes()) {

                if (SDKModule.class.isAssignableFrom(param)) {
                    SDKModule dependency = modules.get(param);

                    if (dependency == null) {
                        throw new MissingDependencyException(moduleClass, param);
                    }

                    args.add(dependency);
                    continue;
                }

                Object service = services.getAssignable(param);

                if (service != null) {
                    args.add(service);
                    continue;
                }

                throw new MissingDependencyException(moduleClass, param);
            }

            return (SDKModule) ctor.newInstance(args.toArray());

        } catch (ModuleException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ModuleException("Failed creating module " + moduleClass.getName(), ex);
        }
    }

    private Constructor<?> resolveConstructor(Class<? extends SDKModule> moduleClass) {
        Constructor<?>[] constructors = moduleClass.getConstructors();

        if (constructors.length != 1) {
            throw new ModuleException(moduleClass.getName() + " must have exactly one public constructor");
        }

        return constructors[0];
    }

    @SuppressWarnings("unchecked")
    private Set<Class<? extends SDKModule>> getDependencies(Class<? extends SDKModule> moduleClass) {
        Constructor<?> ctor = resolveConstructor(moduleClass);

        Set<Class<? extends SDKModule>> dependencies = new LinkedHashSet<>();

        for (Class<?> param : ctor.getParameterTypes()) {

            if (SDKModule.class.isAssignableFrom(param)) {
                dependencies.add((Class<? extends SDKModule>) param);
            }
        }

        return dependencies;
    }

    private void validateModule(Class<? extends SDKModule> moduleClass) {
        if (!moduleClass.isAnnotationPresent(SDKModuleInfo.class)) {
            throw new ModuleException(moduleClass.getName() + " is missing @SDKModuleInfo");
        }

        if (Modifier.isAbstract(moduleClass.getModifiers())) {
            throw new ModuleException(moduleClass.getName() + " cannot be abstract");
        }

        resolveConstructor(moduleClass);
    }

    @SuppressWarnings("unchecked")
    public <T extends SDKModule> T getModule(Class<T> moduleClass) {
        return (T) modules.get(moduleClass);
    }

    public ModuleState getState(Class<? extends SDKModule> moduleClass) {
        return states.get(moduleClass);
    }

    public boolean hasModule(Class<? extends SDKModule> moduleClass) {
        return modules.containsKey(moduleClass);
    }

    public Collection<SDKModule> getModules() {
        return Collections.unmodifiableCollection(modules.values());
    }
}