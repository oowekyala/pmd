/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import java.io.Reader;

import net.sourceforge.pmd.lang.TokenManager;
import net.sourceforge.pmd.lang.ast.impl.javacc.CharStreamFactory;


/**
 * VF Token Manager implementation.
 */
public class XPathTokenManager implements TokenManager {

    private final XPathParserImplTokenManager tokenManager;


    public XPathTokenManager(Reader source) {
        tokenManager = new XPathParserImplTokenManager(CharStreamFactory.simpleCharStream(source));
    }


    @Override
    public Object getNextToken() {
        return tokenManager.getNextToken();
    }


    @Override
    public void setFileName(String fileName) {
        XPathParserImplTokenManager.setFileName(fileName);
    }
}
