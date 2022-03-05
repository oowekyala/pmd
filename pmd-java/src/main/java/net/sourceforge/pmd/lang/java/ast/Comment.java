/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

import java.util.stream.Stream;

import net.sourceforge.pmd.internal.util.IteratorUtil;
import net.sourceforge.pmd.lang.ast.GenericToken;
import net.sourceforge.pmd.lang.ast.impl.javacc.JavaccToken;
import net.sourceforge.pmd.lang.ast.impl.javacc.JjtreeNode;
import net.sourceforge.pmd.lang.document.Chars;
import net.sourceforge.pmd.lang.document.FileLocation;
import net.sourceforge.pmd.lang.document.Reportable;

/**
 * Wraps a comment token to provide some utilities.
 * This is not a node, it's not part of the tree anywhere,
 * just convenient.
 *
 * <p>This class represents any kind of comment. A specialized subclass
 * provides more API for Javadoc comments, see {@link JavadocComment}.
 */
public class Comment implements Reportable {
    //TODO maybe move part of this into pmd core

    private final JavaccToken token;

    Comment(JavaccToken t) {
        this.token = t;
    }

    @Override
    public FileLocation getReportLocation() {
        return getToken().getReportLocation();
    }

    /**
     * @deprecated Use {@link #getText()}
     */
    @Deprecated
    public String getImage() {
        return getToken().getImage();
    }

    /** The token underlying this comment. */
    public final JavaccToken getToken() {
        return token;
    }

    public boolean isSingleLine() {
        return token.kind == JavaTokenKinds.SINGLE_LINE_COMMENT;
    }

    public boolean hasJavadocContent() {
        return token.kind == JavaTokenKinds.FORMAL_COMMENT;
    }

    /** Returns the full text of the comment. */
    public Chars getText() {
        return getToken().getImageCs();
    }

    /**
     * Returns true if the given token has the kind
     * of a comment token (there are three such kinds).
     */
    public static boolean isComment(JavaccToken token) {
        return JavaTokenDocumentBehavior.isComment(token);
    }

    /**
     * Removes the leading comment marker (like {@code *}) of each line
     * of the comment as well as the start marker ({@code //}, {@code /*} or {@code /**}
     * and the end markers (<code>&#x2a;/</code>).
     *
     * <p>Empty lines are removed.
     *
     * @return List of lines of the comments
     */
    public Iterable<Chars> filteredLines() {
        return filteredLines(false);
    }

    public Iterable<Chars> filteredLines(boolean preserveEmptyLines) {
        if (preserveEmptyLines) {
            return () -> IteratorUtil.map(getText().lines().iterator(), Comment::removeCommentMarkup);
        } else {
            return () -> IteratorUtil.mapNotNull(
                getText().lines().iterator(),
                line -> {
                    line = removeCommentMarkup(line);
                    return line.isEmpty() ? null : line;
                }
            );
        }
    }

    /**
     * True if this is a comment delimiter or an asterisk. This
     * tests the whole parameter and not a prefix/suffix.
     */
    @SuppressWarnings("PMD.LiteralsFirstInComparisons") // a fp
    public static boolean isMarkupWord(Chars word) {
        return word.length() <= 3
            && (word.contentEquals("*")
            || word.contentEquals("//")
            || word.contentEquals("/*")
            || word.contentEquals("*/")
            || word.contentEquals("/**"));
    }

    /**
     * Trim the start of the provided line to remove a comment
     * markup opener ({@code //, /*, /**, *}) or closer {@code * /}.
     */
    private static Chars removeCommentMarkup(Chars line) {
        line = line.trim().removeSuffix("*/");
        int subseqFrom = 0;
        if (line.startsWith('/', 0)) {
            if (line.startsWith("**", 1)) {
                subseqFrom = 3;
            } else if (line.startsWith('/', 1)
                || line.startsWith('*', 1)) {
                subseqFrom = 2;
            }
        } else if (line.startsWith('*', 0)) {
            subseqFrom = 1;
        }
        return line.subSequence(subseqFrom, line.length()).trim();
    }

    private static Stream<JavaccToken> getSpecialCommentsIn(JjtreeNode<?> node) {
        return GenericToken.streamRange(node.getFirstToken(), node.getLastToken())
                           .flatMap(it -> IteratorUtil.toStream(GenericToken.previousSpecials(it).iterator()));
    }

    public static Stream<JavaccToken> getLeadingComments(JavaNode node) {
        if (node instanceof AccessNode) {
            node = ((AccessNode) node).getModifiers();
        }
        return getSpecialCommentsIn(node).filter(Comment::isComment);
    }
}
