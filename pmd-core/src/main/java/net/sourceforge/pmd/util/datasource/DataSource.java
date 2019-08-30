/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.util.datasource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.document.ReplaceHandler;

/**
 * Represents a source file to be analyzed. Different implementations can get
 * the source file from different places: the filesystem, a zip or jar file,
 * etc.
 */
public interface DataSource {
    /**
     * Get an InputStream on the source file.
     *
     * @return the InputStream reading the source file
     * @throws IOException
     *             if the file can't be opened
     */
    InputStream getInputStream() throws IOException;


    @Nullable
    default ReplaceHandler<Void> getReplaceHandler(CharSequence fullText) {
        return null;
    }

    /**
     * Return a nice version of the filename.
     *
     * @param shortNames
     *            true if short names are being used
     * @param inputFileName
     *            name of a "master" file this file is relative to
     * @return String
     */
    String getNiceFileName(boolean shortNames, String inputFileName);

    static DataSource fromString(String text, String name) {
        return new DataSource() {
            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(text.getBytes());
            }

            @Override
            public String getNiceFileName(boolean shortNames, String inputFileName) {
                return name;
            }
        };
    }
}
