/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.lang.java.ast

import io.kotlintest.shouldBe
import net.sourceforge.pmd.lang.ast.test.firstToken
import net.sourceforge.pmd.lang.ast.test.shouldBe

// TODO merge the java ASTClassOrInterfaceDeclarationTest into this
class ASTClassOrInterfaceDeclTest : ParserTestSpec({

    parserTest("Local classes") {

        inContext(StatementParsingCtx) {

            """
               @F class Local {

               }
            """ should parseAs {

                child<ASTBlockStatement> {
                    classDecl(simpleName = "Local") {

                        it::isAbstract shouldBe false
                        it::isFinal shouldBe false
                        it::isLocal shouldBe true
                        it::isNested shouldBe false

                        annotation("F")
                        classBody {}
                    }
                }
            }

            """
               @F abstract @C class Local {

               }
            """ should parseAs {

                child<ASTBlockStatement> {
                    classDecl(simpleName = "Local") {
                        it::getDeclaredAnnotations shouldBe listOf(
                                annotation("F"),
                                annotation("C")
                        )

                        it::isAbstract shouldBe true
                        it::isFinal shouldBe false
                        it::isLocal shouldBe true
                        it::isNested shouldBe false

                        classBody {}
                    }
                }
            }
        }
    }


    parserTest("Normal classes") {

        inContext(TopLevelTypeDeclarationParsingCtx) {
            """
               public @F class Top {

               }
            """ should parseAs {

                classDecl(simpleName = "Top") {
                    it.firstToken.getImage() shouldBe "public"

                    annotation("F")
                    classBody {}
                }
            }
        }
    }


})
