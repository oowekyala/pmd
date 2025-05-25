/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import net.sourceforge.pmd.lang.ast.test.shouldBe
import net.sourceforge.pmd.lang.ast.test.shouldHaveText
import net.sourceforge.pmd.lang.java.ast.methodRef


class JdocInlineTagParserTests : JdocParserTestSpec({

    parserTest("Test @link inline tags") {


        """
        /**
         * See {@link #hey}
         */
        """.trimIndent() should parseAsJdoc {
            it shouldHaveText "/**\n * See {@link #hey}\n */" 

            data("See ")
            link {
                it shouldHaveText "{@link #hey}" 
                it::getRef shouldBe fieldRef("hey") {
                    it shouldHaveText "#hey" 
                    emptyClassRef()
                }
            }
        }


        """
        /**
         * See {@link A#method(B, C)}
         */
        """.trimIndent() should parseAsJdoc {
            data("See ")
            link {
                it::getRef shouldBe jdocMethodRef("method") {
                    classRef("A")
                    classRef("B")
                    classRef("C")
                }
            }
        }

        """
        /**
         * See {@link Oha#hey label label}
         */
        """.trimIndent() should parseAsJdoc {
            it shouldHaveText "/**\n * See {@link Oha#hey label label}\n */" 

            data("See ")
            link {
                it shouldHaveText "{@link Oha#hey label label}" 
                it::getRef shouldBe fieldRef("hey") {
                    it shouldHaveText "Oha#hey" 
                    classRef("Oha") {
                        it shouldHaveText "Oha" 
                    }
                }

                it.label.toString() shouldBe "label label"
                data("label label")
            }
        }

        // malformed

        """
        /**
         * See {@link Oha# label label}
         */
        """.trimIndent() should parseAsJdoc {
            it shouldHaveText "/**\n * See {@link Oha# label label}\n */" 

            data("See ")
            link {
                it shouldHaveText "{@link Oha# label label}" 
                it::getRef shouldBe classRef("Oha") {
                    it shouldHaveText "Oha#" 

                    malformed {
                        it.message.shouldContain("Unexpected token '#'")
                    }
                }

                it.label.toString() shouldBe "label label"
                data("label label")
            }
        }

        """
        /**
         * See {@link }
         */
        """.trimIndent() should parseAsJdoc {
            it shouldHaveText "/**\n * See {@link }\n */" 

            data("See ")
            link {
                it shouldHaveText "{@link }" 
                // TODO malformed?
                it::getRef shouldBe null
                it.label?.toString() shouldBe null
            }
        }

        """
        /**
         * See {@link}
         */
        """.trimIndent() should parseAsJdoc {
            it shouldHaveText "/**\n * See {@link}\n */" 

            data("See ")
            link {
                it shouldHaveText "{@link}" 
                // TODO malformed?
                it::getRef shouldBe null
                it.label?.toString() shouldBe null
            }
        }


    }


    parserTest("Test @value inline tags") {

        """
        /**
         * See {@value #hey}
         */
        """.trimIndent() should parseAsJdoc {
            it shouldHaveText "/**\n * See {@value #hey}\n */" 

            data("See ")
            value {
                it shouldHaveText "{@value #hey}" 
                it::getRef shouldBe fieldRef("hey") {
                    it shouldHaveText "#hey" 
                    emptyClassRef()
                }
            }
        }


        // malformed

        """
        /**
         * See {@value Oha#hey label label}
         */
        """.trimIndent() should parseAsJdoc {
            it shouldHaveText "/**\n * See {@value Oha#hey label label}\n */" 

            data("See ")
            value {
                it shouldHaveText "{@value Oha#hey label label}" 
                it::getRef shouldBe fieldRef("hey") {
                    it shouldHaveText "Oha#hey" 
                    classRef("Oha") {
                        it shouldHaveText "Oha" 
                    }
                }

                malformed {
                    it shouldHaveText "label label"
                }
            }
        }


        """
        /**
         * See {@value }
         */
        """.trimIndent() should parseAsJdoc {
            it shouldHaveText "/**\n * See {@value }\n */" 

            data("See ")
            value {
                it shouldHaveText "{@value }" 
                // TODO malformed?
                it::getRef shouldBe null
            }
        }


    }


    parserTest("Test @code inline tag") {


        """
        /**
         * Param {@code <T>} is no {@code
         *   <R>
         * }
         */
        """.trimIndent() should parseAsJdoc {

            data("Param ")
            code("<T>")
            data(" is no ")
            code("<R>")
        }


    }


    parserTest("Test unknown inline tags") {

        """
        /**
         * See {@cobalt #hey}
         */
        """.trimIndent() should parseAsJdoc {
            data("See ")
            unknownInline("@cobalt") {
                it shouldHaveText "{@cobalt #hey}" 
                it::getData shouldBe "#hey"
            }
        }
    }

})
