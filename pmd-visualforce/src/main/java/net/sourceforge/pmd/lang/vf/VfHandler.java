/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.vf;

import net.sourceforge.pmd.cpd.Tokenizer;
import net.sourceforge.pmd.lang.AbstractPmdLanguageVersionHandler;
import net.sourceforge.pmd.lang.Parser;
import net.sourceforge.pmd.lang.ParserOptions;
import net.sourceforge.pmd.lang.vf.ast.VfParser;
import net.sourceforge.pmd.lang.vf.cpd.VfTokenizer;

public class VfHandler extends AbstractPmdLanguageVersionHandler {

    @Override
    public Tokenizer newCpdTokenizer() {
        return new VfTokenizer();
    }

    @Override
    public Parser getParser(ParserOptions parserOptions) {
        return new VfParser();
    }

}
