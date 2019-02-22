package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.Matcher
import io.kotlintest.matchers.ToleranceMatcher
import io.kotlintest.should
import io.kotlintest.shouldBe as ktShouldBe

infix fun Double.plusOrMinus(tolerance: Double): ToleranceMatcher = ToleranceMatcher(this, tolerance)

/**
 * Assert a matcher on all elements of a list.
 */
infix fun <T> List<T>.shouldAll(matcher: Matcher<T>) = this.forEach { it should matcher }
