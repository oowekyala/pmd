/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.ast.NodeStream;
import net.sourceforge.pmd.lang.java.ast.ASTAnyTypeDeclaration;
import net.sourceforge.pmd.lang.java.symbols.JClassSymbol;
import net.sourceforge.pmd.lang.java.symbols.table.JSymbolTable;
import net.sourceforge.pmd.lang.java.symbols.table.coreimpl.NameResolver;
import net.sourceforge.pmd.lang.java.types.JClassType;
import net.sourceforge.pmd.lang.java.types.JTypeMirror;
import net.sourceforge.pmd.lang.java.types.JVariableSig;
import net.sourceforge.pmd.lang.java.types.JVariableSig.FieldSig;
import net.sourceforge.pmd.lang.java.types.TypeOps;

/**
 * A javadoc node that references a Java program element.
 */
public interface JdocRef extends JavadocNode {

    /**
     * A reference to a {@linkplain JdocFieldRef field} or {@linkplain JdocExecutableRef method or constructor}.
     */
    interface JdocMemberRef extends JdocRef {

        /**
         * Returns the reference to the owner class.
         */
        default JdocClassRef getOwnerClassRef() {
            return (JdocClassRef) getFirstChild();
        }
    }

    /**
     * A reference to a java class in javadoc.
     */
    final class JdocClassRef extends AbstractJavadocNode implements JdocRef {

        private int arrayDimensions;

        JdocClassRef(@Nullable JdocToken tok) {
            super(JavadocNodeId.CLASS_REF);
            setFirstToken(tok);
            setLastToken(tok);
        }

        /**
         * Returns the name of the class. This may be a simple name,
         * which needs to be resolved with a {@link JSymbolTable}.
         * This may also be empty, in which case the enclosing class
         * is implied.
         */
        public String getSimpleRef() {
            return getFirstToken().getImage();
        }

        /**
         * Resolve the reference using the java symbol table.
         */
        public @Nullable JTypeMirror resolveRef() {
            JSymbolTable symTable = getJavaSymbolTable();
            if (symTable == null) {
                return null;
            }

            String ref = getSimpleRef();
            if (ref.isEmpty()) {
                ASTAnyTypeDeclaration ctx = getRoot().getContextType();
                return ctx == null ? null : ctx.getTypeMirror();
            }
            JTypeMirror type = symTable.types().resolveFirst(ref);
            int arrayDims = getArrayDimensions();
            if (arrayDims > 0 && type != null) {
                type = type.getTypeSystem().arrayType(type, arrayDims);
            }
            return type;
        }

        /**
         * Returns whether this is an empty ref.
         */
        public boolean isEmptyForSelfClass() {
            return getSimpleRef().isEmpty();
        }

        void setArrayDims(int numDims) {
            this.arrayDimensions = numDims;
        }

        /**
         * The number of array dimensions of this type.
         */
        public int getArrayDimensions() {
            return arrayDimensions;
        }
    }

    /**
     * A reference to a field of a class in javadoc.
     */
    final class JdocFieldRef extends AbstractJavadocNode implements JdocMemberRef {

        private FieldSig resolved = null;

        JdocFieldRef(JdocClassRef classRef, JdocToken fieldName) {
            super(JavadocNodeId.FIELD_REF);
            addChild(classRef, 0);
            setFirstToken(classRef.getFirstToken());
            setLastToken(fieldName);
        }

        /**
         * Resolve the reference to this field. Returns null if it could not
         * be resolved.
         */
        public @Nullable JVariableSig resolveRef() {
            if (resolved != null) {
                return resolved;
            }
            JTypeMirror t = getOwnerClassRef().resolveRef();
            if (!(t instanceof JClassType)) {
                return null;
            }

            JdocComment root = getRoot();
            // null if we're eg in a package-info comment
            ASTAnyTypeDeclaration contextType = root.getContextType();
            String packageName = root.getPackageName();
            String fieldName = getName();
            if (packageName == null) {
                return null;
            }
            JClassSymbol enclosing = contextType == null ? null : contextType.getSymbol();
            NameResolver<FieldSig> fieldResolver = TypeOps.getMemberFieldResolver(t, packageName, enclosing, fieldName);

            resolved = fieldResolver.resolveFirst(fieldName);
            return resolved;
        }

        /**
         * Returns the name of the field.
         */
        public String getName() {
            return getLastToken().getImage();
        }
    }

    /**
     * A reference to a method or constructor of a class in javadoc.
     */
    final class JdocExecutableRef extends AbstractJavadocNode implements JdocMemberRef {

        private final String name;

        JdocExecutableRef(JdocClassRef classRef, JdocToken nametok) {
            super(JavadocNodeId.EXECUTABLE_REF);
            addChild(classRef, 0);
            setFirstToken(classRef.getFirstToken());
            name = nametok.getImage();
        }

        public NodeStream<JdocClassRef> getParamRefs() {
            return children(JdocClassRef.class).drop(1);
        }


        public String getName() {
            return name;
        }
    }
}
