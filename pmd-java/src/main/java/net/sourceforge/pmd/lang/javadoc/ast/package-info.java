/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

/**
 * Contains classes to model the Javadoc AST. Javadoc trees are
 * made accessible from the Java nodes that can bear a Javadoc
 * comment (see {@link net.sourceforge.pmd.lang.java.ast.JavadocCommentOwner}).
 * That is the only supported API point to get a hold of a Javadoc
 * tree, Javadoc parsing tools are internal.
 *
 * <p>See {@link net.sourceforge.pmd.lang.javadoc.ast.JavadocNode}
 * for a description of the structure of the AST.
 */
package net.sourceforge.pmd.lang.javadoc.ast;
