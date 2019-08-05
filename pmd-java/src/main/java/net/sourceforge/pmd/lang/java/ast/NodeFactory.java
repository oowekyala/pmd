/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

import static net.sourceforge.pmd.lang.java.ast.JavaParserTreeConstants.*;

/**
 * @author Clément Fournier
 */
public class NodeFactory {


    public static JavaNode jjtCreate(JavaParser parser, int id) {

        switch (id) {

        case JJTCOMPILATIONUNIT:
            return new ASTCompilationUnit(parser, id);
        case JJTPACKAGEDECLARATION:
            return new ASTPackageDeclaration(parser, id);
        case JJTIMPORTDECLARATION:
            return new ASTImportDeclaration(parser, id);
        case JJTTYPEDECLARATION:
            return new ASTTypeDeclaration(parser, id);
        case JJTCLASSORINTERFACEDECLARATION:
            return new ASTClassOrInterfaceDeclaration(parser, id);
        case JJTEXTENDSLIST:
            return new ASTExtendsList(parser, id);
        case JJTIMPLEMENTSLIST:
            return new ASTImplementsList(parser, id);
        case JJTENUMDECLARATION:
            return new ASTEnumDeclaration(parser, id);
        case JJTENUMBODY:
            return new ASTEnumBody(parser, id);
        case JJTENUMCONSTANT:
            return new ASTEnumConstant(parser, id);
        case JJTANONYMOUSCLASSDECLARATION:
            return new ASTAnonymousClassDeclaration(parser, id);
        case JJTTYPEPARAMETERS:
            return new ASTTypeParameters(parser, id);
        case JJTTYPEPARAMETER:
            return new ASTTypeParameter(parser, id);
        case JJTCLASSORINTERFACEBODY:
            return new ASTClassOrInterfaceBody(parser, id);
        case JJTCLASSORINTERFACEBODYDECLARATION:
            return new ASTClassOrInterfaceBodyDeclaration(parser, id);
        case JJTEMPTYDECLARATION:
            return new ASTEmptyDeclaration(parser, id);
        case JJTFIELDDECLARATION:
            return new ASTFieldDeclaration(parser, id);
        case JJTVARIABLEDECLARATOR:
            return new ASTVariableDeclarator(parser, id);
        case JJTVARIABLEDECLARATORID:
            return new ASTVariableDeclaratorId(parser, id);
        case JJTARRAYINITIALIZER:
            return new ASTArrayInitializer(parser, id);
        case JJTMETHODDECLARATION:
            return new ASTMethodDeclaration(parser, id);
        case JJTMETHODDECLARATOR:
            return new ASTMethodDeclarator(parser, id);
        case JJTFORMALPARAMETERS:
            return new ASTFormalParameters(parser, id);
        case JJTFORMALPARAMETER:
            return new ASTFormalParameter(parser, id);
        case JJTCONSTRUCTORDECLARATION:
            return new ASTConstructorDeclaration(parser, id);
        case JJTEXPLICITCONSTRUCTORINVOCATION:
            return new ASTExplicitConstructorInvocation(parser, id);
        case JJTINITIALIZER:
            return new ASTInitializer(parser, id);
        case JJTINTERSECTIONTYPE:
            return new ASTIntersectionType(parser, id);
        case JJTARRAYTYPEDIMS:
            return new ASTArrayTypeDims(parser, id);
        case JJTARRAYTYPEDIM:
            return new ASTArrayTypeDim(parser, id);
        case JJTARRAYTYPE:
            return new ASTArrayType(parser, id);
        case JJTCLASSORINTERFACETYPE:
            return new ASTClassOrInterfaceType(parser, id);
        case JJTTYPEARGUMENTS:
            return new ASTTypeArguments(parser, id);
        case JJTWILDCARDTYPE:
            return new ASTWildcardType(parser, id);
        case JJTPRIMITIVETYPE:
            return new ASTPrimitiveType(parser, id);
        case JJTRESULTTYPE:
            return new ASTResultType(parser, id);
        case JJTNAMELIST:
            return new ASTNameList(parser, id);
        case JJTCONDITIONALEXPRESSION:
            return new ASTConditionalExpression(parser, id);
        case JJTCONDITIONALOREXPRESSION:
            return new ASTConditionalOrExpression(parser, id);
        case JJTCONDITIONALANDEXPRESSION:
            return new ASTConditionalAndExpression(parser, id);
        case JJTINCLUSIVEOREXPRESSION:
            return new ASTInclusiveOrExpression(parser, id);
        case JJTEXCLUSIVEOREXPRESSION:
            return new ASTExclusiveOrExpression(parser, id);
        case JJTANDEXPRESSION:
            return new ASTAndExpression(parser, id);
        case JJTEQUALITYEXPRESSION:
            return new ASTEqualityExpression(parser, id);
        case JJTINSTANCEOFEXPRESSION:
            return new ASTInstanceOfExpression(parser, id);
        case JJTRELATIONALEXPRESSION:
            return new ASTRelationalExpression(parser, id);
        case JJTSHIFTEXPRESSION:
            return new ASTShiftExpression(parser, id);
        case JJTADDITIVEEXPRESSION:
            return new ASTAdditiveExpression(parser, id);
        case JJTMULTIPLICATIVEEXPRESSION:
            return new ASTMultiplicativeExpression(parser, id);
        case JJTUNARYEXPRESSION:
            return new ASTUnaryExpression(parser, id);
        case JJTPREINCREMENTEXPRESSION:
            return new ASTPreIncrementExpression(parser, id);
        case JJTPREDECREMENTEXPRESSION:
            return new ASTPreDecrementExpression(parser, id);
        case JJTCASTEXPRESSION:
            return new ASTCastExpression(parser, id);
        case JJTPOSTFIXEXPRESSION:
            return new ASTPostfixExpression(parser, id);
        case JJTSWITCHEXPRESSION:
            return new ASTSwitchExpression(parser, id);
        case JJTTHISEXPRESSION:
            return new ASTThisExpression(parser, id);
        case JJTSUPEREXPRESSION:
            return new ASTSuperExpression(parser, id);
        case JJTCLASSLITERAL:
            return new ASTClassLiteral(parser, id);
        case JJTMETHODCALL:
            return new ASTMethodCall(parser, id);
        case JJTARRAYACCESS:
            return new ASTArrayAccess(parser, id);
        case JJTFIELDACCESS:
            return new ASTFieldAccess(parser, id);
        case JJTMETHODREFERENCE:
            return new ASTMethodReference(parser, id);
        case JJTLAMBDAEXPRESSION:
            return new ASTLambdaExpression(parser, id);
        case JJTLAMBDAPARAMETERLIST:
            return new ASTLambdaParameterList(parser, id);
        case JJTLAMBDAPARAMETER:
            return new ASTLambdaParameter(parser, id);
        case JJTBOOLEANLITERAL:
            return new ASTBooleanLiteral(parser, id);
        case JJTNULLLITERAL:
            return new ASTNullLiteral(parser, id);
        case JJTNUMERICLITERAL:
            return new ASTNumericLiteral(parser, id);
        case JJTCHARLITERAL:
            return new ASTCharLiteral(parser, id);
        case JJTSTRINGLITERAL:
            return new ASTStringLiteral(parser, id);
        case JJTARGUMENTLIST:
            return new ASTArgumentList(parser, id);
        case JJTARRAYALLOCATION:
            return new ASTArrayAllocation(parser, id);
        case JJTCONSTRUCTORCALL:
            return new ASTConstructorCall(parser, id);
        case JJTARRAYALLOCATIONDIMS:
            return new ASTArrayAllocationDims(parser, id);
        case JJTARRAYDIMEXPR:
            return new ASTArrayDimExpr(parser, id);
        case JJTSTATEMENT:
            return new ASTStatement(parser, id);
        case JJTLABELEDSTATEMENT:
            return new ASTLabeledStatement(parser, id);
        case JJTBLOCK:
            return new ASTBlock(parser, id);
        case JJTBLOCKSTATEMENT:
            return new ASTBlockStatement(parser, id);
        case JJTLOCALVARIABLEDECLARATION:
            return new ASTLocalVariableDeclaration(parser, id);
        case JJTEMPTYSTATEMENT:
            return new ASTEmptyStatement(parser, id);
        case JJTSTATEMENTEXPRESSION:
            return new ASTStatementExpression(parser, id);
        case JJTASSIGNMENTEXPRESSION:
            return new ASTAssignmentExpression(parser, id);
        case JJTSWITCHSTATEMENT:
            return new ASTSwitchStatement(parser, id);
        case JJTSWITCHLABELEDEXPRESSION:
            return new ASTSwitchLabeledExpression(parser, id);
        case JJTSWITCHLABELEDBLOCK:
            return new ASTSwitchLabeledBlock(parser, id);
        case JJTSWITCHLABELEDTHROWSTATEMENT:
            return new ASTSwitchLabeledThrowStatement(parser, id);
        case JJTSWITCHLABEL:
            return new ASTSwitchLabel(parser, id);
        case JJTIFSTATEMENT:
            return new ASTIfStatement(parser, id);
        case JJTWHILESTATEMENT:
            return new ASTWhileStatement(parser, id);
        case JJTDOSTATEMENT:
            return new ASTDoStatement(parser, id);
        case JJTFORSTATEMENT:
            return new ASTForStatement(parser, id);
        case JJTFORINIT:
            return new ASTForInit(parser, id);
        case JJTSTATEMENTEXPRESSIONLIST:
            return new ASTStatementExpressionList(parser, id);
        case JJTFORUPDATE:
            return new ASTForUpdate(parser, id);
        case JJTBREAKSTATEMENT:
            return new ASTBreakStatement(parser, id);
        case JJTCONTINUESTATEMENT:
            return new ASTContinueStatement(parser, id);
        case JJTRETURNSTATEMENT:
            return new ASTReturnStatement(parser, id);
        case JJTTHROWSTATEMENT:
            return new ASTThrowStatement(parser, id);
        case JJTSYNCHRONIZEDSTATEMENT:
            return new ASTSynchronizedStatement(parser, id);
        case JJTTRYSTATEMENT:
            return new ASTTryStatement(parser, id);
        case JJTRESOURCESPECIFICATION:
            return new ASTResourceSpecification(parser, id);
        case JJTRESOURCES:
            return new ASTResources(parser, id);
        case JJTRESOURCE:
            return new ASTResource(parser, id);
        case JJTCATCHSTATEMENT:
            return new ASTCatchStatement(parser, id);
        case JJTFINALLYSTATEMENT:
            return new ASTFinallyStatement(parser, id);
        case JJTASSERTSTATEMENT:
            return new ASTAssertStatement(parser, id);
        case JJTNORMALANNOTATION:
            return new ASTNormalAnnotation(parser, id);
        case JJTMARKERANNOTATION:
            return new ASTMarkerAnnotation(parser, id);
        case JJTSINGLEMEMBERANNOTATION:
            return new ASTSingleMemberAnnotation(parser, id);
        case JJTMEMBERVALUEPAIR:
            return new ASTMemberValuePair(parser, id);
        case JJTMEMBERVALUEARRAYINITIALIZER:
            return new ASTMemberValueArrayInitializer(parser, id);
        case JJTANNOTATIONTYPEDECLARATION:
            return new ASTAnnotationTypeDeclaration(parser, id);
        case JJTANNOTATIONTYPEBODY:
            return new ASTAnnotationTypeBody(parser, id);
        case JJTANNOTATIONTYPEMEMBERDECLARATION:
            return new ASTAnnotationTypeMemberDeclaration(parser, id);
        case JJTANNOTATIONMETHODDECLARATION:
            return new ASTAnnotationMethodDeclaration(parser, id);
        case JJTDEFAULTVALUE:
            return new ASTDefaultValue(parser, id);
        case JJTMODULEDECLARATION:
            return new ASTModuleDeclaration(parser, id);
        case JJTMODULEDIRECTIVE:
            return new ASTModuleDirective(parser, id);
        case JJTMODULENAME:
            return new ASTModuleName(parser, id);
        case JJTNAME:
            return new ASTName(parser, id);
        case JJTAMBIGUOUSNAME:
            return new ASTAmbiguousName(parser, id);
        case JJTVARIABLEREFERENCE:
            return new ASTVariableReference(parser, id);

        default:
            throw new IllegalArgumentException("Unknown id " + id);
        }

    }

}
