/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast

import net.sourceforge.pmd.lang.javadoc.ast.data
import net.sourceforge.pmd.lang.javadoc.ast.shouldMatchComment

class JavadocTest : ParserTestSpec({

    parserTest("Normal classes") {

        inContext(TopLevelTypeDeclarationParsingCtx) {
            """
               /**
                * sacramento
                */
               public @F class Top {
               
               }
            """ should parseAs {

                classDecl(simpleName = "Top") {
                    it.javadocComment.shouldMatchComment {
                        data("sacramento")
                    }

                    annotation("F")
                    typeBody {}
                }
            }
        }
    }


})
