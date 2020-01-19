/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.pmd.lang.AbstractParser;
import net.sourceforge.pmd.lang.ParserOptions;
import net.sourceforge.pmd.lang.TokenManager;
import net.sourceforge.pmd.lang.ast.AbstractTokenManager;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.ast.ParseException;


/**
 * Adapter for the XPathParser.
 */
public class XPathParserAdapter extends AbstractParser {

    public XPathParserAdapter(ParserOptions parserOptions) {
        super(parserOptions);
    }


    @Override
    public TokenManager createTokenManager(Reader source) {
        return new XPathTokenManager(source);
    }


    @Override
    public boolean canParse() {
        return true;
    }


    @Override
    public Node parse(String fileName, Reader source) throws ParseException {
        AbstractTokenManager.setFileName(fileName);
        return new net.sourceforge.pmd.lang.xpath.ast.XPathParser(new XPathSimpleCharStream(source)).XPathRoot();
    }


    @Override
    public Map<Integer, String> getSuppressMap() {
        return new HashMap<>(); // FIXME
    }
}
