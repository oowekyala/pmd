/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.lang.java.ast.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.lang.java.ast.ASTImportDeclaration;

/**
 * Helper class to analyze {@link ASTImportDeclaration}s.
 */
public final class ImportWrapper {
    private static final Logger LOG = LoggerFactory.getLogger(ImportWrapper.class);

    private final ASTImportDeclaration node;
    private final String name;
    private final String fullname;
    private final Set<String> allStaticDemands;

    public ImportWrapper(ASTImportDeclaration node) {
        this.node = node;
        this.fullname = node.getImportedName();
        this.name = node.getImportedSimpleName();
        this.allStaticDemands = collectStaticFieldsAndMethods(node);
    }

    /**
     * @param node
     */
    private Set<String> collectStaticFieldsAndMethods(ASTImportDeclaration node) {
        if (!isStaticOnDemand() || node == null) {
            return Collections.emptySet();
        }

        // This was edited during the grammar updating process, because
        // ImportDeclaration is not a TypeNode anymore, and there is no Name anymore.
        // If tests are failing, refer to the history of this file to get the
        // previously working version.
        Class<?> type = null;
        if (node != null) {
            type = node.getRoot().getClassTypeResolver().loadClassOrNull(node.getImportedName());
        }

        if (type == null) {
            return Collections.emptySet();
        }

        try {
            Set<String> names = new HashSet<>();
            while (type != null) {
                // consider static fields, public and non-public
                for (Field f : type.getDeclaredFields()) {
                    if (Modifier.isStatic(f.getModifiers())) {
                        names.add(f.getName());
                    }
                }
                // and methods, too
                for (Method m : type.getDeclaredMethods()) {
                    if (Modifier.isStatic(m.getModifiers())) {
                        names.add(m.getName());
                    }
                }

                // consider statics of super classes as well
                type = type.getSuperclass();
            }
            return names;
        } catch (LinkageError e) {
            // This is an incomplete classpath, report the missing class
            LOG.debug("Possible incomplete auxclasspath: Error while processing imports", e);
            return Collections.emptySet();
        }
    }



    public boolean matches(String fullName, String name) {
        if (isStaticOnDemand()) {
            if (allStaticDemands.contains(fullName)) {
                return true;
            }
        }
        if (this.name == null && name == null) {
            return fullName.equals(fullname);
        }
        return name.equals(this.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ImportWrapper that = (ImportWrapper) o;
        return Objects.equals(node.isStatic(), that.node.isStatic())
            && Objects.equals(isOnDemand(), that.isOnDemand())
            && Objects.equals(node.getImportedName(), that.node.getImportedName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(node.isStatic(), node.isImportOnDemand(), node.getImportedName());
    }

    public String getName() {
        return name;
    }

    public String getPackageName() {
        return node.getPackageName();
    }

    public String getFullName() {
        return fullname;
    }

    public ASTImportDeclaration getNode() {
        return node;
    }

    public boolean isStaticOnDemand() {
        return node.isStatic() && node.isImportOnDemand();
    }

    @Override
    public String toString() {
        return "Import[name=" + name + ",fullname=" + fullname + ",static*=" + isStaticOnDemand() + ']';
    }

    public boolean isOnDemand() {
        return node.isImportOnDemand();
    }
}
