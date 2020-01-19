/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

abstract class AbstractXPathExpr extends AbstractXPathNode implements Expr {

    private int parenDepth;

    protected AbstractXPathExpr(int id) {
        super(id);
    }


    void bumpParenDepth() {
        parenDepth++;
    }


    @Override
    public int getParenDepth() {
        return parenDepth;
    }
}
