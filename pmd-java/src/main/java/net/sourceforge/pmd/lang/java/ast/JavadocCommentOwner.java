/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocComment;

/**
 * A node that may own a javadoc comment.
 */
public interface JavadocCommentOwner extends JavaNode {
    // TODO can record components be documented individually?

    /**
     * Returns the javadoc comment that applies to this declaration. If
     * there is none, returns null.
     */
    default @Nullable FormalComment getJavadocComment() {
        return CommentAssignmentPass.getComment(this);
    }

    /**
     * Returns the root of the javadoc tree, if there is a comment.
     */
    default @Nullable JdocComment getJavadocTree() {
        FormalComment comment = getJavadocComment();
        if (comment != null) {
            return comment.getJdocTree();
        }
        return null;
    }

}
