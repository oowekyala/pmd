/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.ast.PrevLinkedToken;
import net.sourceforge.pmd.lang.ast.impl.OffsetBasedToken;
import net.sourceforge.pmd.lang.ast.impl.TokenDocument;

/** A token implementation for Javadoc nodes. */
public final class JdocToken extends OffsetBasedToken<JdocToken> implements PrevLinkedToken<JdocToken> {

    @Nullable
    JdocToken prev;
    @Nullable
    JdocToken next;

    private final String image;
    private final JdocTokenType kind;

    JdocToken(JdocTokenType kind, String image, int startInclusive, int endExclusive, TokenDocument document) {
        super(startInclusive, endExclusive, document);
        this.kind = kind;
        this.image = image;
    }

    /**
     * Constructor for a zero-length token.
     */
    JdocToken(JdocTokenType kind, int offset, TokenDocument document) {
        this(kind, "", offset, offset, document);
    }

    /** Returns the kind of this token. */
    public JdocTokenType getKind() {
        return kind;
    }

    @Override
    @Nullable
    public JdocToken getPrevious() {
        return prev;
    }

    @Nullable
    @Override
    public JdocToken getNext() {
        return next;
    }

    @Override
    public String getImage() {
        return image;
    }

    @Override
    public String toString() {
        return image;
    }

    /** This always returns null. There are no comment tokens in this javadoc grammar. */
    @Override
    public JdocToken getPreviousComment() {
        return null;
    }

    /**
     * Returns true if this token is implicit, ie a zero length token at
     * some position.
     */
    boolean isImplicit() {
        return getImage().isEmpty();
    }

    JdocToken split(int indexInImage, JdocTokenType leftKind, JdocTokenType rightKind) {
        assert indexInImage >= 0 && indexInImage <= image.length();

        String leftIm = this.image.substring(0, indexInImage);
        String rightIm = this.image.substring(indexInImage);

        JdocToken left = new JdocToken(leftKind,
                                       leftIm,
                                       this.getStartInDocument(),
                                       this.getStartInDocument() + indexInImage,
                                       this.getDocument());

        JdocToken right = new JdocToken(rightKind,
                                        rightIm,
                                        this.getStartInDocument() + indexInImage,
                                        this.getEndInDocument(),
                                        this.getDocument());

        left.prev = this.prev;
        left.next = right;

        right.prev = left;
        right.next = this.next;

        this.prev = null;
        this.next = null;

        return left;
    }

    /**
     * Creates a zero-length token with the given kind right before the
     * given successor token. This links the tokens appropriately.
     */
    static JdocToken implicitBefore(JdocTokenType kind, JdocToken successor) {
        JdocToken tok = new JdocToken(kind, successor.getStartInDocument(), successor.getDocument());
        tok.prev = successor.prev;
        tok.next = successor;
        successor.prev = tok;
        return tok;
    }
}
