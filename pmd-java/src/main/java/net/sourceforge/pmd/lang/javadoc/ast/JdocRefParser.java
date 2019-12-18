/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;


import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.BAD_CHAR;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.MEMBER_REFERENCE;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.REF_COMMA;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.REF_LPAREN;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.REF_POUND;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.REF_RPAREN;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.TYPE_REFERENCE;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.WHITESPACE;

import java.util.EnumSet;
import java.util.regex.Pattern;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocClassRef;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocExecutableRef;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocFieldRef;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocMalformed;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocRef;

/**
 * Parses references. At the time the tokens come out those are COMMENT_DATA.
 */
class JdocRefParser extends BaseJavadocParser {

    private static final Pattern REFERENCE_FORMAT = Pattern.compile(
        "((?:[\\w$]+\\.)*[\\w$]+)?"  // type name (g1), null if absent
            + "(?:#"
            + "([\\w$]+)"            // method or field name (g2), null if absent
            + "("                    // params (g3), null if absent
            + "\\([^)]*+\\)"         // (permissive, eat up to the next opening paren)
            + ")?"
            + ")?"
            + "(\\s++(.*))?"            // label (g4), empty if absent
    );

    JdocRefParser(JdocToken tokenToSplit) {
        super(new JavadocLexer(tokenToSplit, JavadocFlexer.REF_START));
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

    @Nullable
    private JdocRef parseInternal() {
        advance();

        final JdocClassRef classRef;
        if (head() == null) {
            return null; // wtf?
        } else if (tokIs(TYPE_REFERENCE)) {
            classRef = new JdocClassRef(head());
            advance();
        } else {
            classRef = new JdocClassRef(JdocToken.implicitBefore(TYPE_REFERENCE, head()));
        }

        if (tokIs(REF_POUND)) {

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
            classRef.pushChild(new JdocMalformed(JdocTokenType.EMPTY_SET, head()));
            classRef.setLastToken(head());
        }
        return classRef;
    }


    private void parseParams(JdocExecutableRef method) {
        final EnumSet<JdocTokenType> skipped = EnumSet.of(WHITESPACE, REF_COMMA, BAD_CHAR);

        while (advance()) {
            while (tokIs(skipped) && advance()) {
                // skip
            }

            if (tokIs(TYPE_REFERENCE)) {
                method.jjtAddChild(new JdocClassRef(head()), method.jjtGetNumChildren() + 1);
            }
            advance();
            while (tokIs(skipped) && advance()) {
                ;  // skip
            }
            if (tokIs(REF_RPAREN)) {
                return;
            }
        }
    }

}
