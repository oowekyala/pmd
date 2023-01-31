/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ast;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

import net.sourceforge.pmd.lang.document.TextFile;

/**
 * An exception that occurs while processing a file. Subtypes include
 * <ul>
 * <li>{@link TokenMgrError}: lexical syntax errors
 * <li>{@link ParseException}: syntax errors
 * <li>{@link SemanticException}: exceptions occurring after the parsing
 * phase, because the source code is semantically invalid
 * </ul>
 */
public class FileAnalysisException extends RuntimeException {

    private String filename = TextFile.UNKNOWN_FILENAME;

    public FileAnalysisException() {
        super();
    }

    public FileAnalysisException(String message) {
        super(message);
    }

    public FileAnalysisException(Throwable cause) {
        super(cause);
    }

    public FileAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileAnalysisException setFileName(String filename) {
        this.filename = Objects.requireNonNull(filename);
        return this;
    }

    protected boolean hasFileName() {
        return !TextFile.UNKNOWN_FILENAME.equals(filename);
    }

    /**
     * The name of the file in which the error occurred.
     */
    public @NonNull String getFileName() {
        return filename;
    }

    @Override
    public String getMessage() {
        return errorKind() + StringUtils.uncapitalize(positionToString()) + ": " + super.getMessage();
    }

    protected String errorKind() {
        return "Error";
    }

    protected String positionToString() {
        if (hasFileName()) {
            return " in file '" + getFileName() + "'";
        }
        return "";
    }


    /**
     * Wraps the cause into an analysis exception. If it is itself an analysis
     * exception, just returns it after setting the filename for context.
     *
     * @param filename Filename
     * @param message  Context message, if the cause is not a {@link FileAnalysisException}
     * @param cause    Exception to wrap
     *
     * @return An exception
     */
    public static FileAnalysisException wrap(@NonNull String filename, @NonNull String message, @NonNull Throwable cause) {
        if (cause instanceof FileAnalysisException) {
            return ((FileAnalysisException) cause).setFileName(filename);
        }

        String fullMessage = "In file '" + filename + "': " + message;

        return new FileAnalysisException(fullMessage, cause).setFileName(filename);
    }
}
