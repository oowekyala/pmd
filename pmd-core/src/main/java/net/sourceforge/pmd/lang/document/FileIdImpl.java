/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.document;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.checkerframework.checker.nullness.qual.Nullable;

public final class FileIdImpl {
    private FileIdImpl() {

    }

    static final class FromPathFileId implements FileId {
        // Compute these beforehand as that will fail if the path
        // is invalid (better now than later).
        // Also, not hitting the filesystem every time we want to
        // do a compareTo is good for performance.
        private transient String absPath;
        private transient String fileName;
        private final String uriString;
        private final String origPath;
        private final @Nullable FileId fsPath;

        FromPathFileId(final Path path, final @Nullable FileId fsPath) {
            absPath = path.normalize().toAbsolutePath().toString();
            uriString = path.normalize().toUri().toString();
            fileName = path.getFileName().toString();
            origPath = path.toString();
            this.fsPath = fsPath;
        }

        private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            URI uri = URI.create(uriString);
            Path path = Paths.get(uri);
            this.absPath = path.toAbsolutePath().toString();
            this.fileName = path.getFileName().toString();
        }

        @Override
        public String getAbsolutePath() {
            return absPath;
        }

        @Override
        public String getUriString() {
            return uriString;
        }

        @Override
        public String getFileName() {
            return fileName;
        }

        @Override
        public String getOriginalPath() {
            return origPath;
        }

        @Override
        public @Nullable FileId getParentFsPath() {
            return fsPath;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof FileId
                && ((FileId) obj).getUriString().equals(this.getUriString());
        }

        @Override
        public int hashCode() {
            return getUriString().hashCode();
        }

        @Override
        public String toString() {
            return "FileId(fromPath=" + origPath + ")";
        }
    }


    static final class FromAbsPathFileId implements FileId {
        private final String absPath;
        private final FileId outer;
        private transient String fileName;

        FromAbsPathFileId(final String absPath, final @Nullable FileId outer) {
            this.outer = outer;
            this.absPath = absPath;
            this.fileName = Paths.get(absPath).getFileName().toString();
        }

        private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            this.fileName = Paths.get(absPath).getFileName().toString();
        }

        @Override
        public String getFileName() {
            return fileName;
        }

        @Override
        public String getOriginalPath() {
            return absPath;
        }

        @Override
        public String getAbsolutePath() {
            return absPath.replace('/', File.separatorChar);
        }

        @Override
        public String getUriString() {
            // we know this one uses platform specific thing (for display)
            String platformAbsPath = getAbsolutePath();
            // we know this one uses / (for URIs)
            String uriAbsPath = platformAbsPath.replace(File.separatorChar, '/');

            return outer != null ? "jar:" + outer.getUriString() + "!" + uriAbsPath
                                 : "file://" + uriAbsPath;
        }

        @Override
        public @Nullable FileId getParentFsPath() {
            return outer;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof FileId && getUriString().equals(((FileId) obj).getUriString());
        }

        @Override
        public int hashCode() {
            return getUriString().hashCode();
        }

        @Override
        public String toString() {
            return "FileId(fromAbsolutePath=" + absPath + ",outer=" + outer + ")";
        }

    }


    static final class FromPathLikeString implements FileId {
        private final String str;
        private transient Path absPath;


        FromPathLikeString(final String str) {
            this.str = str;
            absPath = Paths.get(str).toAbsolutePath();
        }

        private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            this.absPath = Paths.get(str).toAbsolutePath();
        }

        @Override
        public String getAbsolutePath() {
            return absPath.toString();
        }

        @Override
        public String getUriString() {
            // pretend...
            return "file://" + str;
        }

        @Override
        public String getFileName() {
            Path fname = absPath.getFileName();

            // null for the root path
            return fname == null ? "" : fname.toString();
        }

        @Override
        public String getOriginalPath() {
            return str;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof FileId
                && ((FileId) obj).getUriString().equals(this.getUriString());
        }

        @Override
        public int hashCode() {
            return getUriString().hashCode();
        }

        @Override
        public @Nullable FileId getParentFsPath() {
            return null;
        }

        @Override
        public String toString() {
            return "FileId(fromPathLike=" + str + ")";
        }

    }
}
