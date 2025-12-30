/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.test.schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Properties;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.rule.Rule;

/**
 * @author Clément Fournier
 */
public class RuleTestDescriptor {

    private boolean disabled;
    private boolean focused;
    private String description;
    private LanguageVersion languageVersion;
    private final Properties properties = new Properties();
    private final int index;
    private final Rule rule;
    private String code;
    private List<ExpectedProblem> expectedProblems = new ArrayList<>();
    private int lineNumber;

    @Deprecated
    public static final class SuppressionDescriptor {
        private final int line;
        private final String suppressorId;

        private SuppressionDescriptor(int line, String suppressorId) {
            this.line = line;
            this.suppressorId = suppressorId;
        }

        public int getLine() {
            return line;
        }

        public String getSuppressorId() {
            return suppressorId;
        }
    }

    public RuleTestDescriptor(int index, Rule rule) {
        this.index = index;
        this.rule = rule;
        this.languageVersion = rule.getLanguage().getDefaultVersion();
    }

    public Rule getRule() {
        return rule;
    }

    public Properties getProperties() {
        return properties;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LanguageVersion getLanguageVersion() {
        return languageVersion;
    }

    public void setLanguageVersion(LanguageVersion languageVersion) {
        if (!languageVersion.getLanguage().equals(this.getRule().getLanguage())) {
            throw new IllegalArgumentException("Invalid version " + languageVersion);
        }
        this.languageVersion = languageVersion;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = Objects.requireNonNull(code);
    }

    /**
     * Set the expected violations for this test, overwriting the previous value.
     * This list can contain also expected suppressions.
     *
     * @param problems list of problems
     */
    public void setExpectedProblems(@NonNull List<ExpectedProblem> problems) {
        this.expectedProblems = new ArrayList<>(problems);
        this.expectedProblems.sort(ExpectedProblem.COMPARATOR);
    }

    /**
     * @deprecated Use {@link #setExpectedProblems(List)}
     */
    @Deprecated
    public void recordExpectedViolations(int expectedProblems, List<Integer> expectedLineNumbers, List<String> expectedMessages) {
        checkListSize(expectedProblems, expectedLineNumbers);
        checkListSize(expectedProblems, expectedMessages);
        this.expectedProblems.clear();

        for (int i = 0; i < expectedProblems; i++) {
            ExpectedProblem prob = new ExpectedProblem();
            if (i < expectedLineNumbers.size()) {
                prob.lineNumber = expectedLineNumbers.get(i);
            }
            if (i < expectedMessages.size()) {
                prob.message = expectedMessages.get(i);
            }
            this.expectedProblems.add(prob);
        }
    }

    /**
     * @deprecated Use {@link #setExpectedProblems(List)}
     */
    @Deprecated
    public void recordExpectedViolations(int expectedProblems, List<Integer> expectedLineNumbers, List<Integer> expectedEndLineNumbers, List<String> expectedMessages) {
        checkListSize(expectedProblems, expectedEndLineNumbers);
        recordExpectedViolations(expectedProblems, expectedLineNumbers, expectedMessages);
        for (int i = 0; i < expectedProblems; i++) {
            ExpectedProblem prob = this.expectedProblems.get(i);
            if (i < expectedEndLineNumbers.size()) {
                prob.endLineNumber = expectedEndLineNumbers.get(i);
            }
        }
    }

    private void checkListSize(int expectedProblems, List<?> expectedMessages) {
        if (!expectedMessages.isEmpty() && expectedProblems != expectedMessages.size()) {
            throw new IllegalArgumentException(
                "Expected list of size " + expectedProblems + ", got " + expectedMessages);
        }
    }

    @Deprecated
    public int getExpectedProblems() {
        return (int) expectedProblems.stream().filter(it -> !it.isSuppressed()).count();
    }

    public int getIndex() {
        return index;
    }

    @Deprecated
    public List<Integer> getExpectedLineNumbers() {
        if (expectedProblems.stream().allMatch(it -> it.getLineNumber().isPresent())) {
            return expectedProblems.stream().map(ExpectedProblem::getLineNumber)
                                   .map(OptionalInt::getAsInt).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Deprecated
    public List<Integer> getExpectedEndLineNumbers() {
        if (expectedProblems.stream().allMatch(it -> it.getEndLineNumber().isPresent())) {
            return expectedProblems.stream().map(ExpectedProblem::getEndLineNumber)
                                   .map(OptionalInt::getAsInt).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Deprecated
    public List<String> getExpectedMessages() {
        if (expectedProblems.stream().allMatch(it -> it.getMessage().isPresent())) {
            return expectedProblems.stream().map(ExpectedProblem::getMessage)
                                   .map(Optional::get).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * Return a sorted list of expected problems. This may contain also
     * expected suppressions.
     */
    public List<ExpectedProblem> getExpectedProblemList() {
        return expectedProblems;
    }

    public List<ExpectedProblem> getExpectedViolations() {
        return expectedProblems.stream().filter(it -> !it.isSuppressed()).collect(Collectors.toList());
    }

    public List<ExpectedProblem> getExpectedSuppressedViolations() {
        return expectedProblems.stream().filter(ExpectedProblem::isSuppressed).collect(Collectors.toList());
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    /**
     * The line number of the test in the test file.
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Set the line number of the test in the test file.
     */
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    @Deprecated
    public boolean hasExpectedSuppressions() {
        return expectedProblems.stream().anyMatch(ExpectedProblem::isSuppressed);
    }

    @Deprecated
    public void recordExpectedSuppression(int line) {
        recordExpectedSuppression(line, "");
    }

    /**
     * @deprecated Use {@link #setExpectedProblems(List)}
     */
    @Deprecated
    public void recordExpectedSuppression(int line, String suppressor) {
        ExpectedProblem problem = new ExpectedProblem();
        problem.lineNumber = line;
        problem.suppressorId = suppressor;
        this.expectedProblems.add(problem);
        this.expectedProblems.sort(ExpectedProblem.COMPARATOR);
    }

    @Deprecated
    public List<SuppressionDescriptor> getExpectedSuppressions() {
        return expectedProblems.stream().filter(ExpectedProblem::isSuppressed)
                               .map(it -> new SuppressionDescriptor(it.lineNumber, it.suppressorId))
                               .collect(Collectors.toList());
    }


    /**
     * A problem (rule violation or suppressed violation) expected to
     * be found when running a rule on a test code sample.
     */
    public static final class ExpectedProblem {

        private static final Comparator<ExpectedProblem> COMPARATOR =
            Comparator.<ExpectedProblem>comparingInt(it -> it.lineNumber)
                      .thenComparingInt(it -> it.endLineNumber);

        /** Start line. */
        int lineNumber = -1;
        /** End line. */
        int endLineNumber = -1;
        @Nullable
        String message = null;
        @Nullable
        String suppressorId = null;

        public static ExpectedProblem startingAtLine(int lineNumber) {
            assert lineNumber >= 1;
            ExpectedProblem result = new ExpectedProblem();
            result.lineNumber = lineNumber;
            return result;
        }

        public static ExpectedProblem suppressed(int lineNumber, @Nullable String suppressorId) {
            ExpectedProblem result = startingAtLine(lineNumber);
            result.suppressorId = suppressorId == null ? "" : suppressorId;
            return result;
        }

        public OptionalInt getLineNumber() {
            return lineNumber < 1 ? OptionalInt.empty() : OptionalInt.of(lineNumber);
        }

        public OptionalInt getEndLineNumber() {
            return endLineNumber < 1 ? OptionalInt.empty() : OptionalInt.of(endLineNumber);
        }

        public Optional<String> getMessage() {
            return Optional.ofNullable(message);
        }

        public boolean isSuppressed() {
            return suppressorId != null;
        }

        public Optional<String> suppressorId() {
            return Optional.ofNullable(suppressorId);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ExpectedProblem that = (ExpectedProblem) o;
            return lineNumber == that.lineNumber && endLineNumber == that.endLineNumber
                   && Objects.equals(message, that.message)
                   && Objects.equals(suppressorId, that.suppressorId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(lineNumber, endLineNumber, message, suppressorId);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (getLineNumber().isPresent()) {
                sb.append("line ").append(getLineNumber().getAsInt());
            }
            if (getEndLineNumber().isPresent()) {
                sb.append("-").append(getEndLineNumber().getAsInt());
            }
            if (isSuppressed()) {
                sb.append(" (suppressed)");
            }
            sb.append(": ").append(getMessage().orElse("(no message)"));
            return sb.toString();
        }
    }
}
