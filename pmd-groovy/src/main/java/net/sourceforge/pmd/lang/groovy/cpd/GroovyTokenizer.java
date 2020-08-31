/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.groovy.cpd;

import org.codehaus.groovy.antlr.SourceInfo;
import org.codehaus.groovy.antlr.parser.GroovyLexer;

import net.sourceforge.pmd.cpd.TokenEntry;
import net.sourceforge.pmd.cpd.Tokenizer;
import net.sourceforge.pmd.cpd.Tokens;
import net.sourceforge.pmd.lang.ast.TokenMgrError;
import net.sourceforge.pmd.util.document.TextDocument;

import groovyjarjarantlr.Token;
import groovyjarjarantlr.TokenStream;
import groovyjarjarantlr.TokenStreamException;

/**
 * The Groovy Tokenizer
 */
public class GroovyTokenizer implements Tokenizer {

    @Override
    public void tokenize(TextDocument sourceCode, Tokens tokenEntries) {
        GroovyLexer lexer = new GroovyLexer(sourceCode.newReader());
        TokenStream tokenStream = lexer.plumb();

        try {
            Token token = tokenStream.nextToken();

            while (token.getType() != Token.EOF_TYPE) {
                String tokenText = token.getText();


                int lastCol;
                if (token instanceof SourceInfo) {
                    lastCol = ((SourceInfo) token).getColumnLast();
                } else {
                    // fallback
                    lastCol = token.getColumn() + tokenText.length();
                }
                TokenEntry tokenEntry = new TokenEntry(tokenText, sourceCode.getPathId(), token.getLine(), token.getColumn(), lastCol);

                tokenEntries.add(tokenEntry);
                token = tokenStream.nextToken();
            }
        } catch (TokenStreamException err) {
            // Wrap exceptions of the Groovy tokenizer in a TokenMgrError, so
            // they are correctly handled
            // when CPD is executed with the '--skipLexicalErrors' command line
            // option
            throw new TokenMgrError(lexer.getLine(), lexer.getColumn(), lexer.getFilename(), err.getMessage(), err);
        } finally {
            tokenEntries.add(TokenEntry.getEOF());
        }
    }
}
