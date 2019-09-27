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
import net.sourceforge.pmd.lang.services.ServiceKey;

/**
 * @author Clément Fournier
 */
public class ServiceBundleImpl implements ServiceBundle {

    private final Map<ServiceKey<?>, List<?>> map = new ConcurrentHashMap<>();

    @NonNull
    @SuppressWarnings("unchecked")
    private <T> List<T> getMutable(ServiceKey<T> serviceKeyInterface) {
        return (List<T>) map.computeIfAbsent(serviceKeyInterface, k -> new ArrayList<>());
    }


    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getServices(ServiceKey<? super T> serviceKeyInterface) {
        List<T> ts = (List<T>) map.get(serviceKeyInterface);
        if (ts == null) {
            return (List<T>) serviceKeyInterface.getDefaultValue();
        }
        return Collections.unmodifiableList(ts);
    }


    class Mutable implements MutableServiceBundle {

        @Override
        public <T> List<T> getServices(ServiceKey<? super T> serviceKeyInterface) {
            return ServiceBundleImpl.this.getServices(serviceKeyInterface);
        }


        @Override
        public <T> void register(ServiceKey<T> serviceKey, T impl) {
            Objects.requireNonNull(serviceKey, "serviceInterface");
            Objects.requireNonNull(impl, "impl");
            getMutable(serviceKey).add(impl);
        }

        @Override
        public <T> void registerMore(ServiceKey<T> serviceKey, Collection<? extends T> impls) {
            Objects.requireNonNull(serviceKey, "serviceInterface");
            for (T impl : impls) {
                Objects.requireNonNull(impl, "impl");
                getMutable(serviceKey).add(impl);
            }
        }

        @Override
        public <E extends Enum<E>> void registerEnum(ServiceKey<? super E> serviceKey, Class<E> impls) {
            Collections.addAll(getMutable(serviceKey), impls.getEnumConstants());
        }
    }
}
