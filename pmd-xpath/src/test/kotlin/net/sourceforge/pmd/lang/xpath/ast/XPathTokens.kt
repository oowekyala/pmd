package net.sourceforge.pmd.lang.xpath.ast

/**
 * @author Clément Fournier
 * @since 6.7.0
 */
object XPathTokens {


    val binaryOperators =
            mapOf(ASTOrExpr::class.java to listOf("or"),
            ASTAndExpr::class.java to listOf("and"),
            ASTMultiplicativeExpr::class.java to listOf("div", "idiv", "mod", "*"),
            ASTUnionExpr::class.java to listOf("union", "|"),
            ASTIntersectExceptExpr::class.java to listOf("intersect", "except"),
            ASTComparisonExpr::class.java to listOf("ne", "eq", "lt", "le", "gt", "ge", "=", "!=", "<", "<=", ">", ">=", ">>", "<<"))


}