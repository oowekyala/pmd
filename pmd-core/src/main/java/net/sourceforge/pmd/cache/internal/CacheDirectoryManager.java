package net.sourceforge.pmd.cache.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import net.sourceforge.pmd.lang.Language;

/**
 * Interface with a cache directory where PMD can put its data.
 */
public class CacheDirectoryManager {

    private final Path root;

    public CacheDirectoryManager(Path root) {
        this.root = root;
    }

    public void initCache() throws IOException {
        if (Files.isRegularFile(root)) {
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

    public Path getLanguageCache(Language language) throws IOException {
        Path dir = root.resolve(language.getId() + "-cache");
        Files.createDirectories(dir);
        return dir;
    }

}
