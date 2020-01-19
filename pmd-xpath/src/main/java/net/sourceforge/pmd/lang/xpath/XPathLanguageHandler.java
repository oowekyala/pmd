/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath;

import net.sourceforge.pmd.lang.AbstractPmdLanguageVersionHandler;
import net.sourceforge.pmd.lang.Parser;
import net.sourceforge.pmd.lang.ParserOptions;
import net.sourceforge.pmd.lang.XPathHandler;
import net.sourceforge.pmd.lang.ast.xpath.DefaultASTXPathHandler;
import net.sourceforge.pmd.lang.rule.RuleViolationFactory;
import net.sourceforge.pmd.lang.xpath.rule.XPathRuleViolationFactory;


public class XPathLanguageHandler extends AbstractPmdLanguageVersionHandler {

    @Override
    public XPathHandler getXPathHandler() {
        return new DefaultASTXPathHandler();
    }


    @Override
    public RuleViolationFactory getRuleViolationFactory() {
        return XPathRuleViolationFactory.INSTANCE;
    }


    @Override
    public Parser getParser(ParserOptions parserOptions) {
        return new XPathParserAdapter(parserOptions);
    }


}
