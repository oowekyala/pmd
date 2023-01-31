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
 * <p>Concretely the tree
 * <pre>{@code
 * boolean foo(int i, int j) {
 *   return i + j % 2;
 * }
 * }</pre>
 * is hashed as if it was
 * <pre>{@code
 * boolean x0(int x1, int x2) {
 *   return x3 + x4 % x5;
 * }
 * }</pre>
 * where the {@code xi} may match any node of the same kind as the original.
 * So {@code foo} is placed in the same bucket as
 * <pre>{@code
 * boolean bar(int i, int j) {
 *   return i + i % 5;
 * }
 * }</pre>
 *
 * <p>The similarity metric is then affected by the two differences
 * (in places {@code x4} and {@code x5}).
 *
 * <p>TODO relax the kind requirement on hash holes. Maybe only place
 *     a mass requirement.
 *
 * <p>TODO identify unification function using local declarations.
 *     Basically anonymise identifiers when they're locally declared,
 *     but not when they're external.
 *
 * @see <a href="https://ieeexplore.ieee.org/abstract/document/738528">Clone detection using abstract syntax trees</a>
 */
package net.sourceforge.pmd.lang.java.rule.internal.tclones;
