/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.internal.util;


import java.util.Collection;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class AssertionUtil {

    private static final Pattern PACKAGE_PATTERN = Pattern.compile("[\\w$]+(\\.[\\w$]+)*|");
    private static final Pattern BINARY_NAME_PATTERN = Pattern.compile("[\\w$]+(?:\\.[\\w$]+)*(?:\\[])*");

    private AssertionUtil() {
        // utility class
    }


    /** @throws NullPointerException if $name */
    public static void requireContainsNoNullValue(String name, Collection<?> c) {
        for (Object o : c) {
            if (o == null) {
                throw new NullPointerException(name + " contains null elements");
            }
        }
    }

    /** @throws IllegalArgumentException if empty */
    public static void requireNotEmpty(String name, Collection<?> c) {
        if (c.isEmpty()) {
            throw new IllegalArgumentException(name + " is empty");
        }
    }

    public static boolean isValidJavaPackageName(CharSequence name) {
        requireParamNotNull("name", name);
        return PACKAGE_PATTERN.matcher(name).matches();
    }

    /**
     * @throws IllegalArgumentException if the name is not a binary name
     */
    public static void assertValidJavaBinaryName(CharSequence name) {
        if (!isJavaBinaryName(name)) {
            throw new IllegalArgumentException("Not a Java binary name '" + name + "'");
        }
    }

    public static boolean isJavaBinaryName(CharSequence name) {
        return name.length() > 0 && BINARY_NAME_PATTERN.matcher(name).matches();
    }

    private static boolean isValidRange(int startInclusive, int endExclusive, int minIndex, int maxIndex) {
        return startInclusive <= endExclusive && minIndex <= startInclusive && endExclusive <= maxIndex;
    }

    private static String invalidRangeMessage(int startInclusive, int endExclusive, int minIndex, int maxIndex) {
        return "Invalid range [" + startInclusive + "," + endExclusive + "[ in [" + minIndex + "," + maxIndex + "[";
    }

    /**
     * @throws IllegalArgumentException if [startInclusive,endExclusive[ is
     *                                  not a valid substring range for the given string
     */
    public static void validateStringRange(CharSequence string, int startInclusive, int endExclusive) {
        if (!isValidRange(startInclusive, endExclusive, 0, string.length())) {
            throw new IllegalArgumentException(invalidRangeMessage(startInclusive, endExclusive, 0, string.length()));
        }
    }

    /**
     * Like {@link #validateStringRange(CharSequence, int, int)} but eliminated
     * at runtime if running without assertions.
     */
    public static void assertValidStringRange(CharSequence string, int startInclusive, int endExclusive) {
        assert isValidRange(startInclusive, endExclusive, 0, string.length())
            : invalidRangeMessage(startInclusive, endExclusive, 0, string.length());
    }

    /**
     * Returns true if the charsequence is a valid java identifier.
     *
     * @param name Name (non-null)
     *
     * @throws NullPointerException If the name is null
     */
    public static boolean isJavaIdentifier(CharSequence name) {
        int len = name.length();
        if (len == 0 || !Character.isJavaIdentifierStart(name.charAt(0))) {
            return false;
        }

        for (int i = 1; i < len; i++) {
            if (!Character.isJavaIdentifierPart(name.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    public static int requireOver1(String name, final int value) {
        if (value < 1) {
            throw mustBe(name, value, ">= 1");
        }
        return value;
    }


    /**
     * @throws IllegalArgumentException If value < 0
     */
    public static int requireNonNegative(String name, int value) {
        if (value < 0) {
            throw mustBe(name, value, "non-negative");
        }
        return value;
    }


    /**
     * @throws IndexOutOfBoundsException If value < 0
     */
    public static int requireIndexNonNegative(String name, int value) {
        if (value < 0) {
            throw mustBe(name, value, "non-negative", IndexOutOfBoundsException::new);
        }
        return value;
    }

    /**
     * @throws IndexOutOfBoundsException If value < 0 || value >= maxValue
     */
    public static int requireInNonNegativeRange(String name, int value, int maxValue) {
        return requireInExclusiveRange(name, value, 0, maxValue);
    }

    /**
     * @throws IndexOutOfBoundsException If value < 1 || value >= maxValue
     */
    public static int requireInPositiveRange(String name, int value, int maxValue) {
        return requireInExclusiveRange(name, value, 1, maxValue);
    }

    // the difference between those two is the message

    /**
     * @throws IndexOutOfBoundsException If {@code value < minValue || value > maxValue}
     */
    public static int requireInInclusiveRange(String name, int value, int minValue, int maxValue) {
        return requireInRange(name, value, minValue, maxValue, true);
    }

    /**
     * @throws IndexOutOfBoundsException If {@code value < minValue || value > maxValue}
     */
    public static int requireInExclusiveRange(String name, int value, int minValue, int maxValue) {
        return requireInRange(name, value, minValue, maxValue, false);
    }

    public static int requireInRange(String name, int value, int minValue, int maxValue, boolean inclusive) {
        if (value < 0 || inclusive && value > maxValue || !inclusive && value >= maxValue) {
            String message = "in range [" + minValue + "," + maxValue;
            message += inclusive ? "]" : "[";
            throw mustBe(name, value, message, IndexOutOfBoundsException::new);
        }
        return value;
    }

    public static RuntimeException mustBe(String name, Object value, String condition) {
        return mustBe(name, value, condition, IllegalArgumentException::new);
    }

    public static <E extends RuntimeException> E mustBe(String name, Object value, String condition, Function<String, E> exceptionMaker) {
        return exceptionMaker.apply(String.format("%s must be %s, got %s", name, condition, value));
    }

    public static <T> @NonNull T requireParamNotNull(String paramName, T obj) {
        if (obj == null) {
            throw new NullPointerException("Parameter " + paramName + " is null");
        }

        return obj;
    }

    public static @NonNull AssertionError shouldNotReachHere(String explanation) {
        return new AssertionError(makeMessage(explanation));
    }

    private static String makeMessage(String message) {
        String prefix = "This should be unreachable";
        return StringUtils.isBlank(message) ? prefix
                                            : prefix + ": " + message;
    }

    /**
     * An assertion error to emit when we catch an exception that we know can
     * never occur.
     */
    public static @NonNull AssertionError shouldNeverBeThrown(Throwable cause, String explanation) {
        return new AssertionError(makeMessage(explanation), cause);
    }
}
