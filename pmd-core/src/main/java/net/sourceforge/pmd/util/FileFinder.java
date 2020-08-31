/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.sourceforge.pmd.annotation.InternalApi;

/**
 * A utility class for finding files within a directory.
 * @deprecated Is internal API
 */
@Deprecated
@InternalApi
public class FileFinder {

    /**
     * Searches for files in a given directory.
     *
     * @param dir     the directory to search files
     * @param filter  the filename filter that can optionally be passed to get files that match this filter
     * @return list of files from the given directory
     */
    public void findFilesFrom(Set<Path> result, Path dir, Predicate<Path> filter) throws IOException {
        if (Files.isDirectory(dir)) {
            try (Stream<Path> pathStream = Files.walk(dir).filter(filter)) {
                pathStream.forEach(result::add);
            }
        } else {
            result.add(dir);
        }
    }

}
