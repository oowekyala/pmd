/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.cpd.internal;

import java.io.IOException;

import net.sourceforge.pmd.cpd.SourceCode;
import net.sourceforge.pmd.cpd.TokenEntry;
import net.sourceforge.pmd.cpd.Tokenizer;
import net.sourceforge.pmd.cpd.Tokens;
import net.sourceforge.pmd.cpd.token.JavaCCTokenFilter;
import net.sourceforge.pmd.cpd.token.TokenFilter;
import net.sourceforge.pmd.lang.TokenManager;
import net.sourceforge.pmd.lang.ast.FileAnalysisException;
import net.sourceforge.pmd.lang.ast.impl.javacc.CharStream;
import net.sourceforge.pmd.lang.ast.impl.javacc.JavaccToken;
import net.sourceforge.pmd.lang.ast.impl.javacc.JavaccTokenDocument.TokenDocumentBehavior;
import net.sourceforge.pmd.lang.ast.impl.javacc.MalformedSourceException;
import net.sourceforge.pmd.lang.document.CpdCompat;
import net.sourceforge.pmd.lang.document.TextDocument;

public abstract class JavaCCTokenizer implements Tokenizer {

    @SuppressWarnings("PMD.CloseResource")
    protected TokenManager<JavaccToken> getLexerForSource(SourceCode sourceCode) throws IOException, MalformedSourceException {
        TextDocument textDocument = TextDocument.create(CpdCompat.cpdCompat(sourceCode));
        return makeLexerImpl(CharStream.create(textDocument, tokenBehavior()));
    }

    protected TokenDocumentBehavior tokenBehavior() {
        return TokenDocumentBehavior.DEFAULT;
    }

    protected abstract TokenManager<JavaccToken> makeLexerImpl(CharStream sourceCode);

    protected TokenFilter<JavaccToken> getTokenFilter(TokenManager<JavaccToken> tokenManager) {
        return new JavaCCTokenFilter(tokenManager);
    }

    protected TokenEntry processToken(Tokens tokenEntries, JavaccToken currentToken, String filename) {
        return new TokenEntry(getImage(currentToken), filename, currentToken.getReportLocation());
    }

    protected String getImage(JavaccToken token) {
        return token.getImage();
    }

    @Override
    public void tokenize(SourceCode sourceCode, Tokens tokenEntries) throws IOException {
        try {
            TokenManager<JavaccToken> tokenManager = getLexerForSource(sourceCode);
            final TokenFilter<JavaccToken> tokenFilter = getTokenFilter(tokenManager);
            JavaccToken currentToken = tokenFilter.getNextToken();
            while (currentToken != null) {
                tokenEntries.add(processToken(tokenEntries, currentToken, sourceCode.getFileName()));
                currentToken = tokenFilter.getNextToken();
            }
        } catch (FileAnalysisException e) {
            throw e.setFileName(sourceCode.getFileName());
        } finally {
            tokenEntries.add(TokenEntry.getEOF());
        }
    }
}
