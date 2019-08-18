/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.cfa

import net.sourceforge.pmd.lang.java.cfa.internal.CfgTestSpec

/**
 * @author Clément Fournier
 */
class CfgExceptionHandlingTests : CfgTestSpec(
        resourcePrefix = "exceptions",
        defaultTestOpts = CfgDisplayOpts(showLocalErrorHandler = true),
        body =
        {

            parserTest("Test throw statement") {

                cfgTest(ref = "simpleThrow") {
                    "{ a(); throw new RuntimeException(); }"
                }
            }


            parserTest("Test assert statement") {
                cfgTest(ref = "assert") {
                    """{ before(); assert foo() : "message"; after(); }"""
                }

            }

            parserTest("Test assert statement no message") {
                cfgTest(ref = "assertNoMessage") {
                    "{ before(); assert foo(); after(); }"
                }
            }

            parserTest("Return in finally statement ") {
                cfgTest(ref = "returnInFinally") {
                    """ 
            { 
                before(); 
                try {
                
                    if (ifBody1()) return body();
                    if (ifBody2()) throw Body();
                    else doBody();
                
                } catch (Foo e) {
                    return catch1();
                } catch (Bar b) {
                    if (ifCatch2()) throw catch2();
                    else doCatch2();
                } finally {
                    if (ifFin())
                        return inFin();
                }
                
                after();
            }
            """
                }
            }

            parserTest("Finally statement ") {
                cfgTest(ref = "finallyBlock") {
                    """ 
                    { 
                        before(); 
                        try {
                        
                            if (if1()) return 2;
                            if (if2()) throw Foo();
                        
                        } catch (Foo e) {
                        
                            return 3;
                            
                        } catch (Bar e) {
                        
                            cleanup();
                        
                        } finally {
                            doSomething();
                        }
                        
                        after();
                    }
                    """
                }
            }
            parserTest("Try with resources ") {
                cfgTest(ref = "tryResources") {
                    """ 
                    { 
                        before(); 
                        try (Resource1 r1 = foo(); Res2 r2 = bar();){
                        
                            if (if1()) return 2;
                            if (if2()) throw Foo();
                        
                        } catch (Foo e) {
                        
                            return 3;
                            
                        } catch (Bar e) {
                        
                            cleanup();
                        
                        } finally {
                            doSomething();
                        }
                        
                        after();
                    }
                    """
                }
            }
            parserTest("Try with only resources") {
                cfgTest(ref = "tryOnlyResources") {
                    """ 
                    { 
                        before(); 
                        try (Resource1 r1 = foo(); Res2 r2 = bar();){
                        
                            if (if1()) return 2;
                            if (if2()) throw Foo();
                        
                        }
                        
                        after();
                    }
                    """
                }
            }
        })
