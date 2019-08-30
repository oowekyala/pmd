/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.document;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

/**
 * Represents a mutable text document. Instances of this interface maintain
 * a coordinate system that is consistent with the original state of the file,
 * even after performing mutation operations.
 *
 * <p>For example, take a document containing the text "a".
 * You insert "k " at index 0. The document is now "k a". If you
 * now insert "g " at index 0, the document is now "k g a", instead
 * of "g k a", meaning that the index 0 is still relative to the old "a"
 * document.
 *
 * <p>Consider that all mutation operations shift the coordinate system
 * transparently.
 */
public interface MutableDocument<T> extends Document, Closeable {

    /** Insert some text in the document. */
    void insert(int beginLine, int beginColumn, CharSequence textToInsert);


    /** Insert some text in the document. */
    void insert(int offset, CharSequence textToInsert);


    /** Replace a region with some new text. */
    void replace(TextRegion region, CharSequence textToReplace);


    /** Delete a region in the document. */
    void delete(TextRegion region);


    /** Commit the document, discarding the result of the replace handler. */
    @Override
    void close() throws IOException;


    /** Commit the document, returning what the replace handler produced. */
    T commit() throws IOException;

    /**
     * Returns the original text, source of the coordinate system used by mutation
     * operations.
     */
    @Override
    CharSequence getText();


    static MutableDocument<Void> forFile(final Path file, final Charset charset) throws IOException {
        Document doc = Document.forFile(file, charset);
        return doc.newMutableDoc(ReplaceHandler.bufferedFile(doc.getText(), file, charset));
    }


    interface SafeMutableDocument<T> extends MutableDocument<T> {

        @Override
        T commit();


        @Override
        void close();
    }

}
