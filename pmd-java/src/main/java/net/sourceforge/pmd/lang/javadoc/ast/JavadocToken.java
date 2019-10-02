/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.ast.PrevLinkedToken;
import net.sourceforge.pmd.lang.ast.impl.OffsetBasedToken;
import net.sourceforge.pmd.lang.ast.impl.TokenDocument;

/** A token implementation for Javadoc nodes. */
public final class JavadocToken extends OffsetBasedToken<JavadocToken, TokenDocument<JavadocToken>> implements PrevLinkedToken<JavadocToken> {

    @Nullable
    JavadocToken prev;
    @Nullable
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

    @Override
    @Nullable
    public JavadocToken getPrevious() {
        return prev;
    }

    @Nullable
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


}
