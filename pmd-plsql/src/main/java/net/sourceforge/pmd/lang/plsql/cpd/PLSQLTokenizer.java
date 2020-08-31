/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.plsql.cpd;

import net.sourceforge.pmd.cpd.Tokenizer;
import net.sourceforge.pmd.cpd.internal.JavaCCTokenizer;
import net.sourceforge.pmd.lang.TokenManager;
import net.sourceforge.pmd.lang.ast.impl.javacc.JavaccToken;
import net.sourceforge.pmd.lang.ast.impl.javacc.io.CharStream;
import net.sourceforge.pmd.lang.plsql.ast.PLSQLTokenKinds;

public class PLSQLTokenizer extends JavaCCTokenizer {

    private boolean ignoreIdentifiers;
    private boolean ignoreLiterals;

    @Override
    public void setProperties(CpdProperties cpdProperties) {
        ignoreIdentifiers = cpdProperties.getProperty(Tokenizer.IGNORE_IDENTIFIERS);
        ignoreLiterals = cpdProperties.getProperty(Tokenizer.IGNORE_LITERALS);
    }

    public void setIgnoreLiterals(boolean ignore) {
        this.ignoreLiterals = ignore;
    }

    public void setIgnoreIdentifiers(boolean ignore) {
        this.ignoreIdentifiers = ignore;
    }

    @Override
    protected String getImage(JavaccToken plsqlToken) {
        String image = plsqlToken.getImage();

        if (ignoreIdentifiers && plsqlToken.kind == PLSQLTokenKinds.IDENTIFIER) {
            image = String.valueOf(plsqlToken.kind);
        }

        if (ignoreLiterals && (plsqlToken.kind == PLSQLTokenKinds.UNSIGNED_NUMERIC_LITERAL
            || plsqlToken.kind == PLSQLTokenKinds.FLOAT_LITERAL
            || plsqlToken.kind == PLSQLTokenKinds.INTEGER_LITERAL
            || plsqlToken.kind == PLSQLTokenKinds.CHARACTER_LITERAL
            || plsqlToken.kind == PLSQLTokenKinds.STRING_LITERAL
            || plsqlToken.kind == PLSQLTokenKinds.QUOTED_LITERAL)) {
            image = String.valueOf(plsqlToken.kind);
        }
        return image;
    }

    @Override
    protected TokenManager<JavaccToken> makeLexerImpl(CharStream sourceCode) {
        return PLSQLTokenKinds.newTokenManager(sourceCode);
    }
}
