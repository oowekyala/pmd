/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import java.io.StringReader;

import net.sourceforge.pmd.lang.xpath.XPathSimpleCharStream;


/**
 * Synthesises nodes.
 *
 * @author Clément Fournier
 * @since 6.7.0
 */
final class SyntheticNodeFactory {


    private SyntheticNodeFactory() {

    }


    public static ASTAxisStep synthesizeAxisStep(String expr) {
        return getParser(expr).AxisStep();
    }


    public static NodeTest synthesizeNodeTest(String test) {
        return getParser(test).NodeTest();
    }


    public static ASTSequenceType synthesizeSequenceType(String type) {
        return getParser(type).SequenceType();
    }


    private static XPathParser getParser(String source) {
        return new XPathParser(new XPathSimpleCharStream(new StringReader(source)));
    }


}
