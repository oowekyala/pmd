/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ast.impl.javacc.io;

import org.apache.commons.io.input.CharSequenceReader;

import net.sourceforge.pmd.util.StringUtil;

/**
 * An offset-aware char sequence reader. This implements line tracking
 * inefficiently, but since JavaCC only uses {@link #getColumn(int)} and
 * {@link #getLine(int)} for error messages (ie very infrequently), it
 * doesn't matter.
 */
public class CharSeqReader extends CharSequenceReader implements OffsetAwareReader {

    private final CharSequence charSequence;

    /**
     * Construct a new instance with the specified character sequence.
     *
     * @param charSequence The character sequence, may be {@code null}
     */
    public CharSeqReader(CharSequence charSequence) {
        super(charSequence);
        this.charSequence = charSequence;
    }

    @Override
    public void releaseBefore(int outputOffset) {
        // no resources to release
    }

    @Override
    public int getInputOffset(int outputOffset) {
        return outputOffset;
    }

    @Override
    public int getLine(int outputOffset) {
        return StringUtil.lineNumberAt(charSequence, outputOffset);
    }

    @Override
    public int getColumn(int outputOffset) {
        return StringUtil.columnNumberAt(charSequence, outputOffset);
    }

    @Override
    public void close() {
        // do nothing, prevent it from resetting
    }
}
