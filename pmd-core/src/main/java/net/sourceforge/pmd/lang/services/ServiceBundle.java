/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.services;

import java.util.Collection;
import java.util.List;

/** Write access, to build a service registrar. */
public interface ServiceBundle {


    /**
     * Returns a list of service instances for the given service interface.
     *
     * @param serviceKeyInterface Interface of the service
     * @param <T>              Type of service
     *
     * @return A list of services
     *
     * @throws IllegalArgumentException If the key was never registered
     * @throws IllegalArgumentException If the service interface was never registered for the given key
     */
    <T> List<T> getServices(ServiceKey<? super T> serviceKeyInterface);


    default <T> T getSingleService(ServiceKey<? super T> serviceKeyInterface) {
        List<T> services = getServices(serviceKeyInterface);
        if (services.size() != 1) {
            if (services.size() > 1) {
                throw new IllegalStateException(
                    serviceKeyInterface + " is registered more than once for language " + this);
            } else {
                throw new IllegalStateException(serviceKeyInterface + " is not registered for language " + this);
            }
        }
        return services.get(0);
    }


    interface MutableServiceBundle extends ServiceBundle {

        <T> void register(ServiceKey<T> serviceKey, T impl);


        <T> void registerMore(ServiceKey<T> serviceKey, Collection<? extends T> impls);


        <E extends Enum<E>> void registerEnum(ServiceKey<? super E> serviceKey, Class<E> impls);

    }

}
