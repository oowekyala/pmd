/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.apex.cpd;

import java.util.Locale;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.Token;

import net.sourceforge.pmd.cpd.TokenEntry;
import net.sourceforge.pmd.cpd.Tokenizer;
import net.sourceforge.pmd.cpd.Tokens;
import net.sourceforge.pmd.lang.apex.ApexJorjeLogging;
import net.sourceforge.pmd.lang.ast.TokenMgrError;
import net.sourceforge.pmd.util.document.Chars;
import net.sourceforge.pmd.util.document.TextDocument;

import apex.jorje.parser.impl.ApexLexer;

public class ApexTokenizer implements Tokenizer {

    public ApexTokenizer() {
        ApexJorjeLogging.disableLogging();
    }

    private boolean caseSensitive;

    @Override
    public void setProperties(CpdProperties cpdProperties) {
        caseSensitive = cpdProperties.getProperty(Tokenizer.CASE_SENSITIVE);
    }

    @Override
    public void tokenize(TextDocument sourceCode, Tokens tokenEntries) {
        Chars code = sourceCode.getText();

        ANTLRStringStream ass = new ANTLRStringStream(code.toString());
        ApexLexer lexer = new ApexLexer(ass) {
            @Override
            public void emitErrorMessage(String msg) {
                throw new TokenMgrError(getLine(), getCharPositionInLine(), getSourceName(), msg, null);
            }
        };

        try {
            Token token = lexer.nextToken();

            while (token.getType() != Token.EOF) {
                if (token.getChannel() != Lexer.HIDDEN) {
                    String tokenText = token.getText();
                    if (!caseSensitive) {
                        tokenText = tokenText.toLowerCase(Locale.ROOT);
                    }
                    TokenEntry tokenEntry = new TokenEntry(tokenText, sourceCode.getPathId(),
                                                           token.getLine(),
                                                           token.getCharPositionInLine() + 1,
                                                           token.getCharPositionInLine() + tokenText.length() + 1);
                    tokenEntries.add(tokenEntry);
                }
                token = lexer.nextToken();
            }
        } finally {
            tokenEntries.add(TokenEntry.getEOF());
        }
    }
}
