/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author Clément Fournier
 */
public class NodeMetaModel<T extends JavaNode> {

    private final Class<T> nodeClass;

    protected NodeMetaModel(Class<T> nodeClass) {
        this.nodeClass = nodeClass;
    }

    public T cast(JavaNode node) {
        return nodeClass.cast(node);
    }

    public final void write(JavaNode node, DataOutputStream out) throws IOException {
        writeAttributes(nodeClass.cast(node), out);
    }

    public final void readInto(JavaNode node, DataInputStream in) throws IOException {
        readAttributes(nodeClass.cast(node), in);
    }

    /**
     * WriteAttribute {@link #readAttributes(JavaNode, DataInputStream)} must
     * be absolutely compatible, ie read and write exactly the same amount of
     * bytes. In particular they must also both always call *super*
     */
    protected void writeAttributes(T node, DataOutputStream out) throws IOException {
        // to be overridden
        //        out.writeInt(node.getBeginLine());
        //        out.writeInt(node.getEndLine());
    }

    protected void readAttributes(T node, DataInputStream in) throws IOException {
        // to be overridden
        //        in.readInt();
        //        in.readInt();
    }

    protected static void writeNullableStr(DataOutputStream dos, @Nullable String s) throws IOException {
        dos.writeBoolean(s == null);
        if (s != null) {
            dos.writeUTF(s);
        }
    }

    @Nullable
    protected static String readNullableStr(DataInputStream dos) throws IOException {
        boolean isNull = dos.readBoolean();
        return isNull ? null : dos.readUTF();
    }

    protected static <T extends Enum<T>> void writeEnum(@NonNull DataOutputStream dos, T t) throws IOException {
        if (t.getDeclaringClass().getEnumConstants().length < Byte.MAX_VALUE) {
            // 0-128
            dos.writeByte(t.ordinal());
        } else {
            dos.writeInt(t.ordinal());
        }
    }

    protected static <T extends Enum<T>> T readEnum(DataInputStream dos, Class<T> tClass) throws IOException {
        T[] constants = tClass.getEnumConstants();
        if (constants.length < Byte.MAX_VALUE) {
            // 0-128
            return constants[dos.readByte()];
        } else {
            return constants[dos.readInt()];
        }
    }

    public static <T extends JavaNode> NodeMetaModel<T> neverNullImage(Class<T> tClass) {
        return new NodeMetaModel<T>(tClass) {

            @Override
            protected void writeAttributes(T node, DataOutputStream out) throws IOException {
                super.writeAttributes(node, out);
                out.writeUTF(Objects.requireNonNull(node.getImage(),
                                                    "This serializer for " + node + " expects a never-null image"));
            }

            @Override
            protected void readAttributes(T node, DataInputStream in) throws IOException {
                super.readAttributes(node, in);
                node.setImage(in.readUTF());
            }
        };
    }

    public static <T extends JavaNode, E extends Enum<E>> NodeMetaModel<T> singleEnum(Class<T> tClass, Class<E> eClass, Function<T, E> getter, BiConsumer<T, E> setter) {
        return new NodeMetaModel<T>(tClass) {

            @Override
            protected void writeAttributes(T node, DataOutputStream out) throws IOException {
                super.writeAttributes(node, out);
                writeEnum(out, getter.apply(node));
            }

            @Override
            protected void readAttributes(T node, DataInputStream in) throws IOException {
                super.readAttributes(node, in);
                setter.accept(node, NodeMetaModel.readEnum(in, eClass));
            }
        };
    }

}
