/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ast.impl.javacc.io;

import java.io.IOException;
import java.io.Reader;

import net.sourceforge.pmd.lang.ast.CharStream;
import net.sourceforge.pmd.lang.ast.TokenMgrError;

/**
 * Retrieves the reader's input offsets. This provides a view into
 * the reader's own input.
 *
 * <p>The problem is character escaping. For example in the file
 * <pre>{@code cl\\u0061ss}</pre>
 *
 * <p>({@code \\u0061} is the char escape for 'a'). The reader will
 * output only the five characters [c,l,a,s,s], which is what the
 * {@link CharStream} will give to a token manager.
 * However for the purposes of reflecting the source file, the token
 * {@code class} needs to have length 11 (its image will still be
 * the unescaped {@code "class"}).
 *
 * <p>"Output offsets" are those obtained after escape translation,
 * "input offsets" are those in the original file. For example the
 * end of {@code cl\\u0061ss} has output offset 5, input offset 11.
 */
public interface OffsetAwareReader {

    /** @see Reader#read(char[], int, int) */
    int read(char[] cbuf, int off, int len) throws IOException;


    /** @see Reader#read() */
    int read() throws IOException;


    /** @see Reader#close() */
    void close() throws IOException;


    /**
     * Notify this instance that its output offsets STRICTLY before the given
     * offset will never be queried again. This affects {@link #getInputOffset(int)},
     * {@link #getLine(int)} and {@link #getColumn(int)}, and allows
     * this reader to release resources.
     *
     * @param outputOffset Output offset (POV of this instance)
     *
     * @throws AssertionError If two calls are made with decreasing values
     * @throws AssertionError If the parameter is greater than the current offset
     */
    void releaseBefore(int outputOffset);


    /**
     * Returns the input offset corresponding to the given output offset.
     * This includes the length of any unicode escapes.
     *
     * <pre>
     * input:      "a\u00a0b"
     * translated: "a b"
     *
     * inputOffset(0) = 0
     * inputOffset(1) = 1
     * inputOffset(2) = 7 // includes the length of the escape
     * </pre>
     *
     * @param outputOffset Output offset (POV of this instance)
     */
    int getInputOffset(int outputOffset);


    /**
     * Returns the line number at the given output offset. Note that
     * JavaCC only uses this to produce error messages for {@link TokenMgrError},
     * so the implementation does not need to be efficient, unless
     * tokens are line/column based.
     *
     * @param outputOffset Output offset (POV of this instance)
     */
    int getLine(int outputOffset);


    /**
     * Returns the column number at the given output offset. Note that
     * JavaCC only uses this to produce error messages for {@link TokenMgrError},
     * so the implementation does not need to be efficient, unless
     * tokens are line/column based.
     *
     * @param outputOffset Output offset (POV of this instance)
     */
    int getColumn(int outputOffset);


}
