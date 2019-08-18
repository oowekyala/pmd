/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.cfa

import net.sourceforge.pmd.lang.java.cfa.internal.CfgTestSpec

/**
 * @author Clément Fournier
 */
class CfgLoopTests : CfgTestSpec(resourcePrefix = "loops", body = {

    parserTest("Test foreach") {

        cfgTest(ref = "foreach") {
            """ {
                before();
    
                for (Node foo : nodes) {
                    inLoop(foo);
                }
                
                after();
            }
            """
        }
    }


    parserTest("Test full for") {

        cfgTest(ref = "fullFor") {
            """ {
                before();
    
                for (int i = 0; i< 4; i++) {
                    inLoop(foo);
                }
                
                after();
            }
            """
        }
    }


    parserTest("Test for no init") {

        cfgTest(ref = "forNoInit") {
            """ {
                before();
    
                for (; i< 4; i++) {
                    inLoop(foo);
                }
                
                after();
            }
            """
        }
    }


    parserTest("Test for no update") {


        cfgTest(ref = "forNoUpdate") {
            """ 
            {
                before();
    
                for (; i< 4; ) {
                    inLoop(foo);
                }
                
                after();
            }
            """
        }
    }

    parserTest("Test for no condition") {

        cfgTest(ref = "forNoCondition") {
            """ 
            {
                before();
    
                for (; ;i++) {
                    inLoop(foo);
                }
                
                after();
            }
            """
        }
    }

    parserTest("Test while loop") {

        cfgTest(ref = "while") {
            """ 
            {
                before();
    
                while (i++ > 0) {
                    inLoop(foo);
                }
                
                after();
            }
            """
        }
    }


    parserTest("Test do loop") {

        cfgTest(ref = "doLoop") {
            """ 
            {
                before();
    
                do {
                    inLoop(foo);
                } while (i++ > 0);
                
                after();
            }
            """
        }
    }

})

