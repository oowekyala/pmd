package net.sourceforge.pmd.lang.java.symbols.internal.asm;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.document.FileId;
import net.sourceforge.pmd.lang.rule.Rule;

/**
 * A fine-grained dependency graph that stores the dependencies between analyzed files.
 *
 * <p>The analyzed files each have a set of dependencies on other files or on some jars
 * on the classpath.
 *
 * <p>The cached result of a file analysis is fresh if all the requests to the symbol resolver
 * made during the analysis (parsing, type resolution, and rule analysis) of the file will return
 * the same thing as they did during the initial processing. For instance, resolution of an ambiguous
 * name in a file in package p depends on the classes in that package, and also on the imported files.
 * The disambiguation process makes many requests to the classloader (symbol resolver).
 * All requests made to the symbol resolver count as dependencies of this file.
 *
 * <p>During file analysis, each request to the symbol resolver must be done with a specific request ID
 * which is associated to the file-id. This set of strings is the set of "request dependencies" of the file.
 * After analysis, the map of requests to results is cached. The results are either "failed" or a .class
 * file, with its checksum (this could be an ABI checksum for fine granularity).
 *
 * <p>Before the next analysis, for every file that did not change, the requests dependencies of that file are
 * replayed against the current classloader. If any request returns a different result, the dependencies are
 * invalidated and the file is reanalyzed.
 *
 * <p>The current incremental analysis will invalidate all files if any classpath entry has been updated. If
 * one file has been updated and recompiled, all files will therefore be reanalyzed. The new dependency mechanism
 * allows more fine-grained reanalysis, without ignoring dependencies.
 *
 * <p>Where this mechanism will be very useful is to process files that have not yet been compiled. To do this,
 * a first pass will parse all files in the analysis (possibly ignoring code blocks, because we only need symbol
 * signatures anyway). This allows us to build a symbol resolver that considers both classpath, and source path.
 * After the first pass, symbols are still incomplete as their signatures require name resolution. A second pass
 * resolves signatures and promotes these to full-fledged symbols. Note however that they don't have direct access
 * to the AST (we throw away the AST and backing text file to store symbols in a more compact way). Note that name
 * resolution here contributes "request dependencies" too. Once this is done, we have a symbol resolver that represents
 * the full project and does not require class files for the analysed files anymore.
 *
 * <p>At this point these symbols need to be cached for the next analysis (along with their hashes and dependency set).
 * Then rule execution can proceed. This requires full reparsing of changed files. Parsing the file entirely may require
 * more dependencies than just the ones required to resolve signatures. The additional dependencies are cached separately.
 *
 * <p>On the next analysis, if any file was changed, it is first shallow-parsed with the symbol-only parser. Then symbol
 * resolution is redone, and its symbols rehashed. If the hash is the same, then files that depend on that file are not invalidated.
 *
 * <p>At this point we can look forward to full multi-file analysis, where the AST of a file is accessible from the AST
 * of other files. This creates a stronger kind of dependency. There are two kinds of dependencies: either signature-only,
 * or full-text.
 *
 *
 */
public class ClassDependencyGraph {

    private final Map<FileId, FileRequests> classpathRequests = new ConcurrentHashMap<>();

    /**
     * Record that the given origin made a classpath request for the given binary name.
     *
     * @param binaryName Binary name for the request
     * @param request The origin file
     * @param found The result of the request
     */
    void recordClasspathRequest(ClasspathRequest request, @NonNull String binaryName, @Nullable ClassStub found) {
        if (request.origin == null || request.type == DependencyType.NO_DEP) {
            return;
        }

        classpathRequests.compute(request.origin, (fid, entries) -> {
            if (entries == null) {
                entries = new FileRequests();
            }
            long hash = found == null ? 0 : found.abiFingerprint;
            entries.record(binaryName, request.type == DependencyType.SIGNATURE, hash);
            return entries;
        });

        // What should this do?
        //  map the origin file name to the binary name + type of dependency


    }

    public enum DependencyType {
        /** The request only needs access to the signatures of the file. */
        SIGNATURE,
        /** The request needs access to the full text of the source file. */
        FULL,
        /** Used only to check a dependency, should not update dependencies. */
        NO_DEP
    }

    public static final class ClasspathRequest {
        /** The file making the request. */
        final @Nullable FileId origin;
        final DependencyType type;

        public ClasspathRequest(@Nullable FileId origin, DependencyType type) {
            this.origin = origin;
            this.type = type;
        }

        /**
         * No origin creates no dependency. Maybe it would be better to treat it as
         * unknown origin, ie, all files have a dependency on this one.
         */
        public static ClasspathRequest noOrigin() {
            return new ClasspathRequest(null, DependencyType.NO_DEP);
        }

        public static ClasspathRequest unknownOrigin() {
            return new ClasspathRequest(null, DependencyType.SIGNATURE);
        }

        public static ClasspathRequest signatureDep(FileId origin) {
            assert origin != null : "Null file id";
            return new ClasspathRequest(origin, DependencyType.SIGNATURE);
        }

        public static ClasspathRequest signatureDep(Node origin) {
            assert origin != null : "Null file id";
            return new ClasspathRequest(origin.getTextDocument().getFileId(), DependencyType.SIGNATURE);
        }

        public static ClasspathRequest fromRule(FileId origin, Rule rule) {
            return new ClasspathRequest(origin, DependencyType.SIGNATURE);
        }

        public static ClasspathRequest fromRule(Node origin, Rule rule) {
            return fromRule(origin.getTextDocument().getFileId(), rule);
        }
    }


    static final class FileRequests {
        private final HashMap<String, RequestData> byBinaryName = new HashMap<>();

        void record(String name, boolean isSigOnly, long resultHash) {
            byBinaryName.compute(name, (name2, data) -> {
                if (data == null) {
                    return new RequestData(name, isSigOnly, resultHash);
                } else {
                    data.isSigOnly = isSigOnly;
                    // note: maybe the hash for a source file will be different from the binary file.
                    assert data.resultHash == resultHash;
                    return data;
                }
            });
        }
    }

    static final class RequestData {
        private final String binName;
        private boolean isSigOnly;
        private final long resultHash;

        RequestData(String binName, boolean isSigOnly, long resultHash) {
            this.binName = binName;
            this.isSigOnly = isSigOnly;
            this.resultHash = resultHash;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            RequestData that = (RequestData) o;
            return isSigOnly == that.isSigOnly
                && Objects.equals(binName, that.binName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(binName, isSigOnly);
        }
    }

}
