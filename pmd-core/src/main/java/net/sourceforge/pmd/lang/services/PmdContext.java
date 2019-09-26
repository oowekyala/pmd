/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.services;

import java.util.Set;
import java.util.logging.Level;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.services.internal.PmdContextImpl;
import net.sourceforge.pmd.properties.PropertySource;

/**
 * Top-level context for a PMD run. This encapsulates all static state.
 */
public interface PmdContext extends AutoCloseable {

    PmdContext STATIC = new PmdContextImpl("static", PmdContext.class.getClassLoader());


    PropertySource getRunProperties();


    /** Returns the set of known language modules. */
    Set<Language> getLanguages();


    /**
     * Returns the services registered for the given language.
     * If the language is unknown, returns a fresh language module.
     */
    ServiceBundle getServices(Language language);


    PmdLogger logger();


    @Override
    void close();


    static PmdContext create(ClassLoader classLoader, PmdLogger logger) {
        return new PmdContextImpl("pmd", classLoader, logger);
    }


    interface PmdLogger {


        void report(Level level, @Nullable String message, @Nullable Throwable err);


        default void recoverable(String recovery, Throwable e) {
            report(Level.FINE, recovery, e);
        }


        default void ignored(Throwable e) {
            report(Level.FINE, "Ignored exception", e);
        }


        default void fatal(Throwable e) {
            report(Level.SEVERE, null, e);
            throw new FatalError();
        }


        default void warn(String message) {
            report(Level.WARNING, message, null);
        }


        default void info(String message) {
            report(Level.INFO, message, null);
        }


        default void debug(String message) {
            report(Level.FINE, message, null);
        }


        class FatalError extends Error {

        }

    }
}
