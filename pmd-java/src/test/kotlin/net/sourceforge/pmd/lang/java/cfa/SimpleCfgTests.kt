/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.cfa

import net.sourceforge.pmd.lang.java.cfa.internal.CfgTestSpec

/**
 * @author Clément Fournier
 */
class SimpleCfgTests : CfgTestSpec(resourcePrefix = "simpleTests", body = {

    parserTest("Test flat CFG") {
        cfgTest(ref = "flatCfg") {
            "{ int a = 0; a++;}"
        }
    }
    parserTest("Test empty block") {
        // not a typo
        cfgTest(ref = "flatCfg") {
            "{ int a = 0; {} a++; }"
        }
    }

    parserTest("Test if stmt") {
        cfgTest(ref = "ifStmt") {
            """ 
            {
                before();
    
                if (foo()) {
                    inThen();
                    inThen2();
                }
                
                after();
            }
            """
        }

    }

    parserTest("Test if-else stmt") {

        cfgTest(ref = "ifElseStmt") {
            """ 
            {
                before();
    
                if (foo()) {
                    inThen();
                    inThen2();
                } else {
                    bar();
                }
                
                after();
            }
            """
        }

    }

})

