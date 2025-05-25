/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.INLINE_TAG_END;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.TAG_NAME;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocCommentData;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocMalformed;
import net.sourceforge.pmd.lang.javadoc.ast.JdocInlineTag.JdocInheritDoc;
import net.sourceforge.pmd.lang.javadoc.ast.JdocInlineTag.JdocLink;
import net.sourceforge.pmd.lang.javadoc.ast.JdocInlineTag.JdocLiteral;
import net.sourceforge.pmd.lang.javadoc.ast.JdocInlineTag.JdocUnknownInlineTag;
import net.sourceforge.pmd.lang.javadoc.ast.JdocInlineTag.JdocValue;
import net.sourceforge.pmd.lang.javadoc.ast.MainJdocParser.TagParser;

/**
 * Lists all known inline tags and encapsulates the logic to parse them.
 *
 * @author Clément Fournier
 */
enum KnownInlineTagParser implements TagParser {
    LINK("@link") {
        @Override
        public AbstractJavadocNode parse(String name, MainJdocParser parser) {
            parser.advance();
            parser.skipWhitespace();
            JdocToken firstTok = parser.head().getPrevious();
            assert firstTok != null;

            JdocLink tag = new JdocLink(name);
            JdocToken firstLabelTok = parser.parseReference(firstTok, tag);
            if (firstLabelTok != null) {
                while (!parser.tokIs(INLINE_TAG_ENDERS) && parser.advance()) {
                    // skip
                }
                JdocCommentData label = new JdocCommentData(firstLabelTok, parser.head().prev);
                tag.appendChild(label);
            }

            return tag;
        }
    },

    LINKPLAIN("@linkplain") {
        @Override
        public AbstractJavadocNode parse(String name, MainJdocParser parser) {
            return LINK.parse(name, parser);
        }
    },

    CODE("@code") {
        @Override
        public AbstractJavadocNode parse(String name, MainJdocParser parser) {
            parser.advance();
            String data = consumeInlineTag(parser);
            return new JdocLiteral(name, data);
        }
    },

    LITERAL("@literal") {
        @Override
        public AbstractJavadocNode parse(String name, MainJdocParser parser) {
            return CODE.parse(name, parser);
        }
    },

    VALUE("@value") {
        @Override
        public AbstractJavadocNode parse(String name, MainJdocParser parser) {
            parser.advance();
            parser.skipWhitespace();
            JdocToken firstTok = parser.head().getPrevious();
            assert firstTok != null;

            JdocValue tag = new JdocValue(name);
            JdocToken firstLabelTok = parser.parseReference(firstTok, tag);
            if (firstLabelTok != null) {
                expectInlineTagEnd(parser, tag);
            }
            return tag;
        }

    },

    INHERIT_DOC("@inheritDoc") {
        @Override
        public AbstractJavadocNode parse(String name, MainJdocParser parser) {
            JdocInheritDoc tag = new JdocInheritDoc(name);
            parser.advance();
            expectInlineTagEnd(parser, tag);
            return tag;
        }
    };

    private static final EnumSet<JdocTokenType> INLINE_TAG_ENDERS = EnumSet.of(INLINE_TAG_END, TAG_NAME); // TAG_NAME is the start of a block tag

    private static final Map<String, KnownInlineTagParser> LOOKUP = Arrays.stream(values()).collect(Collectors.toMap(KnownInlineTagParser::getName, p -> p));
    private final String name;

    KnownInlineTagParser(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Nullable // case sensitive
    static KnownInlineTagParser lookup(String name) {
        return LOOKUP.get(name);
    }

    public static JdocUnknownInlineTag parseUnknown(String name, MainJdocParser parser) {
        parser.advance();
        String data = consumeInlineTag(parser);
        return new JdocUnknownInlineTag(name, data);
    }

    @NonNull
    private static String consumeInlineTag(MainJdocParser parser) {
        StringBuilder builder = new StringBuilder();
        parser.consumeUntil(it -> INLINE_TAG_ENDERS.contains(it.getKind()),
                            it -> it.getKind().isSignificant(),
                            tok -> builder.append(tok.getImage()));
        return builder.toString();
    }

    private static void expectInlineTagEnd(MainJdocParser parser, JdocInlineTag tag) {
        parser.skipWhitespace();
        if (!parser.tokIs(INLINE_TAG_ENDERS)) {
            tag.appendChild(new JdocMalformed(JdocTokenType.EMPTY_SET, parser.head()));
            while (!parser.tokIs(INLINE_TAG_ENDERS) && parser.advance()) {
                // skip
            }
        }
    }
}
