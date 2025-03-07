package net.sourceforge.pmd.lang.java.symbols.internal.asm;

import java.util.Objects;

import org.checkerframework.checker.nullness.qual.NonNull;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.document.FileId;
import net.sourceforge.pmd.lang.rule.Rule;

/**
 * Metadata about the origin of a request to the classpath. This allows
 * tracking dependencies between source files and classpath entries.
 */
public abstract class ClasspathRequest {

    private ClasspathRequest() {
        // internal extension only
    }

    public static ClasspathRequest unknownOrigin() {
        return new SourceFileRequest(FileId.UNKNOWN, DependencyType.SIGNATURE);
    }

    static ClasspathRequest noOrigin() {
        return NoOrigin.INSTANCE;
    }

    public static ClasspathRequest signatureDep(@NonNull FileId origin) {
        return new SourceFileRequest(origin, DependencyType.SIGNATURE);
    }

    public static ClasspathRequest signatureDep(Node origin) {
        return signatureDep(origin.getTextDocument().getFileId());
    }

    public static ClasspathRequest fromRule(FileId origin, Rule unused) {
        return signatureDep(origin);
    }

    public static ClasspathRequest fromRule(Node origin, Rule rule) {
        return fromRule(origin.getTextDocument().getFileId(), rule);
    }

    static ClasspathRequest classFileRequest(String internalName) {
        return new ClassFileRequest(internalName);
    }

    static ClasspathRequest moduleFileRequest(String internalName) {
        return new ModuleFileRequest(internalName);
    }

    enum DependencyType {
        /** The request only needs access to the signatures of the file. */
        SIGNATURE,
        /** The request needs access to the full text of the source file. */
        FULL,
    }

    /** Does not record dependencies. */
    static final class NoOrigin extends ClasspathRequest {
        static final NoOrigin INSTANCE = new NoOrigin();

        private NoOrigin() {
        }
    }

    /**
     * The request is made from a source file analysed by PMD.
     */
    static final class SourceFileRequest extends ClasspathRequest {
        /** The file making the request. */
        final @NonNull FileId origin;
        /** The kind of data requested. */
        final DependencyType type;

        SourceFileRequest(@NonNull FileId origin, DependencyType type) {
            this.origin = Objects.requireNonNull(origin);
            this.type = type;
        }

    }

    /**
     * The request is made from a class file found on the classpath.
     * This should only be created internally.
     */
    static final class ClassFileRequest extends ClasspathRequest {
        final String internalName;

        ClassFileRequest(String internalName) {
            this.internalName = internalName;
        }
    }

    static final class ModuleFileRequest extends ClasspathRequest {
        final String moduleName;

        ModuleFileRequest(String moduleName) {
            this.moduleName = moduleName;
        }
    }

}
