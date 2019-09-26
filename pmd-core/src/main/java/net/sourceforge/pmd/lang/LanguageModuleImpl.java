/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.sourceforge.pmd.lang.rule.RuleChainVisitor;
import net.sourceforge.pmd.lang.services.ServiceBundle;
import net.sourceforge.pmd.lang.services.internal.ServiceBundleImpl;

public class LanguageModuleImpl implements Language {

    private final ServiceBundle services = new ServiceBundleImpl();

    protected String name;
    protected String terseName;

    public LanguageModuleImpl(String name,
                              String terseName) {
        this.name = name;
        this.terseName = terseName;
    }

    @Override
    public ServiceBundle getServiceBundle() {
        return services;
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
        return getSingleService(RuleChainVisitor.class).getClass();
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
        List<LanguageVersion> versions = getServices(LanguageVersionImpl.class);
        versions.sort(Comparator.naturalOrder());
        return versions;
    }

    @Override
    public int compareTo(Language o) {
        return getName().compareTo(o.getName());
    }
}
