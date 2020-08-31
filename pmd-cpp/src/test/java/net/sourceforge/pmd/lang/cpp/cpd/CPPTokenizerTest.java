/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.cpp.cpd;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.sourceforge.pmd.cpd.Tokenizer;
import net.sourceforge.pmd.cpd.Tokenizer.CpdProperties;
import net.sourceforge.pmd.cpd.Tokens;
import net.sourceforge.pmd.cpd.test.CpdTextComparisonTest;

public class CPPTokenizerTest extends CpdTextComparisonTest {

    public CPPTokenizerTest() {
        super("cpp", ".cpp");
    }

    @Override
    protected String getResourcePrefix() {
        return "../lang/cpp/cpd/testdata";
    }

    @Override
    public CpdProperties defaultProperties() {
        return dontSkipBlocks();
    }

    @Test
    public void testUTFwithBOM() {
        Tokenizer tokenizer = new CPPTokenizer().withProperties(dontSkipBlocks());
        Tokens tokens = tokenize(tokenizer, "\ufeffint start()\n{ int ret = 1;\nreturn ret;\n}\n");
        assertEquals(15, tokens.size());
    }

    @Test
    public void testContinuation() {
        doTest("continuation");
    }

    @Test
    public void testContinuationInIdent() {
        doTest("continuation_intra_token");
    }

    @Test
    public void testContinuationBetweenTokens() {
        doTest("continuation_inter_token");
    }

    @Test
    public void testUnicodeStringSupport() {
        doTest("unicodeStrings");
    }

    @Test
    public void testIgnoreBetweenSpecialComments() {
        doTest("specialComments");
    }

    @Test
    public void testMultiLineMacros() {
        doTest("multilineMacros");
    }

    @Test
    public void testIdentifierValidChars() {
        doTest("identifierChars");
    }

    @Test
    public void testWrongUnicodeInIdentifier() {
        expectTokenMgrError(" void main() { int ⚜ = __; }");
    }

    @Test
    public void testTokenizerWithSkipBlocks() {
        doTest("simpleSkipBlocks", "_skipDefault", skipBlocks());
    }

    @Test
    public void testTokenizerWithSkipBlocksPattern() {
        doTest("simpleSkipBlocks", "_skipDebug", skipBlocks("#if debug|#endif"));
    }

    @Test
    public void testTokenizerWithoutSkipBlocks() {
        doTest("simpleSkipBlocks", "_noSkip", dontSkipBlocks());
    }

    @Test
    public void testAsm() {
        // ASM code containing the '@' character
        doTest("asm", "", dontSkipBlocks());
    }

    @Test
    public void testPreprocessingDirectives() {
        doTest("preprocessorDirectives");
    }

    @Test
    public void testLiterals() {
        doTest("literals");
    }

    @Test
    public void testLexicalErrorFilename() {
        expectTokenMgrError(sourceText("issue-1559"), dontSkipBlocks());
    }


    @Test
    public void testRawStringLiterals() {
        doTest("issue-1784");
    }

    @Test
    public void testTabWidth() {
        doTest("tabWidth");
    }


    private static CpdProperties skipBlocks(String skipPattern) {
        return properties(skipPattern);
    }

    private static CpdProperties skipBlocks() {
        return skipBlocks(Tokenizer.DEFAULT_SKIP_BLOCKS_PATTERN);
    }

    private static CpdProperties dontSkipBlocks() {
        return properties(Tokenizer.NO_SKIP_BLOCKS);
    }

    private static CpdProperties properties(String skipPattern) {
        return new CpdProperties()
            .withProperty(Tokenizer.SKIP_PROC_DIRECTIVES, skipPattern);
    }
}
