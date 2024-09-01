/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.symbols.table.internal;


import static net.sourceforge.pmd.lang.java.symbols.table.ScopeInfo.FORMAL_PARAM;
import static net.sourceforge.pmd.lang.java.symbols.table.ScopeInfo.IMPORT_ON_DEMAND;
import static net.sourceforge.pmd.lang.java.symbols.table.ScopeInfo.SAME_FILE;
import static net.sourceforge.pmd.lang.java.symbols.table.ScopeInfo.SIMPLE_COMPILATION_UNIT;
import static net.sourceforge.pmd.util.AssertionUtil.isValidJavaPackageName;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.ast.NodeStream;
import net.sourceforge.pmd.lang.ast.SemanticErrorReporter;
import net.sourceforge.pmd.lang.java.ast.ASTFormalParameters;
import net.sourceforge.pmd.lang.java.ast.ASTImportDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTList;
import net.sourceforge.pmd.lang.java.ast.ASTTypeDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTTypeParameter;
import net.sourceforge.pmd.lang.java.ast.ASTTypeParameters;
import net.sourceforge.pmd.lang.java.ast.ASTVariableId;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.internal.JavaAstProcessor;
import net.sourceforge.pmd.lang.java.symbols.JClassSymbol;
import net.sourceforge.pmd.lang.java.symbols.JConstructorSymbol;
import net.sourceforge.pmd.lang.java.symbols.JFormalParamSymbol;
import net.sourceforge.pmd.lang.java.symbols.JLocalVariableSymbol;
import net.sourceforge.pmd.lang.java.symbols.JTypeDeclSymbol;
import net.sourceforge.pmd.lang.java.symbols.JVariableSymbol;
import net.sourceforge.pmd.lang.java.symbols.SymbolResolver;
import net.sourceforge.pmd.lang.java.symbols.table.JSymbolTable;
import net.sourceforge.pmd.lang.java.symbols.table.ScopeInfo;
import net.sourceforge.pmd.lang.java.symbols.table.coreimpl.NameResolver;
import net.sourceforge.pmd.lang.java.symbols.table.coreimpl.ShadowChainBuilder;
import net.sourceforge.pmd.lang.java.symbols.table.coreimpl.ShadowChainNode;
import net.sourceforge.pmd.lang.java.types.JClassType;
import net.sourceforge.pmd.lang.java.types.JMethodSig;
import net.sourceforge.pmd.lang.java.types.JTypeMirror;
import net.sourceforge.pmd.lang.java.types.JTypeVar;
import net.sourceforge.pmd.lang.java.types.JVariableSig;

class SymTableFactory {


    protected final String thisPackage;
    protected final JavaAstProcessor processor;

    static final ShadowChainBuilder<JTypeMirror, ScopeInfo> TYPES = new ShadowChainBuilder<JTypeMirror, ScopeInfo>() {

        @Override
        public String getSimpleName(JTypeMirror type) {
            if (type instanceof JClassType) {
                return ((JClassType) type).getSymbol().getSimpleName();
            } else if (type instanceof JTypeVar) {
                JTypeDeclSymbol sym = type.getSymbol();
                assert sym != null : "Must contain named symbols";
                return sym.getSimpleName();
            }
            throw new AssertionError("Cannot contain type " + type);
        }
    };

    static final ShadowChainBuilder<JVariableSig, ScopeInfo> VARS = new ShadowChainBuilder<JVariableSig, ScopeInfo>() {
        @Override
        public String getSimpleName(JVariableSig sym) {
            return sym.getSymbol().getSimpleName();
        }
    };

    static final ShadowChainBuilder<JMethodSig, ScopeInfo> METHODS = new ShadowChainBuilder<JMethodSig, ScopeInfo>() {
        @Override
        public String getSimpleName(JMethodSig sym) {
            return sym.getName();
        }
    };


    SymTableFactory(String thisPackage, JavaAstProcessor processor) {
        this.thisPackage = thisPackage;
        this.processor = processor;
    }


    // <editor-fold defaultstate="collapsed" desc="Utilities for classloading">


    SemanticErrorReporter getLogger() {
        return processor.getLogger();
    }

    JClassSymbol loadClassReportFailure(JavaNode location, String fqcn) {
        JClassSymbol loaded = loadClassOrFail(fqcn);
        if (loaded == null) {
            getLogger().warning(location, JavaSemanticErrors.CANNOT_RESOLVE_SYMBOL, fqcn);
        }

        return loaded;
    }

    /** @see SymbolResolver#resolveClassFromCanonicalName(String) */
    @Nullable
    JClassSymbol loadClassOrFail(String fqcn) {
        return processor.getSymResolver().resolveClassFromCanonicalName(fqcn);
    }

    // </editor-fold>

    protected @NonNull JSymbolTable buildTable(JSymbolTable parent,
                                             ShadowChainNode<JVariableSig, ScopeInfo> vars,
                                             ShadowChainNode<JMethodSig, ScopeInfo> methods,
                                             ShadowChainNode<JTypeMirror, ScopeInfo> types) {
        if (vars == parent.variables() && methods == parent.methods() && types == parent.types()) { // NOPMD CompareObjectsWithEquals
            return parent;
        } else {
            return new SymbolTableImpl(vars, types, methods);
        }
    }

    protected ShadowChainNode<JTypeMirror, ScopeInfo> typeNode(JSymbolTable parent) {
        return parent.types().asNode();
    }

    protected ShadowChainNode<JVariableSig, ScopeInfo> varNode(JSymbolTable parent) {
        return parent.variables().asNode();
    }

    protected ShadowChainNode<JMethodSig, ScopeInfo> methodNode(JSymbolTable parent) {
        return parent.methods().asNode();
    }

    /**
     * Create name resolvers from a set of static imported names. This happens lazily.
     */
    class StaticImportOnDemandWalks {

        private final List<String> staticImportedNames = new ArrayList<>();

        NameResolver<JVariableSig> vars;
        NameResolver<JTypeMirror> types;
        NameResolver<JMethodSig> methods;

        public NameResolver<JVariableSig> getVars() {
            populate();
            return vars;
        }

        public NameResolver<JTypeMirror> getTypes() {
            populate();
            return types;
        }

        public NameResolver<JMethodSig> getMethods() {
            populate();
            return methods;
        }

        void populate() {
            if (vars != null) {
                return;
            }
            ShadowChainBuilder<JTypeMirror, ScopeInfo>.ResolverBuilder importedTypes = TYPES.new ResolverBuilder();
            ShadowChainBuilder<JVariableSig, ScopeInfo>.ResolverBuilder importedFields = VARS.new ResolverBuilder();
            List<NameResolver<JMethodSig>> methodResolvers = new ArrayList<>();

            for (String containerName : staticImportedNames) {

                JClassSymbol sym = loadClassOrFail(containerName);
                if (sym == null) {
                    continue;
                }
                JClassType containerType = (JClassType) sym.getTypeSystem().typeOf(sym, false);


                Pair<ShadowChainBuilder<JTypeMirror, ?>.ResolverBuilder, ShadowChainBuilder<JVariableSig, ?>.ResolverBuilder> pair =
                    JavaResolvers.importOnDemandMembersResolvers(containerType, thisPackage);

                importedTypes.absorb(pair.getLeft());
                importedFields.absorb(pair.getRight());
                methodResolvers.add(JavaResolvers.staticImportOnDemandMethodResolver(containerType, thisPackage));
            }

            types = importedTypes.build();
            vars = importedFields.build();
            methods = NameResolver.composite(methodResolvers);
        }

    }

    JSymbolTable importsOnDemand(JSymbolTable parent, Collection<ASTImportDeclaration> importsOnDemand) {
        if (importsOnDemand.isEmpty()) {
            return parent;
        }

        StaticImportOnDemandWalks statics = new StaticImportOnDemandWalks();
        Set<String> packageAndTypesImportsOD = new HashSet<>();
        for (ASTImportDeclaration importDecl : importsOnDemand) {
            if (importDecl.isStatic()) {
                statics.staticImportedNames.add(importDecl.getImportedName());
            } else {
                packageAndTypesImportsOD.add(importDecl.getImportedName());
            }
        }

        ShadowChainNode<JTypeMirror, ScopeInfo> types = TYPES.shadow(typeNode(parent), IMPORT_ON_DEMAND, name -> statics.getTypes().resolveHere(name));
        if (!packageAndTypesImportsOD.isEmpty()) {
            types = TYPES.shadowWithCache(types, IMPORT_ON_DEMAND, new HashMap<>(), JavaResolvers.importedOnDemand(packageAndTypesImportsOD, processor, thisPackage));
        }
        return buildTable(
            parent,
            VARS.shadow(varNode(parent), IMPORT_ON_DEMAND, name -> statics.getVars().resolveHere(name)),
            METHODS.shadow(methodNode(parent), IMPORT_ON_DEMAND, name -> statics.getMethods().resolveHere(name)),
            types
        );

    }

    JSymbolTable moduleImports(JSymbolTable parent, Collection<ASTImportDeclaration> moduleImports) {
        if (moduleImports.isEmpty()) {
            return parent;
        }

        Set<String> lazyImportedModules = new HashSet<>();
        for (ASTImportDeclaration anImport : moduleImports) {
            assert anImport.isModuleImport() : "Expected module import: " + anImport;
            lazyImportedModules.add(anImport.getImportedName());
        }

        ShadowChainBuilder<JTypeMirror, ScopeInfo>.ResolverBuilder importedTypes = TYPES.new ResolverBuilder();

        ShadowChainNode<JTypeMirror, ScopeInfo> types = TYPES.shadowWithCache(
                typeNode(parent),
                ScopeInfo.MODULE_IMPORT,
                importedTypes.getMutableMap(),
                JavaResolvers.moduleImport(lazyImportedModules, processor, thisPackage)
        );

        return SymbolTableImpl.withTypes(parent, types);
    }


    JSymbolTable singleImportsSymbolTable(JSymbolTable parent, Collection<ASTImportDeclaration> singleImports) {
        if (singleImports.isEmpty()) {
            return parent;
        }

        List<NameResolver<? extends JTypeMirror>> importedStaticTypes = new ArrayList<>();
        List<NameResolver<? extends JVariableSig>> importedStaticFields = new ArrayList<>();
        List<NameResolver<? extends JMethodSig>> importedStaticMethods = new ArrayList<>();

        NameResolver<JTypeMirror> types = fillSingleImports(singleImports);
        fillSingleStaticImports(singleImports, importedStaticTypes, importedStaticFields, importedStaticMethods);

        return buildTable(
            parent,
            VARS.shadow(varNode(parent), ScopeInfo.SINGLE_IMPORT, NameResolver.composite(importedStaticFields)),
            METHODS.shadow(methodNode(parent), ScopeInfo.SINGLE_IMPORT, NameResolver.composite(importedStaticMethods)),
            TYPES.augment(
                TYPES.shadow(typeNode(parent), ScopeInfo.SINGLE_IMPORT, types),
                false,
                ScopeInfo.SINGLE_IMPORT,
                NameResolver.composite(importedStaticTypes)
            )
        );

    }


    private NameResolver<JTypeMirror> fillSingleImports(Iterable<ASTImportDeclaration> singleImports) {

        Map<String, ASTImportDeclaration> importedTypes = new HashMap<>();
        for (ASTImportDeclaration anImport : singleImports) {
            if (anImport.isImportOnDemand()) {
                throw new IllegalArgumentException(anImport.toString());
            }

            if (!anImport.isStatic()) {
                // Single-Type-Import Declaration
                importedTypes.put(anImport.getImportedSimpleName(), anImport);
            }
        }
        return new NameResolver<JTypeMirror>() {
            @Override
            public @NonNull List<JTypeMirror> resolveHere(String simpleName) {
                ASTImportDeclaration anImport = importedTypes.get(simpleName);
                if (anImport != null) {
                    JClassSymbol sym = processor.findSymbolCannotFail(anImport, anImport.getImportedName());
                    return Collections.singletonList(sym.getTypeSystem().typeOf(sym, false));
                }
                return Collections.emptyList();
            }

            @Override
            public boolean isDefinitelyEmpty() {
                return importedTypes.isEmpty();
            }
        };
    }

    private void fillSingleStaticImports(Iterable<ASTImportDeclaration> singleImports,
                                         List<NameResolver<? extends JTypeMirror>> importedTypes,
                                         List<NameResolver<? extends JVariableSig>> importedFields,
                                         List<NameResolver<? extends JMethodSig>> importedStaticMethods) {
        for (ASTImportDeclaration anImport : singleImports) {
            if (anImport.isImportOnDemand()) {
                throw new IllegalArgumentException(anImport.toString());
            }

            String simpleName = anImport.getImportedSimpleName();
            String name = anImport.getImportedName();

            if (anImport.isStatic()) {
                // Single-Static-Import Declaration
                // types, fields or methods having the same name

                int idx = name.lastIndexOf('.');
                if (idx < 0) {
                    continue; // invalid syntax
                }
                String className = name.substring(0, idx);
                class LazyGetter implements Supplier<JClassType> {
                    JClassType type;
                    boolean tried;

                    public JClassType get() {
                        if (tried) {
                            return type;
                        }
                        tried = true;
                        JClassSymbol containerClass = loadClassReportFailure(anImport, className);

                        if (containerClass != null) {
                            type = (JClassType) containerClass.getTypeSystem().declaration(containerClass);
                        }
                        return type;
                    }

                    @Override
                    public String toString() {
                        return className;
                    }
                }
                LazyGetter lazyGetter = new LazyGetter();
                importedStaticMethods.add(JavaResolvers.staticImportMethodResolver(lazyGetter, thisPackage, simpleName));
                importedFields.add(JavaResolvers.staticImportFieldResolver(lazyGetter, thisPackage, simpleName));
                importedTypes.add(JavaResolvers.staticImportClassResolver(lazyGetter, thisPackage, simpleName));
            }
        }
    }

    JSymbolTable javaLangSymTable(JSymbolTable parent) {
        return typesInPackage(parent, "java.lang", ScopeInfo.JAVA_LANG);
    }

    JSymbolTable moduleImportJavaBase(JSymbolTable parent) {
        ShadowChainBuilder<JTypeMirror, ScopeInfo>.ResolverBuilder importedTypes = TYPES.new ResolverBuilder();

        ShadowChainNode<JTypeMirror, ScopeInfo> types = TYPES.shadowWithCache(
                typeNode(parent),
                ScopeInfo.SIMPLE_COMPILATION_UNIT,
                importedTypes.getMutableMap(),
                JavaResolvers.moduleImport(Collections.singleton("java.base"), processor, thisPackage)
        );

        return SymbolTableImpl.withTypes(parent, types);
    }

    JSymbolTable importsOnDemandJavaIo(JSymbolTable parent) {
        StaticImportOnDemandWalks statics = new StaticImportOnDemandWalks();
        statics.staticImportedNames.add("java.io.IO");

        return buildTable(
            parent,
            VARS.shadow(varNode(parent), IMPORT_ON_DEMAND, name -> statics.getVars().resolveHere(name)),
            METHODS.shadow(methodNode(parent), IMPORT_ON_DEMAND, name -> statics.getMethods().resolveHere(name)),
            TYPES.shadow(typeNode(parent), SIMPLE_COMPILATION_UNIT, name1 -> statics.getTypes().resolveHere(name1))
        );
    }

    JSymbolTable samePackageSymTable(JSymbolTable parent) {
        return typesInPackage(parent, thisPackage, ScopeInfo.SAME_PACKAGE);
    }

    @NonNull
    private JSymbolTable typesInPackage(JSymbolTable parent, String packageName, ScopeInfo scopeTag) {
        assert isValidJavaPackageName(packageName) : "Not a package name: " + packageName;

        return SymbolTableImpl.withTypes(
            parent,
            TYPES.augmentWithCache(typeNode(parent), true, scopeTag, JavaResolvers.packageResolver(processor, packageName))
        );
    }

    JSymbolTable typeBody(JSymbolTable parent, @NonNull JClassType t) {

        Pair<NameResolver<JTypeMirror>, NameResolver<JVariableSig>> inherited = JavaResolvers.inheritedMembersResolvers(t);

        JClassSymbol sym = t.getSymbol();

        ShadowChainNode<JTypeMirror, ScopeInfo> types = typeNode(parent);
        if (!sym.isAnonymousClass()) {
            types = TYPES.shadow(types, ScopeInfo.ENCLOSING_TYPE, t);                                                        // self name
        }
        types = TYPES.shadow(types, ScopeInfo.INHERITED, inherited.getLeft());                                           // inherited classes (note they shadow the enclosing type)
        types = TYPES.shadow(types, ScopeInfo.ENCLOSING_TYPE_MEMBER, TYPES.groupByName(t.getDeclaredClasses()));       // inner classes declared here
        types = TYPES.shadow(types, ScopeInfo.TYPE_PARAM, TYPES.groupByName(sym.getTypeParameters()));                   // type parameters

        ShadowChainNode<JVariableSig, ScopeInfo> fields = varNode(parent);
        fields = VARS.shadow(fields, ScopeInfo.INHERITED, inherited.getRight());
        fields = VARS.shadow(fields, ScopeInfo.ENCLOSING_TYPE_MEMBER, VARS.groupByName(t.getDeclaredFields()));

        ShadowChainNode<JMethodSig, ScopeInfo> methods = methodNode(parent);
        BinaryOperator<List<JMethodSig>> merger = JavaResolvers.methodMerger(Modifier.isStatic(t.getSymbol().getModifiers()));
        methods = METHODS.augmentWithCache(methods, false, ScopeInfo.METHOD_MEMBER, JavaResolvers.subtypeMethodResolver(t), merger);

        return buildTable(parent, fields, methods, types);
    }

    JSymbolTable typesInFile(JSymbolTable parent, NodeStream<ASTTypeDeclaration> decls) {
        return SymbolTableImpl.withTypes(parent, TYPES.shadow(typeNode(parent), SAME_FILE, TYPES.groupByName(decls, ASTTypeDeclaration::getTypeMirror)));
    }

    JSymbolTable selfType(JSymbolTable parent, JClassType sym) {
        return SymbolTableImpl.withTypes(parent, TYPES.shadow(typeNode(parent), ScopeInfo.ENCLOSING_TYPE_MEMBER, TYPES.groupByName(sym)));
    }

    JSymbolTable typeHeader(JSymbolTable parent, JClassSymbol sym) {
        return SymbolTableImpl.withTypes(parent, TYPES.shadow(typeNode(parent), ScopeInfo.TYPE_PARAM, TYPES.groupByName(sym.getTypeParameters())));
    }

    /**
     * Symbol table for a body declaration. This places a shadowing
     * group for variables, ie, nested variable shadowing groups will
     * be merged into it but not into the parent. This implements shadowing
     * of fields by local variables and formals.
     */
    JSymbolTable bodyDeclaration(JSymbolTable parent, JClassType enclosing, @Nullable ASTFormalParameters formals, @Nullable ASTTypeParameters tparams) {
        return new SymbolTableImpl(
            VARS.shadow(varNode(parent), ScopeInfo.FORMAL_PARAM, VARS.groupByName(ASTList.orEmptyStream(formals), fp -> {
                ASTVariableId varId = fp.getVarId();
                if (varId.isUnnamed()) {
                    return null;
                }
                JVariableSymbol sym = varId.getSymbol();
                return sym.getTypeSystem().sigOf(enclosing, (JFormalParamSymbol) sym);
            })),
            TYPES.shadow(typeNode(parent), ScopeInfo.TYPE_PARAM, TYPES.groupByName(ASTList.orEmptyStream(tparams), ASTTypeParameter::getTypeMirror)),
            methodNode(parent)
        );
    }

    JSymbolTable recordCtor(JSymbolTable parent, JClassType recordType, JConstructorSymbol symbol) {
        return SymbolTableImpl.withVars(parent, VARS.shadow(varNode(parent), FORMAL_PARAM, VARS.groupByName(symbol.getFormalParameters(), s -> s.getTypeSystem().sigOf(recordType, s))));
    }

    /**
     * Local vars are merged into the parent shadowing group. They don't
     * shadow other local vars, they conflict with them.
     */
    JSymbolTable localVarSymTable(JSymbolTable parent, JClassType enclosing, Iterable<ASTVariableId> ids) {
        List<JVariableSig> sigs = new ArrayList<>();
        for (ASTVariableId id : ids) {
            if (id.isUnnamed()) {
                continue;
            }
            sigs.add(id.getTypeSystem().sigOf(enclosing, (JLocalVariableSymbol) id.getSymbol()));
        }
        return SymbolTableImpl.withVars(parent, VARS.augment(varNode(parent), false, ScopeInfo.LOCAL, VARS.groupByName(sigs)));
    }

    JSymbolTable localVarSymTable(JSymbolTable parent, JClassType enclosing, ASTVariableId id) {
        assert !id.isField();
        if (id.isUnnamed()) { // checks language version already
            return parent;
        }
        return SymbolTableImpl.withVars(parent, VARS.augment(varNode(parent), false, ScopeInfo.LOCAL, id.getTypeSystem().sigOf(enclosing, (JLocalVariableSymbol) id.getSymbol())));
    }

    JSymbolTable localTypeSymTable(JSymbolTable parent, JClassType sym) {
        // TODO is this really not a shadow barrier?
        return SymbolTableImpl.withTypes(parent, TYPES.augment(typeNode(parent), false, ScopeInfo.LOCAL, sym));
    }

}
