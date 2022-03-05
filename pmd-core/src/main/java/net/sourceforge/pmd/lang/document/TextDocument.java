/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.document;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;

import org.checkerframework.checker.nullness.qual.NonNull;

import net.sourceforge.pmd.cpd.SourceCode;
import net.sourceforge.pmd.internal.util.AssertionUtil;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.util.datasource.DataSource;

/**
 * Represents a textual document, providing methods to edit it incrementally
 * and address regions of text. A text document delegates IO operations
 * to a {@link TextFile}. It reflects some in-memory snapshot of the file,
 * though the file may still be edited externally.
 *
 * <p>TextDocument is meant to replace CPD's {@link SourceCode} and PMD's
 * {@link DataSource}, though the abstraction level of {@link DataSource}
 * is the {@link TextFile}.
 */
public interface TextDocument extends Closeable {
    // todo logical sub-documents, to support embedded languages
    //  ideally, just slice the text, and share the positioner
    //  a problem with document slices becomes reference counting for the close routine


    // todo text edition (there are some reverted commits in the branch
    //  with part of this, including a lot of tests)

    /**
     * Returns the language version that should be used to parse this file.
     */
    LanguageVersion getLanguageVersion();

    /**
     * Returns {@link TextFile#getPathId()} for the text file backing this document.
     */
    String getPathId();

    /**
     * Returns {@link TextFile#getDisplayName()} for the text file backing this document.
     */
    String getDisplayName();


    /**
     * Returns the current text of this document. Note that this doesn't take
     * external modifications to the {@link TextFile} into account.
     *
     * <p>Line endings are normalized to {@link TextFileContent#NORMALIZED_LINE_TERM}.
     *
     * @see TextFileContent#getNormalizedText()
     */
    Chars getText();

    /**
     * Returns a slice of the original text. Note that this is not the
     * same as {@code getText().subsequence}, as if this document has
     * translated escapes, the returned char slice will contain the
     * untranslated escapes, whereas {@link #getText()} would return
     * the translated characters.
     *
     * @param region A region, in the coordinate system of this document
     *
     * @return The slice of the original text that corresponds to the region
     *
     * @throws IndexOutOfBoundsException If the region is not a valid range
     */
    Chars sliceOriginalText(TextRegion region);

    /**
     * Returns a slice of the source text. This is always equal to
     * {@code getText().slice(region)}, as the text is the translated text.
     *
     * @param region A region, in the coordinate system of this document
     *
     * @return The slice of the original text that corresponds to the region
     *
     * @throws IndexOutOfBoundsException If the region is not a valid range
     */
    default Chars sliceTranslatedText(TextRegion region) {
        return getText().slice(region);
    }


    /**
     * Returns a checksum for the contents of the file.
     *
     * @see TextFileContent#getCheckSum()
     */
    long getCheckSum();


    /**
     * Returns the input offset for the given output offset. This maps
     * back an offset in the coordinate system of this document, to the
     * coordinate system of the original document. This includes the
     * length of any unicode escapes.
     *
     * <pre>
     * input:      "a\u00a0b"   (original document)
     * translated: "a b"        (this document)
     *
     * translateOffset(0) = 0
     * translateOffset(1) = 1
     * translateOffset(2) = 7 // includes the length of the escape
     * </pre>
     *
     * @param outOffset Output offset
     * @param inclusive Whether the offset is to be interpreted as the index of a character (true),
     *                  or the position after a character (false)
     *
     * @return Input offset
     */
    int inputOffset(int outOffset, boolean inclusive);

    /**
     * Translate a region given in the the coordinate system of this
     * document, to the coordinate system of the original document.
     * This works as if creating a new region with both start and end
     * offsets translated through {@link #inputOffset(int, boolean)}. The
     * returned region may have a different length.
     *
     * @param outputRegion Output region
     *
     * @return Input region
     */
    TextRegion inputRegion(TextRegion outputRegion);


    /**
     * Returns a reader over the text of this document.
     */
    default Reader newReader() {
        return getText().newReader();
    }

    /**
     * Returns the length in characters of the {@linkplain #getText() text}.
     */
    default int getLength() {
        return getText().length();
    }


    /**
     * Returns a region that spans the text of all the given lines.
     * This is intended to provide a replacement for {@link SourceCode#getSlice(int, int)}.
     *
     * <p>Note that, as line numbers may only be obtained from {@link #toLocation(TextRegion)},
     * and hence are line numbers of the original source, both parameters
     * must be line numbers of the source text and not the translated text
     * that this represents.
     *
     * @param startLineInclusive Inclusive start line number (1-based)
     * @param endLineInclusive   Inclusive end line number (1-based)
     *
     * @throws IndexOutOfBoundsException If the arguments do not identify
     *                                   a valid region in the source document
     */
    TextRegion createLineRange(int startLineInclusive, int endLineInclusive);


    /**
     * Turn a text region into a {@link FileLocation}.
     *
     * @return A new file position
     *
     * @throws IndexOutOfBoundsException If the argument is not a valid region in this document
     */
    FileLocation toLocation(TextRegion region);


    // todo doc
    default FileLocation createLocation(int bline, int bcol, int eline, int ecol) {
        return FileLocation.range(getDisplayName(), bline, bcol, eline, ecol);
    }

    /**
     * Slice a piece of text as a new logical sub-document.
     *
     * @param region          Text region (see {@link #sliceTranslatedText(TextRegion)})
     * @param languageVersion Language version of the new document (may be different from that of this document)
     *
     * @return a new sub-document
     *
     * @throws IndexOutOfBoundsException if the region is invalid
     * @throws NullPointerException      if any parameter is null
     */
    default TextDocument subDocument(TextRegion region, LanguageVersion languageVersion) {
        return new ContiguousSubDocument(this, languageVersion, region);
    }

    /**
     * Determines the line number at the given offset (inclusive).
     *
     * @return the line number at the given index
     *
     * @throws IndexOutOfBoundsException If the argument is not a valid offset in this document
     */
    default int lineNumberAt(int offset) {
        return toLocation(TextRegion.fromOffsetLength(offset, 0)).getBeginLine();
    }

    /**
     * Closing a document closes the underlying {@link TextFile}.
     * New editors cannot be produced after that, and the document otherwise
     * remains in its current state.
     *
     * @throws IOException           If {@link TextFile#close()} throws
     * @throws IllegalStateException If an editor is currently open. In this case
     *                               the editor is rendered ineffective before the
     *                               exception is thrown. This indicates a programming
     *                               mistake.
     */
    @Override
    void close() throws IOException;

    static TextDocument create(TextFile textFile) throws IOException {
        return new RootTextDocument(textFile);
    }

    /**
     * Returns a read-only document for the given text.
     *
     * @see TextFile#forCharSeq(CharSequence, String, LanguageVersion)
     */
    static TextDocument readOnlyString(final CharSequence source, LanguageVersion lv) {
        return readOnlyString(source, TextFile.UNKNOWN_FILENAME, lv);
    }

    /**
     * Returns a read-only document for the given text. This works as
     * if by calling {@link TextDocument#create(TextFile)} on a textfile
     * produced by {@link TextFile#forCharSeq(CharSequence, String, LanguageVersion) forString},
     * but doesn't throw {@link IOException}, as such text files will
     * not throw.
     *
     * @see TextFile#forCharSeq(CharSequence, String, LanguageVersion)
     */
    @SuppressWarnings("PMD.CloseResource")
    static TextDocument readOnlyString(@NonNull CharSequence source, @NonNull String filename, @NonNull LanguageVersion lv) {
        TextFile textFile = TextFile.forCharSeq(source, filename, lv);
        try {
            return create(textFile);
        } catch (IOException e) {
            throw AssertionUtil.shouldNeverBeThrown(e, "String text file should never throw IOException");
        }
    }

}
