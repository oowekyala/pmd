/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast

import io.kotest.matchers.shouldBe
import net.sourceforge.pmd.lang.ast.test.shouldBeA
import net.sourceforge.pmd.lang.java.ast.ParserTestSpec
import net.sourceforge.pmd.lang.java.types.JClassType


class JdocReferenceResolutionTests : ParserTestSpec({

    parserTest("Test class reference resolve") {

        val acu = parser.withProcessing().parse(
            """
                package here;
                import a.b.X;

                /**
                 * {@link Foo}
                 * {@link here.Foo}
                 */
                class Foo { }
            """.trimIndent()
        )

        val fooClass = acu.typeDeclarations.firstOrThrow()
        val comment = fooClass.javadocComment!!

        comment.jdocTree.shouldMatchComment {
            link {
                classRef("Foo") {
                    it.resolveRef() shouldBe fooClass.typeMirror
                }
            }
            link {
                classRef("here.Foo") {
                    it.resolveRef() shouldBe fooClass.typeMirror
                }
            }
        }

    }

    parserTest("Test class reference unresolved imported") {

        val acu = parser.withProcessing().parse(
            """
                package here;
                import a.b.X;

                /**
                 * {@link a.b.X}
                 */
                class Foo { }
            """.trimIndent()
        )

        val fooClass = acu.typeDeclarations.firstOrThrow()
        val comment = fooClass.javadocComment!!

        comment.jdocTree.shouldMatchComment {
            link {
                classRef("a.b.X") {
                    it.resolveRef().shouldBeA<JClassType> {
                        it.symbol.canonicalName.shouldBe("a.b.X")
                    }
                }
            }
        }

    }
    parserTest("Test class reference single char identifier") {

        val acu = parser.withProcessing().parse(
            """
                package here;
                import a.b.X;

                /**
                 * {@link X}
                 */
                class Foo { }
            """.trimIndent()
        )

        val fooClass = acu.typeDeclarations.firstOrThrow()
        val comment = fooClass.javadocComment!!

        comment.jdocTree.shouldMatchComment {
            link {
                classRef("a.b.X") {
                    it.resolveRef().shouldBeA<JClassType> {
                        it.symbol.canonicalName.shouldBe("a.b.X")
                    }
                }
            }
        }

    }
    parserTest("Test class reference unresolved") {

        val acu = parser.withProcessing().parse(
            """
                package here;
                import a.b.X;

                /**
                 * {@link b.X}
                 */
                class Foo { }
            """.trimIndent()
        )

        val fooClass = acu.typeDeclarations.firstOrThrow()
        val comment = fooClass.javadocComment!!

        comment.jdocTree.shouldMatchComment {
            link {
                classRef("b.X") {
                    it.resolveRef() shouldBe null
                }
            }
        }
    }
})
