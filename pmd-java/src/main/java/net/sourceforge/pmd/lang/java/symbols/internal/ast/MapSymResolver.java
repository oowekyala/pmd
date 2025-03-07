/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.symbols.internal.ast;

import java.util.Map;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.lang.java.symbols.JClassSymbol;
import net.sourceforge.pmd.lang.java.symbols.JModuleSymbol;
import net.sourceforge.pmd.lang.java.symbols.SymbolResolver;
import net.sourceforge.pmd.lang.java.symbols.internal.asm.ClasspathRequest;

/**
 * A symbol resolver that knows about a few hand-picked symbols.
 * Does not track dependencies, as this is only used for symbols declared in the same file.
 */
final class MapSymResolver implements SymbolResolver {
    private static final Logger LOG = LoggerFactory.getLogger(MapSymResolver.class);

    private final Map<String, JClassSymbol> byCanonicalName;
    private final Map<String, JClassSymbol> byBinaryName;

    MapSymResolver(Map<String, JClassSymbol> byCanonicalName,
                   Map<String, JClassSymbol> byBinaryName) {
        this.byCanonicalName = byCanonicalName;
        this.byBinaryName = byBinaryName;
    }

    @Override
    public @Nullable JClassSymbol resolveClassFromBinaryName(@NonNull String binaryName, ClasspathRequest origin) {
        return byBinaryName.get(binaryName);
    }

    @Override
    public @Nullable JClassSymbol resolveClassFromCanonicalName(@NonNull String canonicalName, ClasspathRequest origin) {
        return byCanonicalName.get(canonicalName);
    }

    @Override
    public @Nullable JModuleSymbol resolveModule(@NonNull String moduleName, ClasspathRequest origin) {
        return null;
    }

    @Override
    public void logStats() {
        LOG.trace("Used {} classes by canonical name and {} classes by binary name",
                byCanonicalName.size(), byBinaryName.size());
    }
}
