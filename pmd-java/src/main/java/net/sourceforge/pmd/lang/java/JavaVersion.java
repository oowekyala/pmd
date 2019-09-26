/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java;

import java.io.Reader;
import java.io.StringReader;

import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.LanguageVersionHandler;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;

/**
 * Language versions of the java language.
 */
public enum JavaVersion implements LanguageVersion<JavaVersion> {
    J1_3(3),
    J1_4(4),
    J1_5(5),
    J1_6(6),
    J1_7(7),
    J8(8),
    J9(9),
    J10(10),
    J11(11),
    J12(12),
    J12__PREVIEW(12, true),
    J13(13),
    J13__PREVIEW(13, true);

    private final int jdkVersion;
    private final boolean previewEnabled;
    private final String version;

    JavaVersion(int jdkVersion, boolean previewEnabled) {

        this.jdkVersion = jdkVersion;
        this.previewEnabled = previewEnabled;
        version = name().replaceFirst("J", "")
                        .replaceFirst("__", "-")
                        .replace('_', '.')
                        .toLowerCase();
    }

    JavaVersion(int jdkVersion) {
        this(jdkVersion, false);
    }

    @Override
    public Language getLanguage() {
        return JavaLanguage.getInstance();
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public LanguageVersionHandler getLanguageVersionHandler() {
        return new JavaLanguageHandler(jdkVersion, previewEnabled);
    }


    public ASTCompilationUnit parse(String code) {
        return parse(":string:", new StringReader(code));
    }

    public ASTCompilationUnit parse(Reader reader) {
        return parse(reader.toString(), reader);
    }

    public ASTCompilationUnit parse(String filename, Reader reader) {
        LanguageVersionHandler lvh = getLanguageVersionHandler();
        return (ASTCompilationUnit) lvh.getParser(lvh.getDefaultParserOptions()).parse(filename, reader);
    }


    @Override
    public int compareTo(LanguageVersion o) {
        return o instanceof JavaVersion ? super.compareTo((JavaVersion) o) : 0;
    }
}
