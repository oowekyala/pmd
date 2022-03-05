/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.metrics;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Map;

import org.junit.Test;

import net.sourceforge.pmd.lang.java.JavaParsingHelper;
import net.sourceforge.pmd.lang.java.ast.ASTAnyTypeDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.metrics.LanguageMetricsProvider;
import net.sourceforge.pmd.lang.metrics.Metric;


/**
 * @author Clément Fournier
 */
public class JavaMetricsProviderTest {

    private final JavaParsingHelper java8 = JavaParsingHelper.DEFAULT.withDefaultVersion("1.8");

    @Test
    public void testComputeAllMetrics() {

        LanguageMetricsProvider provider = java8.getHandler("1.8").getLanguageMetricsProvider();

        ASTCompilationUnit acu = java8.parse("class Foo { void bar() { System.out.println(1); } }");

        ASTAnyTypeDeclaration type = acu.getTypeDeclarations().firstOrThrow();

        Map<Metric<?, ?>, Number> results = provider.computeAllMetricsFor(type);

        assertEquals(9, results.size());
    }


    @Test
    public void testThereIsNoMemoisation() {

        LanguageMetricsProvider provider = java8.getHandler("1.8").getLanguageMetricsProvider();

        ASTAnyTypeDeclaration tdecl1 = java8.parse("class Foo { void bar() { System.out.println(1); } }")
                                            .getTypeDeclarations().firstOrThrow();

        Map<Metric<?, ?>, Number> reference = provider.computeAllMetricsFor(tdecl1);

        // same name, different characteristics
        ASTAnyTypeDeclaration tdecl2 = java8.parse("class Foo { void bar(){} \npublic void hey() { System.out.println(1); } }")
                                            .getTypeDeclarations().firstOrThrow();

        Map<Metric<?, ?>, Number> secondTest = provider.computeAllMetricsFor(tdecl2);

        assertNotEquals(reference, secondTest);

    }


}
