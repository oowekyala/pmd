/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.services;

import java.util.Collection;
import java.util.List;

/** Write access, to build a service registrar. */
public interface ServiceBundle {

    <T> void registerService(Class<T> serviceInterface, T impl);


    <T> void registerService(Class<T> serviceInterface, Collection<? extends T> impls);


    <E extends Enum<E>> void registerService(Class<? super E> serviceInterface, Class<E> impls);


    /**
     * Returns a list of service instances for the given service interface.
     *
     * @param serviceInterface Interface of the service
     * @param <T>              Type of service
     *
     * @return A list of services
     *
     * @throws IllegalArgumentException If the key was never registered
     * @throws IllegalArgumentException If the service interface was never registered for the given key
     */
    <T> List<T> getServices(Class<T> serviceInterface);

}
