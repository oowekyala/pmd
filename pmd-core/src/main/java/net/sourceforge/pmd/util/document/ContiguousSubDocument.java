/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.util.document;

import java.util.Objects;

import net.sourceforge.pmd.lang.LanguageVersion;

/**
 *
 */
final class ContiguousSubDocument extends BaseMappedDocument {

    private final LanguageVersion languageVersion;
    private final int start;
    private final Chars text;

    ContiguousSubDocument(TextDocument base,
                          LanguageVersion languageVersion,
                          TextRegion region) {
        super(base);
        this.languageVersion = Objects.requireNonNull(languageVersion);
        this.start = region.getStartOffset();
        this.text = base.sliceTranslatedText(region);
    }

    @Override
    protected int localOffsetTransform(int outOffset, boolean inclusive) {
        return outOffset + start;
    }

    @Override
    public LanguageVersion getLanguageVersion() {
        return languageVersion;
    }

    @Override
    public Chars getText() {
        return text;
    }
}
