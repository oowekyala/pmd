/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.util.document;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.LanguageVersion;

/**
 * A text document built as a set of deltas over another document.
 */
final class FragmentedTextDocument extends BaseMappedDocument implements TextDocument {

    private final Fragment firstFragment;
    private final Chars text;

    FragmentedTextDocument(TextDocument base, Fragment firstFragment, Fragment lastFragment) {
        super(base);
        assert firstFragment != lastFragment; // NOPMD
        this.firstFragment = firstFragment;
        this.text = toChars(firstFragment, lastFragment);
    }

    private static Chars toChars(Fragment firstFragment, Fragment lastFragment) {
        StringBuilder sb = new StringBuilder(lastFragment.outEnd());
        Fragment f = firstFragment;
        while (f != null) {
            f.getChars().appendChars(sb);
            f = f.next;
        }
        return Chars.wrap(sb);
    }

    @Override
    protected int localOffsetTransform(int outOffset, boolean inclusive) {
        // todo this would be pretty slow when we're in the middle of some escapes
        // we could check save the fragment last accessed to speed it up, and look forwards & backwards
        return inputOffsetAt(outOffset, firstFragment, inclusive);
    }

    @Override
    public Chars getText() {
        return text;
    }


    @Override
    public LanguageVersion getLanguageVersion() {
        return base.getLanguageVersion();
    }


    static int inputOffsetAt(int outputOffset, @Nullable Fragment firstFragment, boolean inclusive) {
        Fragment f = firstFragment;
        if (f == null) {
            return outputOffset;
        }
        while (f.next != null && f.outEnd() < outputOffset) {
            f = f.next;
        }
        if (!inclusive && f.outEnd() == outputOffset) {
            if (f.next != null) {
                f = f.next;
                // fallthrough
            } else {
                return f.outToIn(outputOffset) + 1;
            }
        }
        return f.outToIn(outputOffset);
    }


    /**
     * A delta from the original text to the translated text. This maps
     * a region of the original document to some new characters.
     */
    static final class Fragment {

        private final Chars chars;

        final @Nullable Fragment prev;
        @Nullable Fragment next;

        private final int inLength;
        private final int outStart;
        private final int inStart;

        Fragment(@Nullable Fragment prev, int inLength, Chars chars) {
            this.chars = chars;
            this.prev = prev;
            this.inLength = inLength;
            if (prev != null) {
                prev.next = this;
                this.outStart = prev.outEnd();
                this.inStart = prev.inEnd();
            } else {
                this.outStart = 0;
                this.inStart = 0;
            }
        }

        public Chars getChars() {
            return chars;
        }

        int outStart() {
            return outStart;
        }

        int outLen() {
            return chars.length();
        }

        int outEnd() {
            return outStart() + outLen();
        }

        int inStart() {
            return inStart;
        }

        int inLen() {
            return inLength;
        }

        int inEnd() {
            return inStart() + inLen();
        }

        int outToIn(int outOffset) {
            return inStart() + (outOffset - outStart());
        }

        boolean contains(int outOffset) {
            return outStart() <= outOffset && outEnd() > outOffset;
        }

        @Override
        public String toString() {
            return "Fragment[" + inStart() + ".." + inEnd() + " -> " + outStart() + ".." + outEnd() + "]" + chars;
        }
    }
}