/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.services.internal;


import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.services.PmdContext;
import net.sourceforge.pmd.lang.services.ServiceBundle;
import net.sourceforge.pmd.properties.AbstractPropertySource;
import net.sourceforge.pmd.properties.PropertySource;

public final class PmdContextImpl implements PmdContext {

    private final PropertySource properties;
    private final Map<Language, ServiceBundle> languageServices;
    private final PmdLogger logger;

    public PmdContextImpl(String name, ClassLoader classLoader, PmdLogger logger) {
        properties = new ContextProperties(name);
        this.logger = logger;
        languageServices = new HashMap<>();
        new LanguagePluginLoader(this).load(classLoader);
    }

    public PmdContextImpl(String name, ClassLoader classLoader) {
        this(name, classLoader, new PmdLoggerImpl());
    }

    @Override
    public PropertySource getRunProperties() {
        return properties;
    }

    @Override
    public Set<Language> getLanguages() {
        return languageServices.keySet();
    }


    @Override
    public ServiceBundle getServices(Language language) {
        Objects.requireNonNull(language, "Language is null!");
        return languageServices.computeIfAbsent(language, k -> new ServiceBundleImpl());
    }

    @Override
    public PmdLogger logger() {
        return logger;
    }

    @Override
    public void close() {
        // TODO cleanup services
    }

    static class PmdLoggerImpl implements PmdLogger {

        @Override
        public void report(Level level, @Nullable String message, @Nullable Throwable err) {
            // TODO
        }
    }


    private static class ContextProperties extends AbstractPropertySource {

        private final String name;

        private ContextProperties(String name) {
            this.name = name;
        }

        @Override
        protected String getPropertySourceType() {
            return "context";
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
