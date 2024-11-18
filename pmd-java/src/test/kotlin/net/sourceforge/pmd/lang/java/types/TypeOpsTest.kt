/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.types

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.property.Arb
import io.kotest.property.arbitrary.shuffle
import io.kotest.property.checkAll
import net.sourceforge.pmd.lang.java.symbols.internal.asm.createUnresolvedAsmSymbol

/**
 * @author Clément Fournier
 */
class TypeOpsTest : FunSpec({

    with(TypeDslOf(testTypeSystem)) { // import construction DSL
        with(gen) { // import constants


            // for any permutation of input, the output should be the same
            suspend fun checkMostSpecific(input: List<JTypeMirror>, output: List<JTypeMirror>) {

                checkAll(Arb.shuffle(input)) { ts ->
                    TypeOps.mostSpecific(ts).shouldContainExactlyInAnyOrder(output)
                }
            }

            test("Test most specific") {

                checkAll(ts.subtypesArb()) { (t, s) ->
                    TypeOps.mostSpecific(setOf(t, s)).shouldContainExactly(t)
                }
            }

            test("Test most when types are equal") {

                checkMostSpecific(
                        input = listOf(t_AbstractList, t_AbstractList),
                        output = listOf(t_AbstractList))
            }

            test("Test most specific of unresolved types") {
                val tA = ts.declaration(ts.createUnresolvedAsmSymbol("a.A"))
                val tB = ts.declaration(ts.createUnresolvedAsmSymbol("a.B"))

                checkMostSpecific(
                    input = listOf(tA, tB),
                    output = listOf(tA, tB)
                )
            }

            test("Test most specific unchecked") {


                checkMostSpecific(
                        input = listOf(t_List, `t_List{?}`),
                        output = listOf(`t_List{?}`))

                checkMostSpecific(
                        input = listOf(t_List, `t_List{?}`, `t_List{Integer}`),
                        output = listOf(`t_List{Integer}`))

                checkMostSpecific(
                        input = listOf(t_List, `t_List{?}`, `t_List{Integer}`, `t_List{String}`),
                        output = listOf(`t_List{String}`, `t_List{Integer}`))

            }

            test("Bug #5029: Recursive type projection") {

                val (acu, spy) = javaParser.parseWithTypeInferenceSpy(
                    """
                       class Job<J extends Job<J, R>, R extends Run<J, R>> {
                            J getLast() { return null; }
                       }
                       class Run<J extends Job<J, R>, R extends Run<J, R>> {}
                       
                       class Foo {
                         static Job<?, ?> foo(Job<?,?> job) {
                            var x = job.getLast();
                            return x;
                         }
                       }
                    """.trimIndent()
                )
                val (jobt, runt) = acu.declaredTypeSignatures()
                val xVar = acu.varId("x")
                spy.shouldBeOk {
                    xVar shouldHaveType jobt[`?` extends jobt[`?`, `?` extends runt[`?`, `?`]], `?`]
                }


            }
            test("#5167 problem in projection") {
                val (acu, spy) = javaParser.parseWithTypeInferenceSpy(
                    """
import java.lang.annotation.Annotation;
interface Bar<T> {
    Baz<T> getBaz();
}

interface Predicate<T> {
    boolean check(T t);
}
interface Stream<T>{
    T findSome();
}
interface Baz<T>{
    Stream<Bar<? super T>> filterMethods(Predicate<? super T> p);
}

class Foo {

    private static Bar<?> foo(
        Bar<?> type, Class<? extends Annotation> annotation, boolean required) {
        var method = type.getBaz().filterMethods(m -> true).findSome();
        return method;
    }
}
            """.trimIndent()
                )

                val (barT) = acu.declaredTypeSignatures()
                val methodId = acu.varId("method")

                spy.shouldBeOk {
                    methodId shouldHaveType barT[`?`]
                }
            }
        }
    }


})
