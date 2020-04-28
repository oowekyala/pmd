/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.util.document;

import java.util.Comparator;
import java.util.Objects;

import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.internal.util.AssertionUtil;
import net.sourceforge.pmd.lang.ast.GenericToken;
import net.sourceforge.pmd.lang.ast.Node;

/**
 * A kind of {@link TextRegion} used for reporting. This provides access
 * to the line and column positions, as well as the text file. Instances
 * can be obtained from a {@link TextRegion} with {@link TextDocument#toLocation(TextRegion) TextDocument::toLocation}.
 *
 * <p>This admittedly should replace the text coordinates methods in {@link Node},
 * {@link GenericToken}, and {@link RuleViolation} at least.
 */
public final class FileLocation {

    public static final FileLocation UNDEFINED = new FileLocation("n/a", 1, 1, 1, 1);

    public static final Comparator<FileLocation> COORDS_COMPARATOR =
        Comparator.comparingInt(FileLocation::getBeginLine)
                  .thenComparingInt(FileLocation::getBeginColumn)
                  .thenComparingInt(FileLocation::getEndLine)
                  .thenComparingInt(FileLocation::getEndColumn);


    public static final Comparator<FileLocation> COMPARATOR =
        Comparator.comparing(FileLocation::getFileName).thenComparing(COORDS_COMPARATOR);

    private final int beginLine;
    private final int endLine;
    private final int beginColumn;
    private final int endColumn;
    private final String fileName;

    /** @see #location(String, int, int, int, int) */
    FileLocation(String fileName, int beginLine, int beginColumn, int endLine, int endColumn) {
        this.fileName = Objects.requireNonNull(fileName);
        this.beginLine = AssertionUtil.requireOver1("Begin line", beginLine);
        this.endLine = AssertionUtil.requireOver1("End line", endLine);
        this.beginColumn = AssertionUtil.requireOver1("Begin column", beginColumn);
        this.endColumn = AssertionUtil.requireOver1("End column", endColumn);

        requireLinesCorrectlyOrdered();
    }

    private void requireLinesCorrectlyOrdered() {
        if (beginLine > endLine) {
            throw AssertionUtil.mustBe("endLine", endLine, ">= beginLine (= " + beginLine + ")");
        } else if (beginLine == endLine && beginColumn > endColumn) {
            throw AssertionUtil.mustBe("endColumn", endColumn, ">= beginColumn (= " + beginColumn + ")");
        }
    }

    /**
     * File name of this position.
     */
    public String getFileName() {
        return fileName;
    }

    /** Inclusive, 1-based line number. */
    public int getBeginLine() {
        return beginLine;
    }

    /** Inclusive, 1-based line number. */
    public int getEndLine() {
        return endLine;
    }

    /** Inclusive, 1-based column number. */
    public int getBeginColumn() {
        return beginColumn;
    }

    /** <b>Exclusive</b>, 1-based column number. */
    public int getEndColumn() {
        return endColumn;
    }

    public String startPosToString() {
        return "line " + getBeginLine() + ", column " + getBeginColumn();
    }

    /**
     * Creates a new location from the given parameters.
     *
     * @throws IllegalArgumentException If the file name is null
     * @throws IllegalArgumentException If any of the line/col parameters are strictly less than 1
     * @throws IllegalArgumentException If the line and column are not correctly ordered
     * @throws IllegalArgumentException If the start offset or length are negative
     */
    public static FileLocation location(String fileName, int beginLine, int beginColumn, int endLine, int endColumn) {
        return new FileLocation(fileName, beginLine, beginColumn, endLine, endColumn);
    }

}
