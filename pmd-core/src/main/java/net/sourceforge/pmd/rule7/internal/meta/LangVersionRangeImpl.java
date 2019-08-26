/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.rule7.internal.meta;

import java.util.Objects;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.rule7.RuleDescriptor.LanguageVersionRange;

public class LangVersionRangeImpl implements LanguageVersionRange {

    private final Language lang;
    private final @Nullable LanguageVersion min;
    private final @Nullable LanguageVersion max;

    public LangVersionRangeImpl(Language lang, @Nullable LanguageVersion min, @Nullable LanguageVersion max) {
        this.lang = Objects.requireNonNull(lang, "Language cannot be null");
        this.min = min;
        this.max = max;
    }

    @Override
    public Language getLanguage() {
        return lang;
    }

    @Override
    public @Nullable LanguageVersion getMin() {
        return min;
    }

    @Override
    public @Nullable LanguageVersion getMax() {
        return max;
    }

    @Override
    public LanguageVersionRange withMin(LanguageVersion ver) {
        if (max != null && ver != null && ver.compareTo(max) >= 0) { // min >= max
            throw new IllegalArgumentException("Improper range [" + ver + ", " + max + "[");
        } else if (ver != null && !ver.getLanguage().equals(lang)) {
            throw new IllegalArgumentException("Improper version " + ver + " for language " + lang);
        }
        return new LangVersionRangeImpl(lang, ver, max);
    }

    @Override
    public LanguageVersionRange withMax(LanguageVersion ver) {
        if (min != null && ver != null && ver.compareTo(min) < 0) { // max < min
            throw new IllegalArgumentException("Improper range [" + min + ", " + ver + "[");
        } else if (ver != null && !ver.getLanguage().equals(lang)) {
            throw new IllegalArgumentException("Improper version " + ver + " for language " + lang);
        }
        return new LangVersionRangeImpl(lang, min, ver);
    }

    @Override
    public String toString() {
        if (min == null && max == null) {
            return "{" + lang + "}";
        } else if (min == null) {
            return "{ v in " + lang + " | v < " + max + "}";
        } else if (max == null) {
            return "{ v in " + lang + " | " + min + " <= v }";
        } else {
            return "{ v in " + lang + " | " + min + " <= v < " + max + " }";
        }
    }

    @Override
    public boolean contains(LanguageVersion version) {
        return getLanguage().equals(version.getLanguage())
            && (getMin() == null || getMin().compareTo(version) <= 0)
            && (getMax() == null || getMax().compareTo(version) > 0);
    }
}
