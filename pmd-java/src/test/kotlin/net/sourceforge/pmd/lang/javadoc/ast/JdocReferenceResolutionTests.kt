/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast

import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import net.sourceforge.pmd.lang.ast.test.shouldBe
import net.sourceforge.pmd.lang.ast.test.shouldHaveText
import net.sourceforge.pmd.lang.java.ast.JavadocComment
import net.sourceforge.pmd.lang.java.ast.JavadocCommentOwner
import net.sourceforge.pmd.lang.java.ast.ParserTestSpec


class JdocReferenceResolutionTests : ParserTestSpec({

    parserTest("Test class reference resolve") {

        val acu = parser.parse(
            """
                package here;
                import a.b.X;

                /**
                 * {@link Foo}
                 * {@link here.Foo}
                 * {@link X}
                 * {@link a.b.X}
                 * {@link b.X}
                 */
                class Foo { }
            """.trimIndent()
        )

        val comment = acu.descendants(JavadocCommentOwner::class.java).firstOrThrow().javadocComment!!
        val fooClass = acu.typeDeclarations.firstOrThrow()

        comment.jdocTree.shouldMatchComment {
            jdoc {
                link {
                    classRef("Foo") {
                        it.resolveRef() shouldBe fooClass.typeMirror
                    }
                }
            }
        }

    }

})
