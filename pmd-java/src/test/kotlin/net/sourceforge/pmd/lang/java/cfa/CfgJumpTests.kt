/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.cfa

import net.sourceforge.pmd.lang.java.ast.ParserTestCtx
import net.sourceforge.pmd.lang.java.cfa.internal.CfgTestSpec

/**
 * @author Clément Fournier
 */
class CfgJumpTests : CfgTestSpec(resourcePrefix = "jumps", body = {

    parserTest("Test simple labeled loop") {

        cfgTest(ref = "simpleLabel") {
            """ 
            {
                before();
    
                a:
                for (Node foo : nodes) {
                    inLoop(foo);
                    if (foo.isIt) {
                        break a;
                    }
                }
                
                after();
            }
            """
        }
    }

    fun ParserTestCtx.nestedLabelLoopTest(outLabel: String, inLabel: String, inBreak: String) {

        cfgTest(ref = "breakLabel", vars = mapOf("inBreak" to inBreak)) {
            """
            {
                before();

                $outLabel
                for (Node foo : nodes) {
                    inLoop1(foo);
                    $inLabel
                    for (Node foo2: nodes) {        
                        if (foo.isIt) {
                            break a;
                        } else {
                            $inBreak; // should jump to b
                        }
                    }
                    inLoop1(foo);
                    if (foo.isIt) {
                        break a;
                    }
                }

                after();
            }
            """
        }
    }

    parserTest("Test simple break jumps to innermost when unlabeled ") {
        nestedLabelLoopTest(outLabel = "a:", inLabel = "", inBreak = "break")
    }


    parserTest("Test simple break jumps to innermost loop even if labeled ") {
        nestedLabelLoopTest(outLabel = "a:", inLabel = "b:", inBreak = "break")
    }

    parserTest("Test break jumps to named target") {
        nestedLabelLoopTest(outLabel = "a:", inLabel = "b:", inBreak = "break b")
    }


    parserTest("Test dangling labels") {

        cfgTest("dangling") {
            """ 
            {
                before();
    
                for (Node foo : nodes) {
                    inLoop(foo);
                    if (foo.isIt) {
                        bar();
                        break oha;
                    } else if (shouldContinue()) {
                        foo();
                        continue oha;
                    }
                }
                
                after();
            }
            """
        }
    }

})

