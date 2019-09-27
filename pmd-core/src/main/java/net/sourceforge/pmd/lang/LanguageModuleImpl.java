/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang;

import java.util.Collections;
import java.util.List;

import net.sourceforge.pmd.lang.services.PmdContext;

public class LanguageModuleImpl implements Language {

    protected String name;
    protected String terseName;

    public LanguageModuleImpl(String name,
                              String terseName) {
        this.name = name;
        this.terseName = terseName;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getShortName() {
        return name;
    }

    @Override
    public String getTerseName() {
        return terseName;
    }

    @Override
    public Class<?> getRuleChainVisitorClass() {
        return PmdContext.STATIC.getServices(this).getRulechainVisitor().getClass();
    }

    @Override
    public List<String> getExtensions() {
        return Collections.emptyList();
    }

    @Override
    public boolean hasExtension(String extension) {
        return false;
    }

    @Override
    public List<LanguageVersion> getVersions() {
        return PmdContext.STATIC.getServices(this).getVersions();
    }

    @Override
    public LanguageVersion getDefaultVersion() {
        return PmdContext.STATIC.getServices(this).getDefaultVersion();
    }

    @Override
    public int compareTo(Language o) {
        return getName().compareTo(o.getName());
    }
}
