/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ast.impl;

import net.sourceforge.pmd.lang.ast.GenericToken;

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
    public int getStartInDocument() {
        return startInclusive;
    }

    @Override
    public int getEndInDocument() {
        return endExclusive;
    }

    @Override
    public int getBeginLine() {
        return document == null ? -1 : document.lineNumberFromOffset(startInclusive);
    }

    @Override
    public int getEndLine() {
        return document == null ? -1 : document.lineNumberFromOffset(endExclusive);
    }

    @Override
    public int getBeginColumn() {
        return document == null ? -1 : document.columnFromOffset(startInclusive);
    }

    @Override
    public int getEndColumn() {
        return document == null ? -1 : document.columnFromOffset(endExclusive);
    }

}

