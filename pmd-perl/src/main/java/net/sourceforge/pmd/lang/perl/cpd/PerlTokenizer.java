/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.perl.cpd;

import java.util.ArrayList;

import net.sourceforge.pmd.cpd.AbstractTokenizer;

public class PerlTokenizer extends AbstractTokenizer {
    public PerlTokenizer() {
        this.stringToken = new ArrayList<>();
        this.stringToken.add("\'");
        this.stringToken.add("\"");

        this.ignorableCharacter = new ArrayList<>();

        this.ignorableStmt = new ArrayList<>();

        this.spanMultipleLinesString = true;
    }
}
