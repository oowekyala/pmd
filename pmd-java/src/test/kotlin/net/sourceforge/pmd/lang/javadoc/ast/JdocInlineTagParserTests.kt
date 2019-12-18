/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast

import io.kotlintest.matchers.string.shouldContain
import net.sourceforge.pmd.lang.ast.test.shouldBe


class JdocInlineTagParserTests : JavadocParserSpec({
    /*
        TODO tests:
         - html comments
         - method reference
         - block tags
    */

    parserTest("Test @link inline tags") {


        """
        /**
         * See {@link #hey}
         */
        """.trimIndent() should parseAs {
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
        """.trimIndent() should parseAs {
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
        """.trimIndent() should parseAs {
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
        """.trimIndent() should parseAs {
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
        """.trimIndent() should parseAs {
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


    parserTest("Test @value inline tags") {

        """
        /**
         * See {@value #hey}
         */
        """.trimIndent() should parseAs {
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
        """.trimIndent() should parseAs {
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
        """.trimIndent() should parseAs {
            it::getText shouldBe "/**\n * See {@value }\n */"

            data("See ")
            value {
                it::getText shouldBe "{@value }"
                // TODO malformed?
                it::getRef shouldBe null
            }
        }


    }


    parserTest("Test some inline tags") {


        """
        /**
         * Param {@code <T>} is no {@code
         *   <R>
         * }
         */
        """.trimIndent() should parseAs {

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
        """.trimIndent() should parseAs {
            data("See ")
            unknownInline("@cobalt") {
                it::getText shouldBe "{@cobalt #hey}"
                it::getData shouldBe "#hey"
            }
        }
    }

})
