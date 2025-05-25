/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;


import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.BAD_CHAR;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.MEMBER_REFERENCE;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.REF_COMMA;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.REF_LBRACKET;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.REF_LPAREN;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.REF_POUND;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.REF_RBRACKET;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.REF_RPAREN;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.REF_VARARGS;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.TYPE_REFERENCE;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.WHITESPACE;

import java.util.EnumSet;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocMalformed;
import net.sourceforge.pmd.lang.javadoc.ast.JdocRef.JdocClassRef;
import net.sourceforge.pmd.lang.javadoc.ast.JdocRef.JdocExecutableRef;
import net.sourceforge.pmd.lang.javadoc.ast.JdocRef.JdocFieldRef;

/**
 * Parses javadoc snippets. At the time the tokens come out those are COMMENT_DATA.
 * They're lexed in a second pass depending on parser decisions.
 */
class JdocSnippetParser extends BaseJavadocParser {

    private static final EnumSet<JdocTokenType> PARAM_CLOSERS = EnumSet.of(WHITESPACE, REF_RPAREN);

    JdocSnippetParser(JdocToken tokenToSplit) {
        super(new JavadocLexer(tokenToSplit, JavadocFlexer.SNIPPET_START));
    }

    /**
     * @return A reference, or null if there's nothing to parse.
     *     Tokens of the returned node are the newer tokens
     */
    public @Nullable JdocRef parse() {
        JdocRef ref = parseInternal();
        if (ref == null) {
            return null;
        }

        // Make the lexer consume the rest of the input, to link the next
        // token of the classref correctly to a CDATA with the rest of input.
        while (advance()) {
            // skip
        }
        return ref;
    }

    private @Nullable JdocRef parseInternal() {
        advance();

        final JdocClassRef classRef;
        if (head() == null) {
            return null; // wtf?
        } else if (tokIs(TYPE_REFERENCE)) {
            classRef = new JdocClassRef(head());
            parseArrayDims(classRef); // advances at least 1
        } else {
            classRef = new JdocClassRef(JdocToken.implicitBefore(TYPE_REFERENCE, head()));
        }

        if (tokIs(REF_POUND)) { // #

            if (advance() && tokIs(MEMBER_REFERENCE)) {
                JdocToken nametok = head();

                if (advance() && tokIs(REF_LPAREN)) {
                    // method ref
                    JdocExecutableRef methodRef = new JdocExecutableRef(classRef, nametok);
                    parseParams(methodRef);
                    methodRef.setLastToken(head());
                    return methodRef;
                } else {
                    return new JdocFieldRef(classRef, nametok);
                }
            }
        } else if (tokIs(BAD_CHAR)) {
            classRef.appendChild(new JdocMalformed(JdocTokenType.EMPTY_SET, head()));
            classRef.setLastToken(head());
        }
        return classRef;
    }

    private void parseParams(JdocExecutableRef method) {
        advance();

        while (tokIs(TYPE_REFERENCE)) {
            JdocClassRef klass = new JdocClassRef(head());
            method.appendChild(klass);
            parseArrayDims(klass); // advances at least 1

            if (tokIs(REF_COMMA)) {
                advance();
                skipWhitespace();
            } else {
                break;
            }
        }

        while (!tokIsAny(PARAM_CLOSERS) && advance()) {
            // skip it
        }
    }

    private void parseArrayDims(JdocClassRef owner) {
        // note: this leaves trailing unmatched closing brackets the token stream
        // this also allows the varargs anywhere

        int numDims = 0;
        while (advance() && tokIs(REF_LBRACKET)
            && advance() && tokIs(REF_RBRACKET)) {
            numDims++;
        }

        if (tokIs(REF_VARARGS)) {
            numDims++;
            advance();
        }

        if (numDims > 0) {
            owner.setLastToken(head().getPrevious());
            owner.setArrayDims(numDims);
        }
    }

}
