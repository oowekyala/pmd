/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.services;


import java.util.ServiceLoader;
import java.util.Set;

import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.LanguageVersionHandler;
import net.sourceforge.pmd.lang.LanguageVersionImpl;
import net.sourceforge.pmd.lang.rule.RuleChainVisitor;
import net.sourceforge.pmd.lang.services.ServiceBundle.MutableServiceBundle;
import net.sourceforge.pmd.lang.services.common.FileLanguagePicker;

/**
 * This is the service provider that should be provided to {@link ServiceLoader}.
 * Language implementations need to expose at least one such class in {@code META-INF/services/}.
 *
 * <p>TODO what would be nice is to be able to pass properties to services,
 *     eg -Ljava:eagerTypeResolution -Ljava:disambiguateAll
 *
 * <p>Required services are
 * <ul>
 * <li>Exactly 1 {@link RuleChainVisitor} TODO, will be scrapped
 * <li>At least 1 {@link FileLanguagePicker}
 * <li>At least 1 {@link LanguageVersionImpl}. This could be defaulted when
 * all methods of {@link LanguageVersionHandler} are split into services.
 * </ul>
 */
public interface PmdLanguagePlugin {

    /**
     * Returns an integer that describes the ordering of services that
     * apply to the same language. A lower integer is applied first.
     */
    default int getPriority() {
        return 0;
    }


    /**
     * Returns the language this plugin declares. The language could
     * have been already declared, in which case you should fetch it
     * from the parameter.
     */
    Language getLanguage(Set<Language> alreadyKnown);


    /**
     * Initializes the service context.
     *
     * @param lang           Language instance, the result of {@link #getLanguage(Set)}
     * @param contextBuilder Service builder
     */
    void initialize(Language lang, MutableServiceBundle contextBuilder);

}
