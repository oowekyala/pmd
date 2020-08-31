/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.cpd;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.ast.TokenMgrError;
import net.sourceforge.pmd.lang.scala.ScalaLanguageHandler;
import net.sourceforge.pmd.lang.scala.ScalaLanguageModule;
import net.sourceforge.pmd.util.document.TextDocument;

import scala.collection.Iterator;
import scala.meta.Dialect;
import scala.meta.inputs.Input;
import scala.meta.inputs.Position;
import scala.meta.internal.tokenizers.ScalametaTokenizer;
import scala.meta.tokenizers.TokenizeException;
import scala.meta.tokens.Token;

/**
 * Scala Tokenizer class. Uses the Scala Meta Tokenizer.
 */
public class ScalaTokenizer implements Tokenizer {
    private final Dialect dialect;

    /**
     * Create the Tokenizer using properties from the system environment.
     */
    public ScalaTokenizer(Dialect dialect) {
        this.dialect = dialect;
    }

    @Override
    public void tokenize(TextDocument sourceCode, Tokens tokenEntries) throws IOException {
        // create the input file for scala
        Input.VirtualFile vf = new Input.VirtualFile(sourceCode.getPathId(), sourceCode.getText().toString());
        ScalametaTokenizer tokenizer = new ScalametaTokenizer(vf, dialect);

        // tokenize with a filter
        try {
            scala.meta.tokens.Tokens tokens = tokenizer.tokenize();
            ScalaTokenFilter filter = new ScalaTokenFilter(tokens.iterator());

            Token token;
            while ((token = filter.getNextToken()) != null) {
                if (StringUtils.isEmpty(token.text())) {
                    continue;
                }
                Position pos = token.pos();
                TokenEntry cpdToken = new TokenEntry(token.text(),
                                                     sourceCode.getPathId(),
                                                     pos.startLine() + 1,
                                                     pos.startColumn() + 1,
                                                     pos.endColumn() + 2);
                tokenEntries.add(cpdToken);
            }
        } catch (Exception e) {
            if (e instanceof TokenizeException) { // NOPMD
                // cannot catch it as it's a checked exception and Scala sneaky throws
                TokenizeException tokE = (TokenizeException) e;
                Position pos = tokE.pos();
                throw new TokenMgrError(pos.startLine() + 1, pos.startColumn() + 1, sourceCode.getDisplayName(), "Scalameta threw", tokE);
            } else {
                throw e;
            }
        } finally {
            tokenEntries.add(TokenEntry.getEOF());
        }

    }

    /**
     * Token Filter skips un-helpful tokens to only register important tokens
     * and patterns.
     */
    private static class ScalaTokenFilter {
        Iterator<Token> tokenIter;
        Class<?>[] skippableTokens = new Class<?>[] { Token.Space.class, Token.Tab.class, Token.CR.class,
            Token.LF.class, Token.FF.class, Token.LFLF.class, Token.EOF.class };

        ScalaTokenFilter(Iterator<Token> iterator) {
            this.tokenIter = iterator;
        }

        Token getNextToken() {
            if (!tokenIter.hasNext()) {
                return null;
            }

            Token token;
            do {
                token = tokenIter.next();
            } while (token != null && skipToken(token) && tokenIter.hasNext());

            return token;
        }

        private boolean skipToken(Token token) {
            boolean skip = false;
            if (token.text() != null) {
                for (Class<?> skipTokenClazz : skippableTokens) {
                    skip |= skipTokenClazz.isInstance(token);
                }
            }
            return skip;
        }
    }
}
