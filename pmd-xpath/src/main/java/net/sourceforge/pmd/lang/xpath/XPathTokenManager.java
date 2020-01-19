/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath;

import java.io.Reader;

import net.sourceforge.pmd.lang.TokenManager;
import net.sourceforge.pmd.lang.ast.JavaCharStream;
import net.sourceforge.pmd.lang.xpath.ast.XPathParserTokenManager;


/**
 * VF Token Manager implementation.
 */
public class XPathTokenManager implements TokenManager {
    private final XPathParserTokenManager tokenManager;


    public XPathTokenManager(Reader source) {
        tokenManager = new XPathParserTokenManager(new JavaCharStream(source));
    }


    @Override
    public Object getNextToken() {
        return tokenManager.getNextToken();
    }


    @Override
    public void setFileName(String fileName) {
        XPathParserTokenManager.setFileName(fileName);
    }
}
