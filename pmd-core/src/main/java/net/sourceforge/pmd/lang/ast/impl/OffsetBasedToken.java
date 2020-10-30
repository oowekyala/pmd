/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ast.impl;

import net.sourceforge.pmd.lang.ast.GenericToken;
import net.sourceforge.pmd.util.document.TextRegion;

/**
 * Base class for tokens based on the (start, end) offset representation.
 */
public abstract class OffsetBasedToken<T extends OffsetBasedToken<T, D>, D extends TokenDocument<T>> implements GenericToken<T> {

    protected final D document;
    private final int startInclusive;
    private final int endExclusive;

    public OffsetBasedToken(int startInclusive,
                            int endExclusive,
                            D document) {
        this.startInclusive = startInclusive;
        this.endExclusive = endExclusive;
        this.document = document;
    }

    public D getDocument() {
        return document;
    }

    @Override
    public final TextRegion getRegion() {
        return TextRegion.fromBothOffsets(startInclusive, endExclusive);
    }
}

