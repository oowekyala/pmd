package net.sourceforge.pmd.cache.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.annotation.Experimental;
import net.sourceforge.pmd.lang.Language;

/**
 * Interface with a cache directory where PMD can save data from one run to the next.
 */
@Experimental
public final class PmdCacheManager {

    private static final Logger LOG = LoggerFactory.getLogger(PmdCacheManager.class);

    /** Root directory of the cache. */
    private final Path root;
    /** Temporary directory. */
    private final Path tmpDir;

    /** Initialize a cache manager with the given directory. */
    public PmdCacheManager(Path root) {
        this.root = root;
    }

    /**
     * Initialize the cache directory by creating it. If the root of the cache
     * is not a directory, delete it before creating the directory.
     *
     * @throws IOException If creating directories fails
     */
    public void initCache() throws IOException {
        if (Files.isRegularFile(root)) {
            LOG.debug("Path is not a directory, deleting it before proceeding: {}", root);
            Files.delete(root);
        }
        if (!Files.exists(root)) {
            Files.createDirectories(root);
        }
    }

    /**
     * Path to the violation cache file within the directory.
     */
    public Path getAnalysisCacheFile() {
        return root.resolve("analysis-cache");
    }

    /**
     * Return a language-specific directory for a {@link net.sourceforge.pmd.lang.LanguageProcessor}
     * to save data.
     * @param language A language
     * @return A path (may not have been created)
     */
    public Path getLanguageCache(Language language) {
        return root.resolve(language.getId() + "-cache");
    }

}
