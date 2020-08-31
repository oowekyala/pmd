/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang;


import java.util.function.Supplier;

import net.sourceforge.pmd.cpd.Tokenizer;
import net.sourceforge.pmd.cpd.Tokenizer.CpdProperties;

/**
 * Base language version handler for languages that only support CPD.
 *
 * @author Clément Fournier
 * @since 6.10.0
 */
public class CpdOnlyHandler extends AbstractLanguageVersionHandler {

    private final Supplier<Tokenizer> tokenizerMaker;

    public CpdOnlyHandler(Supplier<Tokenizer> tokenizerMaker) {
        this.tokenizerMaker = tokenizerMaker;
    }

    @Override
    public Tokenizer newCpdTokenizer() {
        return tokenizerMaker.get();
    }

    @Override
    public Parser getParser(ParserOptions parserOptions) {
        throw new UnsupportedOperationException("I don't support parsing");
    }
}
