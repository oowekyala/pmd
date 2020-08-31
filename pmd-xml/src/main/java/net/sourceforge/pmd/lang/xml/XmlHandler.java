/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xml;

import net.sourceforge.pmd.cpd.Tokenizer;
import net.sourceforge.pmd.lang.AbstractPmdLanguageVersionHandler;
import net.sourceforge.pmd.lang.Parser;
import net.sourceforge.pmd.lang.ParserOptions;
import net.sourceforge.pmd.lang.xml.cpd.XmlTokenizer;

/**
 * Implementation of LanguageVersionHandler for the XML.
 */
public class XmlHandler extends AbstractPmdLanguageVersionHandler {

    @Override
    public Tokenizer newCpdTokenizer() {
        return new XmlTokenizer();
    }

    @Override
    public ParserOptions getDefaultParserOptions() {
        return new XmlParserOptions();
    }

    @Override
    public Parser getParser(ParserOptions parserOptions) {
        return new XmlParser((XmlParserOptions) parserOptions);
    }

}
