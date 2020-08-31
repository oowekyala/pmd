/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.cpd;

import java.util.Collections;
import java.util.List;

import net.sourceforge.pmd.cpd.Tokenizer.CpdProperties;
import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.LanguageVersionHandler;
import net.sourceforge.pmd.lang.Parser;
import net.sourceforge.pmd.lang.ParserOptions;

public class AnyLanguage implements Language {

    public static final AnyLanguage INSTANCE = new AnyLanguage();

    private static final String NAME = "Any Language";
    private static final String ID = "any";

    private final LanguageVersion anyVersion = new LanguageVersion(this, "0", new LanguageVersionHandler() {
        @Override
        public Tokenizer getCpdTokenizer(CpdProperties cpdProperties) {
            return new AnyTokenizer();
        }

        @Override
        public Parser getParser(ParserOptions parserOptions) {
            return task -> {
                throw new UnsupportedOperationException("Parse not supported");
            };
        }
    });

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getShortName() {
        return NAME;
    }

    @Override
    public String getTerseName() {
        return ID;
    }

    @Override
    public List<String> getExtensions() {
        return Collections.emptyList();
    }

    @Override
    public boolean hasExtension(String extension) {
        return true;
    }

    @Override
    public List<LanguageVersion> getVersions() {
        return Collections.singletonList(anyVersion);
    }

    @Override
    public boolean hasVersion(String version) {
        return "0".equals(version);
    }

    @Override
    public LanguageVersion getVersion(String version) {
        return hasVersion(version) ? anyVersion : null;
    }

    @Override
    public LanguageVersion getDefaultVersion() {
        return anyVersion;
    }

    @Override
    public int compareTo(Language o) {
        return this.equals(o) ? 0 : -o.compareTo(this);
    }
}
