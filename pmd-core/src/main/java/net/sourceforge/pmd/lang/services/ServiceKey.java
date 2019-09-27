/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.services;


import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.Nullable;

public final class ServiceKey<T> {

    private final Class<T> type;
    private final int min;
    private final int max;
    private final @Nullable List<T> defaultValue;


    private ServiceKey(Class<T> type, int min, int max, @Nullable List<T> defaultValue) {
        this.type = type;
        this.min = min;
        this.max = max;
        this.defaultValue = defaultValue == null ? null : Collections.unmodifiableList(new ArrayList<>(defaultValue));
    }


    public Class<T> getServiceInterface() {
        return type;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public @Nullable List<T> getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String toString() {
        return type.toString();
    }

    @Override
    public boolean equals(Object data) {
        if (this == data) {
            return true;
        }
        if (data == null || getClass() != data.getClass()) {
            return false;
        }
        ServiceKey<?> that = (ServiceKey<?>) data;
        return Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }


    public static <T> ServiceKey<T> unique(Class<T> type, T defaultValue) {
        return new ServiceKey<>(type, 1, 1, defaultValue == null ? null : Collections.singletonList(defaultValue));
    }

    public static <T> ServiceKey<T> optional(Class<T> type, T defaultValue) {
        return new ServiceKey<>(type, 0, 1, Collections.singletonList(defaultValue));
    }

    public static <T> ServiceKey<T> optionalList(Class<T> type, List<T> defaultValue) {
        return new ServiceKey<>(type, 0, Integer.MAX_VALUE, defaultValue == null ? emptyList() : defaultValue);
    }

    public static <T> ServiceKey<T> nonEmptyList(Class<T> type, List<T> defaultValue) {
        return new ServiceKey<>(type, 1, Integer.MAX_VALUE, defaultValue);
    }

}
