/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.WHITESPACE;

import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author Clément Fournier
 */
public class BaseJavadocParser {

    protected TokenCursor<JdocToken> tokens;

    public BaseJavadocParser(JavadocLexer lexer) {this.tokens = new TokenCursor<>(lexer);}

    protected boolean skipWhitespace() {
        while (head().getKind() == WHITESPACE && advance()) {
            // advance
        }
        return !tokens.isEoi();
    }

    protected boolean nextNonWs() {
        return advance() && skipWhitespace();
    }

    protected boolean tokIs(JdocTokenType ttype) {
        return tokens.head() != null && tokens.head().getKind() == ttype;
    }

    protected boolean tokIs(EnumSet<JdocTokenType> ttype) {
        return tokens.head() != null && ttype.contains(tokens.head().getKind());
    }


    /** Returns the current token. */
    protected JdocToken head() {
        return tokens.head();
    }

    /** Move the cursor back [n] tokens. */
    private void backup(int n) {
        tokens.backup(n);
    }

    /**
     * Returns false if end of input is reached (in which case tok
     * remains the last non-null token).
     */
    protected boolean advance() {
        return tokens.advance();
    }

    /**
     * Consumes token until [stopCondition] is true. All tokens matching
     * the [filter] are fed to the [action]. This method starts by testing
     * the current token.
     */
    protected void consumeUntil(Predicate<JdocToken> stopCondition, Predicate<JdocToken> filter, Consumer<JdocToken> action) {
        tokens.consumeUntil(stopCondition, filter, action);
    }
}
