/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang;

/**
 * @author Clément Fournier
 */
public interface LanguageVersion<T extends LanguageVersion<T>> extends Comparable<T> {

    Language getLanguage();


    String getVersion();


    LanguageVersionHandler getLanguageVersionHandler();


    /**
     * Get the name of this LanguageVersion. This is Language name appended with
     * the LanguageVersion version if not an empty String.
     *
     * @return The name of this LanguageVersion.
     */
    default String getName(){
        return getVersion().length() > 0 ? getLanguage().getShortName() + ' ' + getVersion()
                                         : getLanguage().getShortName();
    }

    /**
     * Get the short name of this LanguageVersion. This is Language short name
     * appended with the LanguageVersion version if not an empty String.
     *
     * @return The short name of this LanguageVersion.
     */
    default String getShortName() {
        return getVersion().length() > 0 ? getLanguage().getShortName() + ' ' + getVersion()
                                         : getLanguage().getShortName();
    }


    /**
     * Get the terse name of this LanguageVersion. This is Language terse name
     * appended with the LanguageVersion version if not an empty String.
     *
     * @return The terse name of this LanguageVersion.
     */
    default String getTerseName() {
        return getVersion().length() > 0 ? getLanguage().getTerseName() + ' ' + getVersion()
                                         : getLanguage().getTerseName();

    }


    @Override
    int compareTo(LanguageVersion o);
}
