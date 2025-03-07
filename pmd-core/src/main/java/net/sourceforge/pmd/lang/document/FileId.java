/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.document;

import java.io.Serializable;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.document.FileIdImpl.FromAbsPathFileId;
import net.sourceforge.pmd.lang.document.FileIdImpl.FromPathFileId;
import net.sourceforge.pmd.lang.document.FileIdImpl.FromPathLikeString;
import net.sourceforge.pmd.renderers.Renderer;
import net.sourceforge.pmd.reporting.RuleViolation;

/**
 * An identifier for a {@link TextFile}. This is not a path, but provides
 * several methods to be rendered into path-like strings in different formats
 * (for use mostly by {@link Renderer} instances). File IDs are used to
 * identify files e.g. in {@link RuleViolation}, {@link FileLocation}, {@link TextFile}.
 *
 * <p>Note that the addressed file may not be an actual file on a file system.
 * For instance, you can create file ids from strings ({@link #fromPathLikeString(String)}),
 * or use {@link #STDIN} to address standard input. The rendering methods
 * of this interface (like {@link #getAbsolutePath()}) do not have to return
 * actual paths for those exotic files, and operate on a best-effort basis.
 *
 * @author Clément Fournier
 */
public interface FileId extends Comparable<FileId>, Serializable {

    /**
     * The name used for an unknown file. This is mostly only
     * relevant for unit tests.
     */
    FileId UNKNOWN = new FileId() {
        @Override
        public String getFileName() {
            return "(unknown)";
        }

        @Override
        public String getOriginalPath() {
            return "(unknown)";
        }

        @Override
        public String getAbsolutePath() {
            return getOriginalPath();
        }

        @Override
        public String getUriString() {
            return "file://" + getOriginalPath();
        }

        @Override
        public @Nullable FileId getParentFsPath() {
            return null;
        }

        @Override
        public String toString() {
            return "FileId(unknown)";
        }

        private Object readResolve() {
            return UNKNOWN;
        }
    };

    /** The virtual file ID for standard input. */
    FileId STDIN = new FileId() {
        @Override
        public String getAbsolutePath() {
            return "stdin";
        }

        @Override
        public String getUriString() {
            return "stdin";
        }

        @Override
        public String getFileName() {
            return "stdin";
        }

        @Override
        public String getOriginalPath() {
            return "stdin";
        }

        @Override
        public @Nullable FileId getParentFsPath() {
            return null;
        }

        @Override
        public String toString() {
            return "FileId(STDIN)";
        }

        private Object readResolve() {
            return STDIN;
        }
    };


    /**
     * Return the simple file name, like {@link Path#getFileName()}.
     * This includes the extension.
     */
    String getFileName();

    /**
     * Return the path as it was input by the user. This may be a
     * relative or absolute path.
     */
    String getOriginalPath();

    /**
     * Return an absolute path to this file in its containing file system.
     * If the file is in a zip file, then this returns a path from the
     * zip root, and does not include the path of the zip itself.
     */
    String getAbsolutePath();

    /**
     * Return a string that looks like a URI pointing to this file.
     */
    String getUriString();

    /**
     * If this file is in a nested filesystem (eg a zip file), return
     * the file ID of the container in the outer file system. Return
     * null if this is in the root file system.
     */
    @Nullable FileId getParentFsPath();


    /**
     * Two file IDs are equal if they have the same {@link #getUriString()}.
     *
     * @param o Object
     */
    @Override
    boolean equals(Object o);


    @Override
    default int compareTo(FileId o) {
        return this.getAbsolutePath().compareTo(o.getAbsolutePath());
    }

    /**
     * This method is intentionally only meant for debugging, and its output
     * is unspecified. Code that needs a string representation should use one
     * of the named string conversion methods.
     */
    @Override
    String toString();

    /**
     * Return a path ID for the given string. The string is interpreted
     * as a file system path, so that {@link #getAbsolutePath()} and
     * {@link #getUriString()} may work.
     *
     * @param str A string. Should be a valid file system path for the platform (see
     *            {@link Paths#get(String, String...)}.
     *
     * @return A new file id
     */
    static FileId fromPathLikeString(String str) {
        return new FromPathLikeString(str);
    }

    /**
     * Return a new path id for the given path.
     *
     * @param path   The path
     * @param fsPath The file id of the containing file system, if it is some Zip file.
     *
     * @return A new file id.
     */
    static FileId fromPath(Path path, @Nullable FileId fsPath) {
        return new FromPathFileId(path, fsPath);
    }

    /**
     * Return a file ID for the given path. This uses {@link #fromPath(Path, FileId)}
     * and defaults the second parameter to null.
     */
    static FileId fromPath(Path path) {
        return fromPath(path, null);
    }

    /**
     * Return a file ID whose methods behave the same as the first parameter,
     * and whose {@link #getParentFsPath()} returns the second parameter.
     *
     * @param self         A file id
     * @param parentFsPath Another file id for the parent.
     */

    static FileId asChildOf(FileId self, FileId parentFsPath) {
        return new FileId() {
            @Override
            public @Nullable FileId getParentFsPath() {
                return parentFsPath;
            }

            @Override
            public String getUriString() {
                return self.getUriString();
            }

            @Override
            public String getFileName() {
                return self.getFileName();
            }

            @Override
            public String getOriginalPath() {
                return self.getOriginalPath();
            }

            @Override
            public String getAbsolutePath() {
                return self.getAbsolutePath();
            }

            @Override
            public String toString() {
                return "FileId(" + self + ",asChildOf=" + parentFsPath + ")";
            }
        };
    }

    /**
     * Return a file ID which interprets the first parameter as an absolute path.
     * The path must be a valid path for this system ({@link Paths#get(String, String...)} should not fail).
     * The URI is rebuilt using the outer file ID if it is non-null.
     *
     * @param absPath Absolute path for the file
     * @param outer   File ID of the outer file system (Zip), if it exists
     *
     * @return A new file id
     */
    static FileId fromAbsolutePath(String absPath, @Nullable FileId outer) {
        return new FromAbsPathFileId(absPath, outer);
    }

    /**
     * Return a file ID for a URI.
     * The URI must have scheme {@code file} or {@code jar} and be a
     * valid URI (see {@link URI#create(String)}). If the scheme is {@code jar},
     * then the {@link #getParentFsPath()} is populated with the path of the jar.
     *
     * @param uriStr A uri string
     *
     * @return A new file id
     */
    static FileId fromURI(String uriStr) throws IllegalArgumentException {
        URI uri = URI.create(uriStr);
        String schemeSpecificPart = uri.getSchemeSpecificPart();
        if ("jar".equals(uri.getScheme())) {
            int split = schemeSpecificPart.lastIndexOf('!');
            if (split == -1) {
                throw new IllegalArgumentException("expected a jar specific path");
            } else {
                String zipUri = schemeSpecificPart.substring(0, split);
                String localPath = schemeSpecificPart.substring(split + 1);
                FileId outer = fromURI(zipUri);

                return fromAbsolutePath(localPath, outer);
            }
        } else if ("file".equals(uri.getScheme())) {
            Path path = Paths.get(uri);
            return fromPath(path);
        }
        throw new UnsupportedOperationException("Unknown scheme " + uriStr);
    }
}
