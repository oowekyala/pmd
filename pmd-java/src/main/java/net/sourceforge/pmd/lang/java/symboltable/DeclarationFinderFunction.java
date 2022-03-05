/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.symboltable;

import java.util.function.Predicate;

import net.sourceforge.pmd.annotation.InternalApi;
import net.sourceforge.pmd.lang.java.ast.ASTMethodReference;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;

@Deprecated
@InternalApi
public class DeclarationFinderFunction implements Predicate<NameDeclaration> {

    private NameOccurrence occurrence;
    private NameDeclaration decl;

    public DeclarationFinderFunction(NameOccurrence occurrence) {
        this.occurrence = occurrence;
    }

    @Override
    public boolean test(NameDeclaration nameDeclaration) {
        // do not match method references
        if (occurrence.getLocation() instanceof ASTMethodReference) {
            return false;
        }

        if (isDeclaredBefore(nameDeclaration) && isSameName(nameDeclaration)) {
            decl = nameDeclaration;
            return false;
        }
        return true;
    }

    private boolean isDeclaredBefore(NameDeclaration nameDeclaration) {
        return nameDeclaration.getNode() == null || occurrence.getLocation() == null
                || nameDeclaration.getNode().getBeginLine() <= occurrence.getLocation().getBeginLine();
    }

    private boolean isSameName(NameDeclaration nameDeclaration) {
        return occurrence.getImage().equals(nameDeclaration.getName());
    }

    public NameDeclaration getDecl() {
        return this.decl;
    }
}
