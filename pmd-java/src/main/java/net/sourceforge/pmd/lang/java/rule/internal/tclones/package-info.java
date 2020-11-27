/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

/**
 * The way this works is based on the paper linked below.
 * Basically we first hash subtrees using a hash function that mostly
 * just hashes the structure of the tree (but not eg identifiers).
 * Trees are placed into buckets based on this imperfect hash.
 *
 * <p>Then trees of the same bucket, which remember, have the same structure
 * but may differ slightly, are compared using a more complete similarity-checking
 * function (which may eg compare attributes).
 *
 * <p>If the similarity score is greater than a preset limit, the trees
 * are marked as duplicates.
 *
 * <p>This handles near-duplicates better than CPD, also since it uses
 * the AST it doesn't show count in irrelevant token sequences like
 * closing braces.
 *
 * <p>TODO identify unification function using local declarations.
 *     Basically anonymise identifiers when they're locally declared,
 *     but not when they're external.
 *
 * @see <a href="https://ieeexplore.ieee.org/abstract/document/738528">Clone detection using abstract syntax trees</a>
 */
package net.sourceforge.pmd.lang.java.rule.internal.tclones;
