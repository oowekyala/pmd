/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.lang.java.cfa.dfa

import net.sourceforge.pmd.lang.java.cfa.internal.DfaTestSpec

/**
 * @author Clément Fournier
 */
class SimpleDfaTests : DfaTestSpec(resourcePrefix = "simple", body = {

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

    parserTest("f:Test if stmt") {
        cfgTest(ref = "ifStmt") {
            """ 
            {
                before();
                
                Object a = fetch();
    
                if (foo()) {
                   a = c.inThen(a);
                   a = a.inThen2(b);
                }
                
                a.after();
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

