/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.xpath.internal;

import java.util.function.BiPredicate;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.java.ast.Annotatable;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.ast.TypeNode;
import net.sourceforge.pmd.lang.java.symbols.JClassSymbol;
import net.sourceforge.pmd.lang.java.types.JTypeMirror;
import net.sourceforge.pmd.lang.java.types.TypeSystem;
import net.sourceforge.pmd.lang.java.types.TypeTestUtil;
import net.sourceforge.pmd.lang.java.types.TypesFromReflection;
import net.sourceforge.pmd.lang.rule.xpath.impl.XPathFunctionDefinition.Type;
import net.sourceforge.pmd.lang.rule.xpath.impl.XPathFunctionException;

/**
 * XPath function {@code pmd-java:typeIs(typeName as xs:string) as xs:boolean}
 * and {@code typeIsExactly}.
 *
 * <p>Example XPath 2.0: {@code //ClassType[pmd-java:typeIs('java.lang.String')]}
 *
 * <p>Returns true if the type of the node matches, false otherwise.
 */
public class BaseContextNodeTestFun<T extends JavaNode>
    extends BaseRewrittenFunction<JTypeMirror, T> {

    static final Type[] SINGLE_STRING_SEQ = {Type.SINGLE_STRING};
    private final TypeSystem ts;
    private final BiPredicate<JTypeMirror, T> checker;
    protected BaseContextNodeTestFun(TypeSystem ts, Class<T> klass, String localName, BiPredicate<JTypeMirror, T> checker) {
        super(localName, klass);
        this.ts = ts;
        this.checker = checker;
    }

    @Override
    protected JTypeMirror parseArgument(String arg) throws XPathFunctionException {
        return TypesFromReflection.loadType(ts, arg, unresolvedStore);
    }

    @Override
    protected boolean matches(T contextNode, String arg, JTypeMirror parsedArg, boolean isConstant) throws XPathFunctionException {
        return checker.test(parsedArg, contextNode);
    }

    @Override
    public FunctionCall makeCallExpression() {
        return (contextNode, arguments) -> {
            String fullTypeName = arguments[0].toString();
            return klass.isInstance(contextNode) && checker.test(fullTypeName, (T) contextNode);
        };
    }

    public static BaseJavaXPathFunction typeIsExactly(TypeSystem ts) {
        return new BaseContextNodeTestFun<>(ts, TypeNode.class, "typeIsExactly", TypeTestUtil::isExactlyA);
    }

    public static BaseJavaXPathFunction typeIs(TypeSystem ts) {
        return new BaseContextNodeTestFun<>(ts, TypeNode.class, "typeIs", TypeTestUtil::isA);
    }

    public static BaseJavaXPathFunction hasAnnotation(TypeSystem ts) {
        return new BaseContextNodeTestFun<>(ts, Annotatable.class, "hasAnnotation", (name, node) -> node.isAnnotationPresent(name));
    }

}
