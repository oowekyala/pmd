/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.services.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.checkerframework.checker.nullness.qual.NonNull;

import net.sourceforge.pmd.lang.services.ServiceBundle;

/**
 * @author Clément Fournier
 */
public class ServiceBundleImpl implements ServiceBundle {

    private final Map<Class<?>, List<?>> map = new ConcurrentHashMap<>();


    @NonNull
    @SuppressWarnings("unchecked")
    private <T> List<T> getMutable(Class<T> serviceInterface) {
        return (List<T>) map.computeIfAbsent(serviceInterface, k -> new ArrayList<>());
    }

    @Override
    public <T> List<T> getServices(Class<T> serviceInterface) {
        @SuppressWarnings("unchecked")
        List<T> ts = (List<T>) map.get(serviceInterface);
        if (ts == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(ts);
    }

    @Override
    public <T> void registerService(Class<T> serviceInterface, T impl) {
        Objects.requireNonNull(serviceInterface, "serviceInterface");
        Objects.requireNonNull(impl, "impl");
        getMutable(serviceInterface).add(impl);
    }

    @Override
    public <T> void registerService(Class<T> serviceInterface, Collection<? extends T> impls) {
        Objects.requireNonNull(serviceInterface, "serviceInterface");
        for (T impl : impls) {
            Objects.requireNonNull(impl, "impl");
            getMutable(serviceInterface).add(impl);
        }
    }

    @Override
    public <E extends Enum<E>> void registerService(Class<? super E> serviceInterface, Class<E> impls) {
        Collections.addAll(getMutable(serviceInterface), impls.getEnumConstants());
    }
}
