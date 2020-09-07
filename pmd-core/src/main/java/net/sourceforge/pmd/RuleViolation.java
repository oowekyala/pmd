/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd;

import net.sourceforge.pmd.lang.rule.RuleDescriptor;

/**
 * A RuleViolation is created by a Rule when it identifies a violation of the
 * Rule constraints. RuleViolations are simple data holders that are collected
 * into a {@link Report}.
 *
 * <p>Since PMD 6.21.0, implementations of this interface are considered internal
 * API and hence deprecated. Clients should exclusively use this interface.
 *
 * @see Rule
 */
public interface RuleViolation {
    // todo move to package reporting

    /**
     * Get the Rule which identified this violation.
     *
     * @return The identifying Rule.
     */
    RuleDescriptor getRule();

    /**
     * Get the description of this violation.
     *
     * @return The description.
     */
    String getDescription();


    /**
     * Get the source file name in which this violation was identified.
     *
     * @return The source file name.
     */
    String getFilename();

    /**
     * Get the begin line number in the source file in which this violation was
     * identified.
     *
     * @return Begin line number.
     */
    int getBeginLine();

    /**
     * Get the column number of the begin line in the source file in which this
     * violation was identified.
     *
     * @return Begin column number.
     */
    int getBeginColumn();

    /**
     * Get the end line number in the source file in which this violation was
     * identified.
     *
     * @return End line number.
     */
    int getEndLine();

    /**
     * Get the column number of the end line in the source file in which this
     * violation was identified.
     *
     * @return End column number.
     */
    int getEndColumn();

    /**
     * Get the package name of the Class in which this violation was identified.
     *
     * @return The package name.
     */
    // TODO Isn't this Java specific?
    String getPackageName();

    /**
     * Get the name of the Class in which this violation was identified.
     *
     * @return The Class name.
     */
    // TODO Isn't this Java specific?
    String getClassName();

    /**
     * Get the method name in which this violation was identified.
     *
     * @return The method name.
     */
    // TODO Isn't this Java specific?
    String getMethodName();

    /**
     * Get the variable name on which this violation was identified.
     *
     * @return The variable name.
     */
    String getVariableName();
}
