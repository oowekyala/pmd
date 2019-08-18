/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.cfa

import net.sourceforge.pmd.lang.java.cfa.internal.CfgTestSpec

class CfgSwitchTests : CfgTestSpec(resourcePrefix = "switches", body =  {



    parserTest("Test empty switch") {

        cfgTest(ref = "emptySwitch") {
            """ 
            {
                before();
    
                switch (foo) {}
                
                after();
            }
            """
        }
    }

    parserTest("Test default") {


        cfgTest(ref = "withDefault") {
            """ 
            {
                before();
    
                switch (foo) {
                    case 1: case1();
                    case 2: case2(); break;
                    case 3: case3();
                    default: oha();
                }
                
                after();
            }
            """
        }
    }

    parserTest("Test no default") {


        cfgTest(ref = "noDefault") {
            """ 
            {
                before();
    
                switch (foo) {
                    case 1: case1();
                    case 2: case2(); break;
                    case 3: case3();
                }
                
                after();
            }
            """
        }
    }


})

