/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.lang.services.common;

import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.LanguageVersion;

/** A file selector for a language. Every language must declare such a service. */
public interface LanguageVersionDefaulter {

    LanguageVersion defaultVersion(Language language);


    static LanguageVersionDefaulter thisOne(Language language, LanguageVersion version) {
        if (!language.getVersions().contains(version)) {
            throw new AssertionError("Version " + version + " is not registered for language " + language);
        }
        return lang -> {
            if (lang != language) {
                throw new AssertionError("Invalid language " + lang + ", expecting " + language);
            }
            return version;
        };
    }

}
