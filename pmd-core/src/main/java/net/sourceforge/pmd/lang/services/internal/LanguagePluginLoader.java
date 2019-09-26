/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.services.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Consumer;

import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.LanguageVersionImpl;
import net.sourceforge.pmd.lang.rule.RuleChainVisitor;
import net.sourceforge.pmd.lang.services.PmdLanguagePlugin;
import net.sourceforge.pmd.lang.services.common.FileLanguagePicker;

/**
 * Replacement for {@link LanguageRegistry}.
 */
public class LanguagePluginLoader {


    public static Set<Language> masterLoad(ClassLoader classLoader) {

        Set<Language> languages = loadImpl(classLoader, e -> {/*TODO log*/});
        validate(languages);
        return Collections.unmodifiableSet(sort(languages));
    }


    private static void validate(Set<Language> languages) {

        StringBuilder errorMessage = new StringBuilder();
        AssertionError error = null;
        for (Language lang : languages) {

            validateAndDefault(errorMessage, lang);


            if (errorMessage.length() != 0) {
                AssertionError newErr = new AssertionError(errorMessage.toString().trim());
                if (error == null) {
                    error = newErr;
                } else {
                    error.addSuppressed(newErr);
                }
                errorMessage = new StringBuilder();
            }
        }

        if (error != null) {
            throw error;
        }
    }

    private static void validateAndDefault(StringBuilder errorMessage, Language lang) {
        // those are the required services
        expectExactly(errorMessage, lang, RuleChainVisitor.class, 1);
        expectAtLeast(errorMessage, lang, FileLanguagePicker.class, 1);
        expectAtLeast(errorMessage, lang, LanguageVersionImpl.class, 1);

        // todo for other stuff, add a default here (eg violation suppressors, node describer for designer, etc)
    }

    private static <T> void expectAtLeast(StringBuilder errorMessage,
                                          Language lang,
                                          Class<T> serviceInterface,
                                          int minSize) {

        List<T> services = lang.getServices(serviceInterface);
        if (services.size() < minSize) {
            errorMessage.append("Expected at least ").append(minSize).append(" ")
                        .append(serviceInterface.getName()).append(", got ")
                        .append(services.size()).append('\n');
        }
    }

    private static <T> void expectExactly(StringBuilder errorMessage,
                                          Language lang,
                                          Class<T> serviceInterface,
                                          int size) {

        List<T> services = lang.getServices(serviceInterface);
        if (services.size() == size) {
            errorMessage.append("Expected exactly ").append(size).append(" ")
                        .append(serviceInterface.getName()).append(", got ")
                        .append(services.size()).append('\n');
        }
    }


    private static Set<Language> loadImpl(ClassLoader classLoader, Consumer<Throwable> exceptionHandler) {


        // Use current class' classloader instead of the threads context classloader, see https://github.com/pmd/pmd/issues/1377
        ServiceLoader<PmdLanguagePlugin> serviceLoader = ServiceLoader.load(PmdLanguagePlugin.class, classLoader);
        List<PmdLanguagePlugin> pluginList = new ArrayList<>();
        //noinspection ForLoopReplaceableByForEach
        for (Iterator<PmdLanguagePlugin> iterator = serviceLoader.iterator(); iterator.hasNext(); ) {
            try {
                PmdLanguagePlugin plugin = iterator.next();
                pluginList.add(plugin);
            } catch (UnsupportedClassVersionError e) {
                // Some languages require java8 and are therefore only available
                // if java8 or later is used as runtime.
                exceptionHandler.accept(e);
            }
        }

        pluginList.sort(Comparator.comparingInt(PmdLanguagePlugin::getPriority));

        Set<Language> seen = new LinkedHashSet<>();

        for (PmdLanguagePlugin plugin : pluginList) {
            try {
                Language lang = plugin.getLanguage(seen);
                plugin.initialize(lang, lang.getServiceBundle());
            } catch (Throwable e) {
                exceptionHandler.accept(e);
            }
        }

        return seen;
    }

    private static Set<Language> sort(Set<Language> langs) {
        ArrayList<Language> languages = new ArrayList<>(langs);
        languages.sort(Comparator.comparing(Language::getName));
        return new LinkedHashSet<>(langs);
    }


}
