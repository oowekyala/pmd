/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

import java.util.stream.Stream;

import net.sourceforge.pmd.internal.util.IteratorUtil;
import net.sourceforge.pmd.lang.ast.impl.OffsetBasedToken;
import net.sourceforge.pmd.lang.ast.impl.TokenDocument;

/** A token implementation for Javadoc nodes. */
public class JavadocToken extends OffsetBasedToken<JavadocToken, TokenDocument<JavadocToken>> {

    JavadocToken prev;
    JavadocToken next;

    private final String image;
    private final JavadocTokenType kind;

    JavadocToken(JavadocTokenType kind, String image, int startInclusive, int endExclusive, TokenDocument<JavadocToken> document) {
        super(startInclusive, endExclusive, document);
        this.kind = kind;
        this.image = image;
    }

    /** Returns the kind of this token. */
    public JavadocTokenType getKind() {
        return kind;
    }

    /** Returns the previous token. */
    public JavadocToken getPrevious() {
        return prev;
    }

    @Override
    public JavadocToken getNext() {
        return next;
    }

    /**
     * Returns null. There are no comment tokens in this Javadoc implementation.
     */
    @Override
    public JavadocToken getPreviousComment() {
        return null;
    }

    @Override
    public String getImage() {
        return image;
    }

    @Override
    public String toString() {
        return image;
    }


    public Stream<JavadocToken> rangeTo(JavadocToken last) {
        return IteratorUtil.generate(this, t -> t == last ? null : t.next);
    }
}
