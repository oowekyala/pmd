/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast

import io.kotest.matchers.should
import io.kotest.matchers.string.shouldContain
import net.sourceforge.pmd.lang.ast.test.shouldBe
import net.sourceforge.pmd.lang.java.ast.ProcessorTestSpec


class JdocInlineTagParserTests : ProcessorTestSpec({
    /*
        TODO tests:
         - html comments
         - method reference
         - block tags
    */

    jdocParserTest("Test @link inline tags") {


        """
        /**
         * See {@link #hey}
         */
        """.trimIndent() should parseAsJdoc {
            it::getText shouldBe "/**\n * See {@link #hey}\n */"

            data("See ")
            link {
                it::getText shouldBe "{@link #hey}"
                it::getRef shouldBe fieldRef("hey") {
                    it::getText shouldBe "#hey"
                    emptyClassRef()
                }
            }
        }


        """
        /**
         * See {@link Oha#hey label label}
         */
        """.trimIndent() should parseAsJdoc {
            it::getText shouldBe "/**\n * See {@link Oha#hey label label}\n */"

            data("See ")
            link {
                it::getText shouldBe "{@link Oha#hey label label}"
                it::getRef shouldBe fieldRef("hey") {
                    it::getText shouldBe "Oha#hey"
                    classRef("Oha") {
                        it::getText shouldBe "Oha"
                    }
                }

                it::getLabel shouldBe "label label"
                data("label label")
            }
        }

        // malformed

        """
        /**
         * See {@link Oha# label label}
         */
        """.trimIndent() should parseAsJdoc {
            it::getText shouldBe "/**\n * See {@link Oha# label label}\n */"

            data("See ")
            link {
                it::getText shouldBe "{@link Oha# label label}"
                it::getRef shouldBe classRef("Oha") {
                    it::getText shouldBe "Oha#"

                    malformed {
                        it.message.shouldContain("Unexpected token #")
                    }
                }

                it::getLabel shouldBe "label label"
                data("label label")
            }
        }

        """
        /**
         * See {@link }
         */
        """.trimIndent() should parseAsJdoc {
            it::getText shouldBe "/**\n * See {@link }\n */"

            data("See ")
            link {
                it::getText shouldBe "{@link }"
                // TODO malformed?
                it::getRef shouldBe null
                it::getLabel shouldBe null
            }
        }

        """
        /**
         * See {@link}
         */
        """.trimIndent() should parseAsJdoc {
            it::getText shouldBe "/**\n * See {@link}\n */"

            data("See ")
            link {
                it::getText shouldBe "{@link}"
                // TODO malformed?
                it::getRef shouldBe null
                it::getLabel shouldBe null
            }
        }


    }


    jdocParserTest("Test @value inline tags") {

        """
        /**
         * See {@value #hey}
         */
        """.trimIndent() should parseAsJdoc {
            it::getText shouldBe "/**\n * See {@value #hey}\n */"

            data("See ")
            value {
                it::getText shouldBe "{@value #hey}"
                it::getRef shouldBe fieldRef("hey") {
                    it::getText shouldBe "#hey"
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
            it::getText shouldBe "/**\n * See {@value Oha#hey label label}\n */"

            data("See ")
            value {
                it::getText shouldBe "{@value Oha#hey label label}"
                it::getRef shouldBe fieldRef("hey") {
                    it::getText shouldBe "Oha#hey"
                    classRef("Oha") {
                        it::getText shouldBe "Oha"
                    }
                }

                malformed {
                    it::getText shouldBe "label"
                }
            }
        }


        """
        /**
         * See {@value }
         */
        """.trimIndent() should parseAsJdoc {
            it::getText shouldBe "/**\n * See {@value }\n */"

            data("See ")
            value {
                it::getText shouldBe "{@value }"
                // TODO malformed?
                it::getRef shouldBe null
            }
        }


    }


    jdocParserTest("Test @code inline tag") {


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


    jdocParserTest("Test unknown inline tags") {

        """
        /**
         * See {@cobalt #hey}
         */
        """.trimIndent() should parseAsJdoc {
            data("See ")
            unknownInline("@cobalt") {
                it::getText shouldBe "{@cobalt #hey}"
                it::getData shouldBe "#hey"
            }
        }
    }

})
