/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.ast.NodeStream;
import net.sourceforge.pmd.lang.java.symbols.table.JSymbolTable;

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
            return (JdocClassRef) jjtGetChild(0);
        }
    }

    /**
     * A reference to a java class in javadoc.
     */
    class JdocClassRef extends AbstractJavadocNode implements JdocRef {

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

    }

    /**
     * A reference to a field of a class in javadoc.
     */
    class JdocFieldRef extends AbstractJavadocNode implements JdocMemberRef {

        JdocFieldRef(JdocClassRef classRef, JdocToken fieldName) {
            super(JavadocNodeId.FIELD_REF);
            jjtAddChild(classRef, 0);
            setFirstToken(classRef.getFirstToken());
            setLastToken(fieldName);
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
    class JdocExecutableRef extends AbstractJavadocNode implements JdocMemberRef {

        private final String name;

        public JdocExecutableRef(JdocClassRef classRef, JdocToken nametok) {
            super(JavadocNodeId.EXECUTABLE_REF);
            jjtAddChild(classRef, 0);
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
