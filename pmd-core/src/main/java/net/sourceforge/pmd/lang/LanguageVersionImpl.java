/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang;

/**
 * Created by christoferdutz on 21.09.14.
 */
public class LanguageVersionImpl implements LanguageVersion {

    private final Language language;
    private final String version;
    private final LanguageVersionHandler languageVersionHandler;

    public LanguageVersionImpl(Language language, String version, LanguageVersionHandler languageVersionHandler) {
        this.language = language;
        this.version = version;
        this.languageVersionHandler = languageVersionHandler;
    }

    @Override
    public Language getLanguage() {
        return language;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public LanguageVersionHandler getLanguageVersionHandler() {
        return languageVersionHandler;
    }

    @Override
    public String getName() {
        return version.length() > 0 ? language.getName() + ' ' + version : language.getName();
    }

    @Override
    public String getShortName() {
        return version.length() > 0 ? language.getShortName() + ' ' + version : language.getShortName();
    }

    @Override
    public String getTerseName() {
        return version.length() > 0 ? language.getTerseName() + ' ' + version : language.getTerseName();
    }

    @Override
    public int compareTo(LanguageVersion o) {
        if (o == null) {
            return 1;
        }

        int comp = getName().compareTo(o.getName());
        if (comp != 0) {
            return comp;
        }

        String[] vals1 = getName().split("\\.");
        String[] vals2 = o.getName().split("\\.");
        int i = 0;
        while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
            i++;
        }
        if (i < vals1.length && i < vals2.length) {
            int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
            return Integer.signum(diff);
        } else {
            return Integer.signum(vals1.length - vals2.length);
        }
    }

    @Override
    public String toString() {
        return language.toString() + "+version:" + version;
    }
}
