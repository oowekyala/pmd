package net.sourceforge.pmd.lang.xpath.ast

import com.github.oowekyala.treeutils.matchers.TreeNodeWrapper
import net.sourceforge.pmd.lang.ast.Node
import net.sourceforge.pmd.lang.ast.test.NodeSpec
import net.sourceforge.pmd.lang.ast.test.shouldBe


fun TreeNodeWrapper<Node, *>.infixExpr(op: XpBinaryOp, contents: NodeSpec<ASTInfixExpr> = EmptyAssertions) =
        child<ASTInfixExpr> {
            it::getOperator shouldBe op
            contents()
        }

fun TreeNodeWrapper<Node, *>.int(v: Int, contents: NodeSpec<ASTNumericLiteral> = EmptyAssertions) =
        child<ASTNumericLiteral> {
            it::isIntegerLiteral shouldBe true
            it::getIntValue shouldBe v
            contents()
        }
fun TreeNodeWrapper<Node, *>.stringLit(value: String, contents: NodeSpec<ASTStringLiteral> = EmptyAssertions) =
        child<ASTStringLiteral> {
            it::getUnescapedValue shouldBe value
            contents()
        }

fun TreeNodeWrapper<Node, *>.simpleVarRef(name: String, contents: NodeSpec<ASTVarRef> = EmptyAssertions) =
        child<ASTVarRef> {
            it::getVarNameNode shouldBe simpleName(name)
            contents()
        }

fun TreeNodeWrapper<Node, *>.simpleName(name: String, contents: NodeSpec<ASTName> = EmptyAssertions) =
        child<ASTName> {
            it::getImage shouldBe name
            it::getLocalName shouldBe name
            it::getExplicitNamespacePrefix shouldBe null
            it::isUriLiteral shouldBe false

            contents()
        }

fun TreeNodeWrapper<Node, *>.prefixedName(prefix: String, local: String, contents: NodeSpec<ASTName> = EmptyAssertions) =
        child<ASTName> {
            it::getImage shouldBe "$prefix:$local"
            it::getLocalName shouldBe local
            it::getExplicitNamespacePrefix shouldBe prefix
            it::isUriLiteral shouldBe false

            contents()
        }

private val EmptyAssertions: NodeSpec<*> = {}
