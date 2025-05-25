/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.ast.PrevLinkedToken;
import net.sourceforge.pmd.lang.ast.impl.OffsetBasedToken;
import net.sourceforge.pmd.lang.document.Chars;
import net.sourceforge.pmd.lang.document.FileLocation;

/** A token implementation for Javadoc nodes. */
public final class JdocToken extends OffsetBasedToken<JdocToken, JavadocTokenDocument> implements PrevLinkedToken<JdocToken> {

    @Nullable
    JdocToken prev;
    @Nullable
    JdocToken next;

    private final JdocTokenType kind;
    private String image;

    JdocToken(JdocTokenType kind, @Nullable String constImage, int startInclusive, int endExclusive, JavadocTokenDocument document) {
        super(startInclusive, endExclusive, document);
        this.kind = kind;
        image = kind.isConst() ? kind.getConstValue() : constImage;
    }

    /**
     * Constructor for a zero-length token.
     */
    JdocToken(JdocTokenType kind, int offset, JavadocTokenDocument document) {
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
    public Chars getImageCs() {
        return document.getTextDocument().sliceTranslatedText(getRegion());
    }

    @Override
    public String getImage() {
        if (image == null) {
            image = getImageCs().toString();
        }
        return image;
    }

    @Override
    public FileLocation getReportLocation() {
        return document.getTextDocument().toLocation(getRegion());
    }

    /** This always returns null. There are no comment tokens in this javadoc grammar. */
    @Override
    public JdocToken getPreviousComment() {
        return null;
    }

    @Override
    public boolean isImplicit() {
        return getImage().isEmpty();
    }

    @Override
    public boolean isEof() {
        return false;
    }

    /**
     * Creates a zero-length token with the given kind right before the
     * given successor token. This links the tokens appropriately.
     */
    static JdocToken implicitBefore(JdocTokenType kind, JdocToken successor) {
        JdocToken tok = new JdocToken(kind, successor.getRegion().getStartOffset(), successor.getDocument());
        tok.prev = successor.prev;
        tok.next = successor;
        successor.prev = tok;
        return tok;
    }

    @Override
    public String toString() {
        return "JdocToken{" +
            "kind=" + kind +
            ", image='" + image + '\'' +
            '}';
    }
}
