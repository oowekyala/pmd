/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal.tclones;

import java.util.Arrays;
import java.util.Objects;

/** A very tiny "map" implementation, for which insertion order matters. */
final class AttrMap {

    String[] keys;
    Object[] values;
    int size;

    static final AttrMap EMPTY = new AttrMap();

    private AttrMap() {
        // for the empty map
    }

    public AttrMap(String firstK, Object firstV) {
        keys = new String[] {firstK};
        values = new Object[] {firstV};
        size = 1;
    }


    void put(String key, Object value) {
        // note: if this is #EMPTY, will throw NPE

        if (keys.length == size) {
            keys = Arrays.copyOf(keys, size * 2);
            values = Arrays.copyOf(values, size * 2);
        }

        keys[size] = key;
        values[size] = value;
        size++;
    }

    // note: ORDER of insertion of keys is important

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AttrMap attrMap = (AttrMap) o;
        return size == attrMap.size &&
            Arrays.equals(keys, attrMap.keys) &&
            Arrays.equals(values, attrMap.values);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(size);
        result = 31 * result + Arrays.hashCode(keys);
        result = 31 * result + Arrays.hashCode(values);
        return result;
    }
}
