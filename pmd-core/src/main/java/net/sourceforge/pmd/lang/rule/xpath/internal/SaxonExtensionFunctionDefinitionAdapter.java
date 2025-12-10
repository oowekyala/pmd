/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.rule.xpath.internal;

import java.util.Collection;
import java.util.Optional;
import javax.xml.namespace.QName;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.rule.xpath.impl.XPathFunctionDefinition;
import net.sourceforge.pmd.lang.rule.xpath.impl.XPathFunctionException;
import net.sourceforge.pmd.util.AssertionUtil;
import net.sourceforge.pmd.util.CollectionUtil;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.EmptyAtomicSequence;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ValidationException;
import net.sf.saxon.value.BigDecimalValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.SequenceExtent;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

/**
 * Converts PMD's {@link XPathFunctionDefinition} into Saxon's {@link ExtensionFunctionDefinition}.
 */
public class SaxonExtensionFunctionDefinitionAdapter extends ExtensionFunctionDefinition {
    private static final SequenceType SINGLE_ELEMENT_SEQUENCE_TYPE = NodeKindTest.ELEMENT.one();
    private static final SequenceType OPTIONAL_ELEMENT_SEQUENCE_TYPE = NodeKindTest.ELEMENT.zeroOrOne();
    private static final SequenceType ELEMENT_SEQUENCE_SEQUENCE_TYPE = NodeKindTest.ELEMENT.zeroOrMore();

    private final XPathFunctionDefinition definition;

    public SaxonExtensionFunctionDefinitionAdapter(XPathFunctionDefinition definition) {
        this.definition = definition;
    }

    private SequenceType convertToSequenceType(XPathFunctionDefinition.Type type) {
        switch (type) {
        case SINGLE_STRING: return SequenceType.SINGLE_STRING;
        case SINGLE_BOOLEAN: return SequenceType.SINGLE_BOOLEAN;
        case SINGLE_ELEMENT: return SINGLE_ELEMENT_SEQUENCE_TYPE;
        case SINGLE_INTEGER: return SequenceType.SINGLE_INTEGER;
        case STRING_SEQUENCE: return SequenceType.STRING_SEQUENCE;
        case OPTIONAL_STRING: return SequenceType.OPTIONAL_STRING;
        case OPTIONAL_DECIMAL: return SequenceType.OPTIONAL_DECIMAL;
        case OPTIONAL_ELEMENT: return OPTIONAL_ELEMENT_SEQUENCE_TYPE;
        case ELEMENT_SEQUENCE: return ELEMENT_SEQUENCE_SEQUENCE_TYPE;
        }
        // should not occur, above switch is exhaustive
        throw AssertionUtil.shouldNotReachHere("Type " + type + " is not supported");
    }

    private SequenceType[] convertToSequenceTypes(XPathFunctionDefinition.Type[] types) {
        SequenceType[] result = new SequenceType[types.length];
        for (int i = 0; i < types.length; i++) {
            result[i] = convertToSequenceType(types[i]);
        }
        return result;
    }

    @Override
    public StructuredQName getFunctionQName() {
        QName qName = definition.getQName();
        return new StructuredQName(qName.getPrefix(), qName.getNamespaceURI(), qName.getLocalPart());
    }

    @Override
    public SequenceType[] getArgumentTypes() {
        return convertToSequenceTypes(definition.getArgumentTypes());
    }

    @Override
    public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
        return convertToSequenceType(definition.getResultType());
    }

    @Override
    public boolean dependsOnFocus() {
        return definition.dependsOnContext();
    }

    @Override
    public ExtensionFunctionCall makeCallExpression() {
        XPathFunctionDefinition.FunctionCall call = definition.makeCallExpression();
        return new ExtensionFunctionCall() {
            @Override
            public Expression rewrite(StaticContext context, Expression[] arguments) throws XPathException {
                Object[] convertedArguments = new Object[definition.getArgumentTypes().length];
                for (int i = 0; i < convertedArguments.length; i++) {
                    if (arguments[i] instanceof StringLiteral) {
                        convertedArguments[i] = ((StringLiteral) arguments[i]).getString().toString();
                    }
                }
                try {
                    call.staticInit(convertedArguments);
                } catch (XPathFunctionException e) {
                    XPathException xPathException = new XPathException(e);
                    xPathException.setIsStaticError(true);
                    throw xPathException;
                }
                return null;
            }

            @Override
            public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
                Node contextNode = null;
                if (definition.dependsOnContext()) {
                    contextNode = XPathElementToNodeHelper.itemToNode(context.getContextItem());
                }
                Object[] convertedArguments = new Object[definition.getArgumentTypes().length];
                for (int i = 0; i < convertedArguments.length; i++) {
                    convertedArguments[i] = convertSaxonToJava(arguments[i], definition.getArgumentTypes()[i]);
                }


                Object result;
                try {
                    result = call.call(contextNode, convertedArguments);
                } catch (XPathFunctionException e) {
                    throw new XPathException(e);
                }
                return convertJavaToSaxon(context, result);
            }
        };
    }

    private Object convertSaxonToJava(Sequence o, XPathFunctionDefinition.Type argTy) throws XPathException {
        switch (argTy) {
            case SINGLE_STRING:
                return o.head().getStringValue();
            case SINGLE_ELEMENT:
                return o.head();
        }
        throw new UnsupportedOperationException(
            "Don't know how to convert sequence " + o + " to " + argTy);
    }

    private Sequence convertJavaToSaxon(XPathContext context, Object o) throws ValidationException {
        switch (definition.getResultType()) {
            case SINGLE_BOOLEAN:
                return BooleanValue.get((Boolean) o);
            case SINGLE_INTEGER:
                return Int64Value.makeIntegerValue((Integer) o);
            case SINGLE_STRING:
                return new StringValue((String) o);
            case OPTIONAL_STRING:
                return o instanceof Optional && ((Optional<?>) o).isPresent()
                       ? new StringValue(((Optional<String>) o).get())
                       : EmptyAtomicSequence.getInstance();
            case STRING_SEQUENCE:
                if (o instanceof Collection) {
                    return SequenceExtent.makeSequenceExtent(CollectionUtil.map((Collection<String>) o, StringValue::new));
                }
                break;
            case OPTIONAL_DECIMAL:
                return o instanceof Optional && ((Optional<?>) o).isPresent()
                       ? new BigDecimalValue(((Optional<Double>) o).get())
                       : EmptySequence.getInstance();
            case OPTIONAL_ELEMENT:
                // Recognize Optional<Node> and @Nullable Node.
                Node node;
                if (o instanceof Optional) {
                    if (((Optional<?>) o).isPresent()) {
                        Object obj = ((Optional<?>) o).get();
                        if (obj instanceof Node) {
                            node = (Node) obj;
                        } else {
                            break;
                        }
                    } else {
                        node = null;
                    }
                } else if (o == null || o instanceof Node) {
                    node = (Node) o;
                } else {
                    break;
                }
                if (node == null) {
                    return EmptySequence.getInstance();
                }
                {
                    AstTreeInfo tree = ((AstElementNode) context.getContextItem()).getTreeInfo();
                    return tree.findWrapperFor(node);
                }
            case SINGLE_ELEMENT:
                if (o instanceof Node) {
                    AstTreeInfo tree = ((AstElementNode) context.getContextItem()).getTreeInfo();
                    return tree.findWrapperFor((Node) o);
                } else if (o == null) {
                    return EmptySequence.getInstance();
                }
                break;
            case ELEMENT_SEQUENCE:
                if (o instanceof Collection) {
                    AstTreeInfo tree = ((AstElementNode) context.getContextItem()).getTreeInfo();
                    return SequenceExtent.makeSequenceExtent(CollectionUtil.map((Collection<Node>) o, tree::findWrapperFor));
                }
                break;
            default:
                break;
        }
        String resultClass = o == null ? "" : " (" + o.getClass() + ")";
        throw new UnsupportedOperationException(
            "Don't know how to interpret object " + o + resultClass + " as XPath type "
            + definition.getResultType());
    }
}
