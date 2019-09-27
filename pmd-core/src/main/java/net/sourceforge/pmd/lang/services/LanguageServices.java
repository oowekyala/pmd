/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.services;


import java.util.List;

import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.ast.AstProcessingStage;
import net.sourceforge.pmd.lang.rule.RuleChainVisitor;
import net.sourceforge.pmd.lang.services.common.FileLanguagePicker;
import net.sourceforge.pmd.lang.services.common.LanguageVersionDefaulter;

/**
 * Contextualized Language instance. This can be obtained from {@link PmdContext#getServices(Language)}.
 */
public interface LanguageServices {

    ServiceKey<FileLanguagePicker> FILE_LANGUAGE_PICKER = ServiceKey.nonEmptyList(FileLanguagePicker.class, null);
    ServiceKey<LanguageVersion> LANGUAGE_VERSIONS = ServiceKey.nonEmptyList(LanguageVersion.class, null);
    ServiceKey<LanguageVersionDefaulter> DEFAULT_LANGUAGE_VERSION = ServiceKey.unique(LanguageVersionDefaulter.class, null);
    ServiceKey<RuleChainVisitor> RULECHAIN_VISITOR = ServiceKey.unique(RuleChainVisitor.class, null);
    ServiceKey<AstProcessingStage> PROCESSING_STAGES = ServiceKey.optionalList(AstProcessingStage.class, null);


    Language getLanguage();


    default FileLanguagePicker getFilePicker() {
        return FileLanguagePicker.merged(getBundle().getServices(FILE_LANGUAGE_PICKER));
    }


    default LanguageVersion getDefaultVersion() {
        return getBundle().getSingleService(DEFAULT_LANGUAGE_VERSION).defaultVersion(getLanguage());
    }


    default List<LanguageVersion> getVersions() {
        List<LanguageVersion> versions = getBundle().getServices(LANGUAGE_VERSIONS);
        versions.sort(LanguageVersion::compareTo); // naturalOrder is unchecked
        return versions;
    }


    default RuleChainVisitor getRulechainVisitor() {
        return getBundle().getSingleService(RULECHAIN_VISITOR);
    }


    default List<AstProcessingStage> getProcessingStages() {
        List<AstProcessingStage> versions = getBundle().getServices(PROCESSING_STAGES);
        versions.sort(AstProcessingStage::compareTo); // naturalOrder is unchecked
        return versions;
    }


    ServiceBundle getBundle();


}
