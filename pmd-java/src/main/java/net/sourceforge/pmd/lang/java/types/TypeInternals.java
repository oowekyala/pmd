/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.types;

import java.util.function.UnaryOperator;

import net.sourceforge.pmd.annotation.InternalApi;
import net.sourceforge.pmd.lang.java.symbols.SymbolResolver;

/**
 * @author Clément Fournier
 */
@InternalApi
public final class TypeInternals {

    private TypeInternals() {
        // utility class
    }

    public static void transformResolver(TypeSystem ts, UnaryOperator<SymbolResolver> resolver) {
        ts.transformResolver(resolver);
    }

}
