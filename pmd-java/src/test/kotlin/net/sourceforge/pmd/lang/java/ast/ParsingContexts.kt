/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast

import net.sourceforge.pmd.lang.ast.Node
import net.sourceforge.pmd.lang.ast.RootNode
import net.sourceforge.pmd.lang.java.ast.ParserTestCtx.Companion.findFirstNodeOnStraightLine

/**
 * @author Clément Fournier
 */

/**
 * Describes a kind of node that can be found commonly in the same contexts.
 * This type defines some machinery to parse a string to this kind of node
 * without much ado by placing it in a specific parsing context.
 */
abstract class NodeParsingCtx<T : Node, C : AbstractParserTestSpec.VersionedTestCtx<*, C>>(val constructName: String) {

    abstract fun getTemplate(construct: String, ctx: C): String

    abstract fun retrieveNode(acu: RootNode): T

    /**
     * Parse the string in the context described by this object. The parsed node is usually
     * the child of the returned [T] node. Note that [parseAndFind] can save you some keystrokes
     * because it finds a descendant of the wanted type.
     *
     * @param construct The construct to parse
     *
     * @return A [T] whose child is the given statement
     *
     * @throws ParseException If the argument is no valid construct of this kind (mind the language version)
     */
    open fun parseNode(construct: String, ctx: C): T {
        val root = ctx.javaVersion.parse(getTemplate(construct, ctx))

        return retrieveNode(root)
    }

    /**
     * Parse the string the context described by this object, and finds the first descendant of type [N].
     * The descendant is searched for by [findFirstNodeOnStraightLine], to prevent accidental
     * mis-selection of a node. In such a case, a [NoSuchElementException] is thrown, and you
     * should fix your test case.
     *
     * @param construct The construct to parse
     * @param N The type of node to find
     *
     * @return The first descendant of type [N] found in the parsed expression
     *
     * @throws NoSuchElementException If no node of type [N] is found by [findFirstNodeOnStraightLine]
     * @throws ParseException If the argument is no valid construct of this kind
     *
     */
    inline fun <reified N : Node> parseAndFind(construct: String, ctx: C): N =
            parseNode(construct, ctx).findFirstNodeOnStraightLine(N::class.java)
                    ?: throw NoSuchElementException("No node of type ${N::class.java.simpleName} in the given $constructName:\n\t$construct")

    override fun toString(): String = "$constructName context"
}

object ExpressionParsingCtx : NodeParsingCtx<ASTExpression, ParserTestCtx>("expression") {

    override fun getTemplate(construct: String, ctx: ParserTestCtx): String =
            """
            ${ctx.imports.joinToString(separator = "\n")}
            ${ctx.genClassHeader} {
                {
                    Object o = $construct;
                }
            }
            """.trimIndent()


    override fun retrieveNode(acu: RootNode): ASTExpression = acu.getFirstDescendantOfType(ASTExpression::class.java)!!
}

object StatementParsingCtx : NodeParsingCtx<ASTBlockStatement, ParserTestCtx>("statement") {

    override fun getTemplate(construct: String, ctx: ParserTestCtx): String =
            """
            ${ctx.imports.joinToString(separator = "\n")}
            ${ctx.genClassHeader} {
                {
                    $construct
                }
            }
            """.trimIndent()


    override fun retrieveNode(acu: RootNode): ASTBlockStatement = acu.getFirstDescendantOfType(ASTBlockStatement::class.java)
}

object TopLevelTypeDeclarationParsingCtx : NodeParsingCtx<ASTAnyTypeDeclaration, ParserTestCtx>("top-level declaration") {

    override fun getTemplate(construct: String, ctx: ParserTestCtx): String = """
        ${ctx.imports.joinToString(separator = "\n")}
        $construct
        """.trimIndent()

    override fun retrieveNode(acu: RootNode): ASTAnyTypeDeclaration = acu.getFirstDescendantOfType(ASTAnyTypeDeclaration::class.java)!!
}

object EnclosedDeclarationParsingCtx : NodeParsingCtx<ASTAnyTypeBodyDeclaration, ParserTestCtx>("enclosed declaration") {

    override fun getTemplate(construct: String, ctx: ParserTestCtx): String = """
            ${ctx.imports.joinToString(separator = "\n")}
            ${ctx.genClassHeader} {
                $construct
            }
            """.trimIndent()

    override fun retrieveNode(acu: RootNode): ASTAnyTypeBodyDeclaration =
            acu.getFirstDescendantOfType(ASTAnyTypeBodyDeclaration::class.java)!!
}

object TypeParametersParsingCtx : NodeParsingCtx<ASTTypeParameters, ParserTestCtx>("type parameters") {
    override fun getTemplate(construct: String, ctx: ParserTestCtx): String =
            """
            ${ctx.imports.joinToString(separator = "\n")}
            ${ctx.genClassHeader} {


                public $construct void f() {}
            }
            """.trimIndent()

    override fun retrieveNode(acu: RootNode): ASTTypeParameters =
            acu.getFirstDescendantOfType(ASTMethodDeclaration::class.java).typeParameters
}

object TypeParsingCtx : NodeParsingCtx<ASTType, ParserTestCtx>("type") {
    override fun getTemplate(construct: String, ctx: ParserTestCtx): String =
            """
            ${ctx.imports.joinToString(separator = "\n")}
            ${ctx.genClassHeader} {
                Object f = ($construct) null;
            }
            """.trimIndent()

    override fun retrieveNode(acu: RootNode): ASTType =
            acu.getFirstDescendantOfType(ASTCastExpression::class.java).castType
}

object AnnotationParsingCtx : NodeParsingCtx<ASTAnnotation, ParserTestCtx>("annotation") {
    override fun getTemplate(construct: String, ctx: ParserTestCtx): String =
            """
            ${ctx.imports.joinToString(separator = "\n")}
            ${ctx.genClassHeader} {
                Object f = ($construct Type) null;
            }
            """.trimIndent()

    override fun retrieveNode(acu: RootNode): ASTAnnotation =
            acu.getFirstDescendantOfType(ASTCastExpression::class.java)
                    .getFirstDescendantOfType(ASTAnnotation::class.java)
}


