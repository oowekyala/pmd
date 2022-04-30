/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ast.test

import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import net.sourceforge.pmd.lang.ast.Node
import net.sourceforge.pmd.lang.ast.TextAvailableNode
import kotlin.test.assertEquals


/**
 * Returns the text as a string. This is to allow
 * comparing the text to another string
 */
val TextAvailableNode.textStr: String
    get() = text.toString()

infix fun TextAvailableNode.shouldHaveText(str: String) {
    assertEquals(expected = str, actual = text.toString(), message = "Text is different")
}
inline fun <reified T : Node> Node.getDescendantsOfType(): List<T> = descendants(T::class.java).toList()
inline fun <reified T : Node> Node.getFirstDescendantOfType(): T = descendants(T::class.java).firstOrThrow()

fun Node.textOfReportLocation(): String? =
        reportLocation.regionInFile?.let(textDocument::sliceOriginalText)?.toString()


fun Node.assertTextRangeIsOk() {

    reportLocation shouldNotBe null

    val parent = parent ?: return

    if (this is TextAvailableNode && parent is TextAvailableNode) {
        parent.text.toString().shouldContain(this.text.toString())
    }
}


fun Node.assertBounds(bline: Int, bcol: Int, eline: Int, ecol: Int) {
    reportLocation.apply {
        this::getStartLine shouldBe bline
        this::getStartColumn shouldBe bcol
        this::getEndLine shouldBe eline
        this::getEndColumn shouldBe ecol
    }
}
