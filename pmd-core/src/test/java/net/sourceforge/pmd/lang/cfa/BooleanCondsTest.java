/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.cfa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import net.sourceforge.pmd.lang.cfa.exprs.EdgeCondition;
import net.sourceforge.pmd.lang.cfa.exprs.SymbolicExprFactory;


@RunWith(Parameterized.class)
public class BooleanCondsTest {

    private static final Object OBJ = new Object();
    private static final DefaultImplCond A = new DefaultImplCond("A");
    private static final DefaultImplCond B = new DefaultImplCond("B");

    @Parameter
    public EdgeCondition param;

    @Test
    public void testNegation() {
        // A != !A
        assertNotEquals(param, param.negate());
    }

    @Test
    public void testTRUEAbsorbsOR() {
        // (true || A) == true
        assertSame(EdgeCondition.TRUE, EdgeCondition.TRUE.or(param));
        assertSame(EdgeCondition.TRUE, param.or(EdgeCondition.TRUE));
    }

    @Test
    public void testFALSENeutralOR() {
        // (false || A) == A
        assertEquals(param, EdgeCondition.TRUE.negate().or(param));
        assertEquals(param, param.or(EdgeCondition.TRUE.negate()));
    }


    @Test
    public void testFALSEAbsorbsAND() {
        // (false && A) == false
        assertSame(EdgeCondition.TRUE.negate(), EdgeCondition.TRUE.negate().and(param));
        assertSame(EdgeCondition.TRUE.negate(), param.and(EdgeCondition.TRUE.negate()));
    }

    @Test
    public void testTRUENeutralAND() {
        // (true && A) == A
        assertEquals(param, EdgeCondition.TRUE.and(param));
        assertEquals(param, param.and(EdgeCondition.TRUE));
    }

    @Test
    public void testToStringPrecedence() {
        assertEquals("(A || B) && A", A.or(B).and(A).toString());
        assertEquals("A && B || B", A.and(B).or(B).toString());
    }

    @Test
    public void testNegationReduction() {
        // !(a || b) -> !a && !b
        assertEquals(param.negate().and(B.negate()), param.or(B).negate());
        // !(a && b) -> !a || !b
        assertEquals(param.negate().or(B.negate()), param.and(B).negate());

        // !!a -> a
        assertEquals(param, param.negate().negate());
    }

    @Parameters
    public static Collection<EdgeCondition> data() {

        SymbolicExprFactory<Object> factory = EdgeCondition.defaultFactory();
        return Arrays.asList(
            EdgeCondition.TRUE,
            EdgeCondition.TRUE.negate(),
            A,
            A.negate(),
            B,
            B.negate(),
            B.or(A),
            B.and(A),
            factory.ifPendingReturn(),
            factory.ifPendingThrow(),
            factory.pendingReturn(),
            factory.pendingThrow(),
            factory.catchMatches(OBJ),
            factory.catchMatches(OBJ).negate(),
            factory.iterableHasNext(OBJ),
            factory.iterableHasNext(OBJ).negate(),
            factory.equality(OBJ, OBJ),
            factory.equality(OBJ, OBJ).negate()
        );
    }

    static class DefaultImplCond implements EdgeCondition {

        private final String toString;

        DefaultImplCond(String toString) {
            this.toString = toString;
        }

        @Override
        public String toString() {
            return toString;
        }
    }


}
