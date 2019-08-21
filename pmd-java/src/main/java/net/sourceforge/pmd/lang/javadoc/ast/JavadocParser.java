/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;


import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.COMMENT_END;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.COMMENT_START;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.TAG_NAME;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.function.Function;

import org.checkerframework.checker.nullness.qual.NonNull;

import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdCommentData;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdInlineTag;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdMalformed;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdWhitespace;

public class JavadocParser {

    private final JavadocLexerAdapter lexer;

    private final Deque<AbstractJavadocNode> nodes = new ArrayDeque<>();
    private final Deque<JavadocToken> tokens = new ArrayDeque<>();
    private final Deque<Integer> marks = new ArrayDeque<>();

    private JavadocToken tok;
    private int curMark;

    public JavadocParser(String fileText, int startOffset, int maxOffset) {
        lexer = new JavadocLexerAdapter(fileText, startOffset, maxOffset);
    }

    public JavadocNode.JdComment parse() {
        JavadocNode.JdComment comment = new JavadocNode.JdComment();
        JavadocToken tok = lexer.getNextToken();
        if (tok == null) {
            return null;
        }
        @NonNull JavadocToken prev = tok;
        if (tok.getKind() == COMMENT_START) {
            comment.jjtSetFirstToken(tok);

            while ((tok = lexer.getNextToken()) != null) {
                prev = tok;

                switch (tok.getKind()) {
                case COMMENT_END:
                    // TODO unwind stack
                    comment.jjtSetLastToken(tok);
                    return comment;
                case COMMENT_DATA:
                    growDataLeaf(tok, tok);
                    break;
                case WHITESPACE:
                    linkLeaf(new JdWhitespace(tok));
                    break;
                case INLINE_TAG_START:
                    inlineTag(tok);
                    break;
                }
            }
            comment.jjtSetLastToken(prev);
            return comment;
        }
        return null;
    }

    private void inlineTag() {
        if (advance() != null && tok.getKind() == TAG_NAME) {
            open(new JdInlineTag(tok.getImage()));

        }
    }

    private boolean isEnd(JavadocToken next) {
        return next == null || next.getKind() == COMMENT_END;
    }

    private void treatAsDataUntil(final JavadocToken first, Function<? super JavadocToken, Boolean> recoverWhile) {
        growDataLeaf(first, consumeWhile(first, recoverWhile));
    }

    private @NonNull JavadocToken consumeWhile(final JavadocToken first, Function<? super JavadocToken, Boolean> take) {

        JavadocToken current = first;
        @NonNull JavadocToken last = current;
        while (current != null && take.apply(current)) {
            last = current;
            current = lexer.getNextToken();
        }

        return current == null ? last : current;
    }

    private JdMalformed error(JavadocTokenType expected, JavadocToken actual, Function<? super JavadocToken, Boolean> recoverWhile) {
        JdMalformed error = new JdMalformed(expected, actual);
        error.jjtSetFirstToken(actual);
        error.jjtSetLastToken(consumeWhile(actual, recoverWhile));
        return error;
    }

    private void linkLeaf(AbstractJavadocNode node) {
        if (node == null) {
            return;
        }
        AbstractJavadocNode top = this.stack.peek();
        Objects.requireNonNull(top).jjtAddChild(node, top.jjtGetNumChildren());
    }

    private void growDataLeaf(JavadocToken first, JavadocToken last) {
        AbstractJavadocNode top = this.stack.peek();
        JavadocNode lastNode = top.jjtGetNumChildren() > 0 ? top.jjtGetChild(top.jjtGetNumChildren() - 1) : null;
        if (lastNode instanceof JdCommentData) {
            ((JdCommentData) lastNode).jjtSetLastToken(last);
        } else {
            linkLeaf(new JdCommentData(first, last));
        }
    }


    private JavadocToken advance() {
        return tok = lexer.getNextToken();
    }

    private void open(AbstractJavadocNode node) {
        node.jjtSetFirstToken(tok);
        marks.push(curMark);
        curMark = nodes.size();
        nodes.push(node);
    }

    private void close() {
        AbstractJavadocNode top = nodes.pop();
        int arity = nodes.size() - curMark;
        curMark = marks.pop();
        while (arity-- > 0) {
            top.jjtAddChild(popNode(), arity);
        }
        top.jjtSetLastToken(tok);
        nodes.add(top);
    }

    private AbstractJavadocNode popNode() {
        curMark = marks.pop();
        return nodes.pop();
    }


}
