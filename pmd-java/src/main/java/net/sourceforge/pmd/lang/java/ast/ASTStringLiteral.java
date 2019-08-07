/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.commons.lang3.StringEscapeUtils;


/**
 * Represents a string literal. The image of this node can be the literal as it appeared
 * in the source, but JavaCC performs its own unescaping and some escapes may be lost.
 * At the very least it has delimiters. {@link #getUnescapedValue()} allows to recover
 * the actual runtime value.
 */
public final class ASTStringLiteral extends AbstractLiteral implements ASTLiteral {


    ASTStringLiteral(int id) {
        super(id);
    }


    ASTStringLiteral(JavaParser p, int id) {
        super(p, id);
    }


    private String reconstructedImage = null;


    @Override
    public String getImage() {
        if (reconstructedImage == null) {
            reconstructedImage = getEscapedStringLiteral(super.getImage());
        }
        return reconstructedImage;
    }


    /**
     * Tries to reconstruct the original string literal. If the original length
     * is greater than the parsed String literal, then probably some unicode
     * escape sequences have been used.
     */
    private String getEscapedStringLiteral(String javaccEscaped) {
        int fullLength = getEndColumn() - getBeginColumn();
        if (fullLength > javaccEscaped.length()) {
            StringBuilder result = new StringBuilder(fullLength);
            for (int i = 0; i < javaccEscaped.length(); i++) {
                char c = javaccEscaped.charAt(i);
                if (c < 0x20 || c > 0xff || javaccEscaped.length() == 1) {
                    String hex = "0000" + Integer.toHexString(c);
                    result.append("\\u").append(hex.substring(hex.length() - 4));
                } else {
                    result.append(c);
                }
            }
            return result.toString();
        }
        return javaccEscaped;
    }


    /**
     * Accept the visitor. *
     */
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
        String image = getImage();
        String woDelims = image.substring(1, image.length() - 1);
        return StringEscapeUtils.unescapeJava(woDelims);
    }

    private String escapedImage() {
        return super.getImage();
    }

    @Override
    public NodeMetaModel<? extends JavaNode> metaModel() {
        return new NodeMetaModel<ASTStringLiteral>(ASTStringLiteral.class, true) {
            @Override
            protected void writeAttributes(ASTStringLiteral node, DataOutputStream out) throws IOException {
                super.writeAttributes(node, out);
                out.writeUTF(node.escapedImage());
            }

            @Override
            protected void readAttributes(ASTStringLiteral node, DataInputStream in) throws IOException {
                super.readAttributes(node, in);
                node.setImage(in.readUTF());
            }
        };
    }
}
