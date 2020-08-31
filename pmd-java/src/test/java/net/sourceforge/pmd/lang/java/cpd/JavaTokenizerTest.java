/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.cpd;

import org.junit.Test;

import net.sourceforge.pmd.cpd.Tokenizer;
import net.sourceforge.pmd.cpd.Tokenizer.CpdProperties;
import net.sourceforge.pmd.cpd.test.CpdTextComparisonTest;
import net.sourceforge.pmd.lang.java.JavaLanguageModule;

public class JavaTokenizerTest extends CpdTextComparisonTest {

    public JavaTokenizerTest() {
        super(JavaLanguageModule.TERSE_NAME, ".java");
    }

    @Test
    public void testCommentsIgnored() {
        doTest("simpleClassWithComments");
    }

    @Test
    public void testDiscardedElements() {
        doTest("discardedElements", "_ignore_annots", ignoreAnnotations());
    }

    @Test
    public void testDiscardedElementsExceptAnnots() {
        doTest("discardedElements", "_no_ignore_annots");
    }

    @Test
    public void testIgnoreBetweenSpecialComments() {
        doTest("specialComments");
    }

    @Test
    public void testIgnoreBetweenSpecialAnnotation() {
        doTest("ignoreSpecialAnnotations");
    }

    @Test
    public void testIgnoreBetweenSpecialAnnotationAndIgnoreAnnotations() {
        doTest("ignoreSpecialAnnotations", "_ignore_annots", ignoreAnnotations());
    }

    @Test
    public void testIgnoreIdentifiersDontAffectConstructors() {
        doTest("ignoreIdentsPreservesCtor", "", ignoreIdents());
    }

    @Test
    public void testIgnoreIdentifiersHandlesEnums() {
        doTest("ignoreIdentsPreservesEnum", "", ignoreIdents());
    }

    @Test
    public void testIgnoreIdentifiersWithClassKeyword() {
        doTest("ignoreIdentsPreservesClassLiteral", "", ignoreIdents());
    }

    @Test
    public void testIgnoreLiterals() {
        doTest("ignoreLiterals", "", ignoreLiterals());
    }

    @Test
    public void testNoIgnoreLiterals() {
        doTest("ignoreLiterals", "_noignore");
    }

    @Test
    public void testTabWidth() {
        doTest("tabWidth");
    }


    private static CpdProperties ignoreAnnotations() {
        return properties(true, false, false);
    }

    private static CpdProperties ignoreIdents() {
        return properties(false, false, true);
    }

    private static CpdProperties ignoreLiterals() {
        return properties(false, true, false);
    }


    @Override
    public CpdProperties defaultProperties() {
        return properties(false, false, false);
    }

    private static CpdProperties properties(boolean ignoreAnnotations,
                                            boolean ignoreLiterals,
                                            boolean ignoreIdents) {
        CpdProperties properties = new CpdProperties();
        properties.setProperty(Tokenizer.IGNORE_ANNOTATIONS, ignoreAnnotations);
        properties.setProperty(Tokenizer.IGNORE_IDENTIFIERS, ignoreIdents);
        properties.setProperty(Tokenizer.IGNORE_LITERALS, ignoreLiterals);
        return properties;
    }


}
