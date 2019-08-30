/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.document;


import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch.Diff;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch.Operation;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch.Patch;

/** Handles text updates for a {@link MutableDocument}. */
public interface ReplaceHandler<T extends Object> {


    /**
     * Replace the content of a region with some text.
     */
    void replace(TextRegion original, TextRegion mapped, String text);


    /**
     * Commit the document (eg writing it to disk), and returns a new
     * document corresponding to the new document.
     *
     * @return An updated replace function
     */
    T commit() throws IOException;


    /**
     * Write updates into an in-memory buffer, commit writes to disk.
     * This doesn't use any IO resources outside of the commit method.
     */
    static ReplaceHandler<Void> bufferedFile(CharSequence originalBuffer, Path path, Charset charSet) {

        return new ReplaceHandler<Void>() {

            private StringBuilder builder = new StringBuilder(originalBuffer);

            @Override
            public void replace(TextRegion original, TextRegion mapped, String text) {
                builder.replace(mapped.getStartOffset(), mapped.getEndOffset(), text);
            }

            @Override
            public Void commit() throws IOException {
                String done = builder.toString();
                byte[] bytes = done.getBytes(charSet);
                Files.write(path, bytes);
                return null;
            }
        };
    }


    /** A replace handler that may not throw an exception. */
    interface SafeReplaceHandler<T> extends ReplaceHandler<T> {

        @Override
        T commit();
    }

}
