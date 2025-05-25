/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

/** Ids for javadoc nodes. */
public enum JavadocNodeId {
    ROOT("Comment"),
    ;

    private final String xpathName;

    JavadocNodeId(String xpathName) {
        this.xpathName = xpathName;
    }

    public String getXPathNodeName() {
        return xpathName;
    }

}
