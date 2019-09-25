/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.services;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ServiceConfigurationError;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A one-shot loader that can load implementations of {@code T} from a
 * service provider. This loader makes it possible to implement services
 * as enum constants and as singletons.
 *
 * @param <S> Type of service to load
 */
public class LanguageServiceLoader<S> {

    private final Class<S> serviceInterface;
    private final Class<? extends S> serviceImpl;
    private final List<String> failures = new ArrayList<>();

    public LanguageServiceLoader(Class<S> serviceInterface,
                                 Class<? extends S> serviceImpl) {

        this.serviceInterface = serviceInterface;
        this.serviceImpl = serviceImpl;

        if (!serviceInterface.isAssignableFrom(serviceImpl)) {
            throw new IllegalArgumentException(serviceInterface + " is not a supertype of " + serviceImpl);
        }
    }


    /**
     * Load the services.
     *
     * @return A list of services. May be empty if services were loaded from an enum with no constants.
     *
     * @throws ServiceConfigurationError If no way was found to instantiate the service
     */
    public List<S> load() throws ServiceConfigurationError {
        // TODO consider java 9 provider methods
        if (serviceImpl.isEnum()) {
            return Collections.unmodifiableList(Arrays.asList(serviceImpl.getEnumConstants()));
        }

        List<S> singleton = tryLoadSingleton();
        if (singleton.isEmpty()) {
            S s = tryLoadFromConstructor();
            if (s != null) {
                return Collections.singletonList(s);
            } else {
                throw new ServiceConfigurationError("Couldn't load implementation of " + serviceInterface + " from " + serviceImpl
                                                        + "\nFailures:\n\t" + String.join("\n\t", failures));
            }
        } else {
            return singleton;
        }
    }

    private void logFailure(String message) {
        failures.add(message);
    }

    private List<S> tryLoadSingleton() {
        // TODO consider scala and kotlin 'object'

        Method getInstance = null;
        try {
            getInstance = serviceImpl.getMethod("getInstance");
        } catch (NoSuchMethodException ignored) {
            logFailure("No static method 'getInstance()'");
        }
        if (getInstance == null) {
            try {
                getInstance = serviceImpl.getMethod("instance");
            } catch (NoSuchMethodException ignored) {
                logFailure("No static method 'instance()'");
            }
        }

        if (getInstance != null) {
            if (Modifier.isStatic(getInstance.getModifiers())) {
                if (serviceInterface.isAssignableFrom(getInstance.getReturnType())) {

                    S singleton;
                    try {
                        getInstance.setAccessible(true); // may fail if module is not open
                        singleton = (S) getInstance.invoke(null);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        return Collections.emptyList();
                    }

                    return Collections.singletonList(singleton);
                } else {
                    logFailure("Method " + getInstance + "'s return type is not assignable to " + serviceInterface);
                }
            } else {
                logFailure("Method " + getInstance + " is not static");
            }
        }

        return Collections.emptyList();
    }

    @Nullable
    private S tryLoadFromConstructor() {
        Constructor<? extends S> constructor;
        try {
            constructor = serviceImpl.getConstructor();
        } catch (NoSuchMethodException e) {
            logFailure(serviceImpl + " does not have a noarg constructor");
            return null;
        }

        try {
            constructor.setAccessible(true); // may fail if module is not open
            return constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            logFailure(e.getMessage());
            return null;
        }
    }


}
