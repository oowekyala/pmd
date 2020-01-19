/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath;

import net.sourceforge.pmd.lang.AbstractPmdLanguageVersionHandler;
import net.sourceforge.pmd.lang.Parser;
import net.sourceforge.pmd.lang.ParserOptions;
import net.sourceforge.pmd.lang.xpath.ast.XPathParser;


public class XPathLanguageHandler extends AbstractPmdLanguageVersionHandler {


    @Override
    public Parser getParser(ParserOptions parserOptions) {
        return new XPathParser(parserOptions);
    }


}
