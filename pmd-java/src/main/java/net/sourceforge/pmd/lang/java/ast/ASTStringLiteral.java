/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Represents a string literal. The image of this node is the literal as it appeared
 * in the source ({@link #getText()}). {@link #getUnescapedValue()} allows to recover
 * the actual runtime value, by processing escapes.
 */
public final class ASTStringLiteral extends AbstractLiteral implements ASTLiteral {


    ASTStringLiteral(int id) {
        super(id);
    }


    ASTStringLiteral(JavaParser p, int id) {
        super(p, id);
    }


    @Override
    public String getImage() {
        return getText().toString();
    }


    @Override
    public Object jjtAccept(JavaParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    /**
     * Returns the value without delimiters and unescaped.
     */
    public String getUnescapedValue() {
        String image = super.getImage();
        String woDelims = image.substring(1, image.length() - 1);
        return StringEscapeUtils.unescapeJava(woDelims);
    }

}
