/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc;

import org.checkerframework.checker.nullness.qual.NonNull;

import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.ast.test.BaseParsingHelper;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocLanguage;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocComment;

/**
 *
 */
public class JavadocParsingHelper extends BaseParsingHelper<JavadocParsingHelper, JdocComment> {

    public static final JavadocParsingHelper DEFAULT = new JavadocParsingHelper(Params.getDefault());

    private final Language myLanguage = new JavadocLanguage();

    public JavadocParsingHelper(Params params) {
        super(JavadocLanguage.NAME, JdocComment.class, params);
    }

    @Override
    public @NonNull Language getLanguage() {
        return myLanguage;
    }

    @Override
    protected @NonNull JavadocParsingHelper clone(@NonNull Params params) {
        return new JavadocParsingHelper(params);
    }
}
