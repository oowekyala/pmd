/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java

import net.sourceforge.pmd.lang.java.ast.ASTPrimitiveType.PrimitiveType
import net.sourceforge.pmd.lang.java.ast.ASTPrimitiveType.PrimitiveType._
import net.sourceforge.pmd.lang.java.ast.BinaryOp._
import scala.jdk.CollectionConverters._

package object ast {

  object InfixExpr {
    def unapply(x: ASTExpression): Option[(ASTExpression, BinaryOp, ASTExpression)] =
      x match {
        case x: ASTInfixExpression => Some((x.getLeftOperand, x.getOperator, x.getRightOperand))
        case _ => None
      }
  }

  object VarAccess {
    def unapply(x: ASTExpression): Option[String] =
      x match {
        case x: ASTVariableAccess => Some(x.getVariableName)
        case _ => None
      }
  }

  object FieldAccess {
    def unapply(x: ASTExpression): Option[(ASTExpression, String)] =
      x match {
        case x: ASTFieldAccess => Some((x.getQualifier, x.getFieldName))
        case _ => None
      }
  }

  object MethodCall {
    def unapply(x: ASTExpression): Option[(ASTExpression, String, List[ASTExpression])] =
      x match {
        case x: ASTMethodCall => Some((x.getQualifier, x.getMethodName, List.from(IterableHasAsScala(x.getArguments).asScala)))
        case _ => None
      }
  }

  object ExprCall {
    def unapply(x: ASTExpression): Option[(ASTExpression, String)] =
      x match {
        case x: ASTFieldAccess => Some((x.getQualifier, x.getFieldName))
        case _ => None
      }
  }

  object Literal {
    def unapply(x: ASTExpression): Option[(Object, PrimitiveType)] =
      x match {
        case x: ASTNumericLiteral => Some((x.getConstValue, x.getPrimitiveType))
        case x: ASTBooleanLiteral => Some((x.getConstValue, BOOLEAN))
        case x: ASTCharLiteral => Some((x.getConstValue, CHAR))
        case _ => None
      }
  }



  def findIterableName(conditionExpr: ASTExpression, ItName: String): Option[String] = {

    val sizeExpr =
      conditionExpr match {
        case InfixExpr(VarAccess(ItName), LE, InfixExpr(e, SUB, Literal(1, INT))) => e
        case InfixExpr(VarAccess(ItName), LT, e) => e
        case _ => return None
      }

    sizeExpr match {
      case FieldAccess(VarAccess(arr), "length") => Some(arr)
      case MethodCall(VarAccess(coll), "size", List()) => Some(coll)
      case _ => None
    }
  }
}
