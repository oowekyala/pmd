/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.lang.java.ast;

import java.util.Iterator;

import net.sourceforge.pmd.lang.ast.RootNode;

public final class ASTPackage extends AbstractJavaNode implements RootNode, JSingleChildNode<ASTCompilationUnit>, Iterable<ASTCompilationUnit> {


    public ASTPackage(String pname) {
        super(NodeFactory.PACKAGE);
        setImage(pname);
    }

    @Override
    public Object jjtAccept(JavaParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }

    @Override
    public String getXPathNodeName() {
        return "Package";
    }

    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }

    @Override
    public ASTCompilationUnit getRoot() {
        return getFirstChild();
    }

    @Override
    public ASTCompilationUnit jjtGetChild(int index) {
        return (ASTCompilationUnit) super.jjtGetChild(index);
    }



    @Override
    public Iterator<ASTCompilationUnit> iterator() {
        return new NodeChildrenIterator<>(this, ASTCompilationUnit.class);
    }
}
