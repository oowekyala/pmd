/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java;

import static net.sourceforge.pmd.lang.services.common.FileLanguagePicker.ofExtensions;
import static net.sourceforge.pmd.lang.services.common.LanguageVersionDefaulter.thisOne;

import java.util.Set;

import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.services.PmdLanguagePlugin;
import net.sourceforge.pmd.lang.services.ServiceBundle;
import net.sourceforge.pmd.lang.services.common.FileLanguagePicker;
import net.sourceforge.pmd.lang.services.common.LanguageVersionDefaulter;


public class JavaLanguagePlugin implements PmdLanguagePlugin {

    @Override
    public Language getLanguage(Set<Language> alreadyKnown) {
        return JavaLanguage.getInstance();
    }

    @Override
    public void initialize(Language lang, ServiceBundle contextBuilder) {
        contextBuilder.registerService(FileLanguagePicker.class, ofExtensions("java"));
        contextBuilder.registerService(LanguageVersionDefaulter.class, thisOne(lang, JavaVersion.J13));
    }
}
