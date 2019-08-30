/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;


import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;

import net.sourceforge.pmd.document.TextRegion;
import net.sourceforge.pmd.lang.ast.impl.JavaccToken;

class TreeEditionHelper {

    private final DeletionVisitor deletionVisitor;
    private final JavaEditSession session;

    TreeEditionHelper(JavaEditSession session) {
        this.session = session;
        this.deletionVisitor = new DeletionVisitor();
    }


    void deleteInParent(AbstractJavaNode node, AbstractJavaNode child) {
        node.jjtAccept(deletionVisitor, child);
    }

    /**
     * The "data" param is the child to delete, the "node" param is the parent.
     */
    private class DeletionVisitor extends SideEffectingVisitorAdapter<AbstractJavaNode> {

        @Override
        public void visit(JavaNode node, AbstractJavaNode data) {
            deleteNodeAndWs(data);
        }

        @Override
        public void visit(ASTWildcardType node, AbstractJavaNode data) {
            deleteChildOfTypeParamOrWildcard(node, data);
        }

        @Override
        public void visit(ASTTypeParameter node, AbstractJavaNode data) {
            deleteChildOfTypeParamOrWildcard(node, data);
        }


        private void deleteChildOfTypeParamOrWildcard(Annotatable node, AbstractJavaNode child) {
            if (child instanceof ASTReferenceType) {
                // the bound node (then delete also extends/super)
                // eg:
                //   "?"_"extends"_"Object"

                JavaccToken qmark = node.jjtGetFirstToken();
                List<ASTAnnotation> annots = node.getDeclaredAnnotations();
                if (!annots.isEmpty()) {
                    qmark = annots.get(annots.size() - 1).jjtGetLastToken().next;
                }

                // qmark is "?"

                final TextRegion toDelete = session.getDocument().createRegionWithEnd(
                    qmark.getStartInDocument() + 1,
                    child.jjtGetLastToken().getEndInDocument()
                );
                session.getDocument().delete(toDelete);
            } else {
                deleteNodeAndWs(child);
            }
        }


        private void deleteNodeAndWs(AbstractJavaNode data) {
            @NonNull JavaccToken prev = data.jjtGetFirstToken();
            JavaccToken first = data.jjtGetFirstToken().getPreviousComment();
            while (first != null) {
                prev = first;
                first = first.getPreviousComment();
            }

            final TextRegion reg = data.getRegion();

            final TextRegion toDelete = reg.grow(prev.getStartInDocument()).shift(-prev.getStartInDocument());
            session.getDocument().delete(toDelete);
        }
    }
}
