/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc;

import org.checkerframework.checker.nullness.qual.NonNull;

import net.sourceforge.pmd.lang.PmdCapableLanguage;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocLanguage;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocComment;
import net.sourceforge.pmd.lang.test.ast.BaseParsingHelper;

/**
 *
 */
public class JavadocParsingHelper extends BaseParsingHelper<JavadocParsingHelper, JdocComment> {

    public static final JavadocParsingHelper DEFAULT = new JavadocParsingHelper(Params.getDefault());

    private final PmdCapableLanguage myLanguage = JavadocLanguage.INSTANCE;

    public JavadocParsingHelper(BaseParsingHelper.Params params) {
        super(JavadocLanguage.INSTANCE.getId(), JdocComment.class, params);
    }

    @Override
    public @NonNull PmdCapableLanguage getLanguage() {
        return myLanguage;
    }

    @Override
    protected @NonNull JavadocParsingHelper clone(@NonNull Params params) {
        return new JavadocParsingHelper(params);
    }
}
