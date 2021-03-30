/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.ast.impl.javacc.JavaccToken;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocComment;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocParserFacade;

/**
 * A wrapper for Javadoc {@link Comment}s.
 */
public class FormalComment extends Comment {

    private JdocComment parsed;
    private JavadocCommentOwner owner;

    public FormalComment(JavaccToken t) {
        super(t);
        assert t.kind == JavaTokenKinds.FORMAL_COMMENT;
    }

    void setOwner(JavadocCommentOwner owner) {
        this.owner = owner;
    }

    public @Nullable JavadocCommentOwner getOwner() {
        return owner;
    }

    /**
     * Returns the root of the parsed javadoc tree.
     */
    public @NonNull JdocComment getJdocTree() {
        if (parsed == null) {
            parsed = JavadocParserFacade.parseJavaToken(this, getToken());
        }
        return parsed;
    }

}
