/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath;

import java.io.Writer;

import net.sourceforge.pmd.lang.AbstractLanguageVersionHandler;
import net.sourceforge.pmd.lang.Parser;
import net.sourceforge.pmd.lang.ParserOptions;
import net.sourceforge.pmd.lang.VisitorStarter;
import net.sourceforge.pmd.lang.XPathHandler;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.ast.xpath.DefaultASTXPathHandler;
import net.sourceforge.pmd.lang.rule.RuleViolationFactory;
import net.sourceforge.pmd.lang.xpath.ast.ASTXPathRoot;
import net.sourceforge.pmd.lang.xpath.rule.XPathRuleViolationFactory;
import net.sourceforge.pmd.lang.xpath.symboltable.SymbolTableFacade;


public class XPathLanguageHandler extends AbstractLanguageVersionHandler {

    @Override
    public XPathHandler getXPathHandler() {
        return new DefaultASTXPathHandler();
    }


    @Override
    public VisitorStarter getSymbolFacade() {
        return new VisitorStarter() {
            @Override
            public void start(Node rootNode) {
                SymbolTableFacade.runOn((ASTXPathRoot) rootNode);
            }
        };
    }


    @Override
    public RuleViolationFactory getRuleViolationFactory() {
        return XPathRuleViolationFactory.INSTANCE;
    }


    @Override
    public Parser getParser(ParserOptions parserOptions) {
        return new XPathParserAdapter(parserOptions);
    }


    @Override
    public VisitorStarter getDumpFacade(final Writer writer, final String prefix, final boolean recurse) {
        return VisitorStarter.DUMMY; // FIXME
    }
}
