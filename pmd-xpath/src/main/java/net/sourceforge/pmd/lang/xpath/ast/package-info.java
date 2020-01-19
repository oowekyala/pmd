/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

/**
 * Package containing classes pertaining to the XPath AST.
 *
 * <p>This AST implements XPath 3.0 constructs. Many productions are represented
 * as marker interfaces instead of nodes to have a small AST, which is easier to
 * handle.
 *
 * <p>The XPath AST is supposed to be modifiable, in order to replace nodes
 * or rewrite the expression.
 */

package net.sourceforge.pmd.lang.xpath.ast;

