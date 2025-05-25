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

import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocCommentData;
import net.sourceforge.pmd.lang.javadoc.ast.JdocInlineTag.JdocInheritDoc;
import net.sourceforge.pmd.lang.javadoc.ast.JdocInlineTag.JdocLink;
import net.sourceforge.pmd.lang.javadoc.ast.JdocInlineTag.JdocLiteral;
import net.sourceforge.pmd.lang.javadoc.ast.JdocInlineTag.JdocSnippet;
import net.sourceforge.pmd.lang.javadoc.ast.JdocInlineTag.JdocUnknownInlineTag;
import net.sourceforge.pmd.lang.javadoc.ast.JdocInlineTag.JdocValue;

/**
 * Lists all known inline tags and encapsulates the logic to parse them.
 *
 * @author Clément Fournier
 */
enum KnownInlineTagParser implements InlineTagParser {
    LINK("@link") {
        @Override
        public JdocInlineTag parse(String name, MainJdocParser parser) {
            parser.nextNonWs();
            JdocToken firstTok = parser.head().getPrevious();
            assert firstTok != null;

            JdocLink tag = new JdocLink(name);
            JdocToken firstLabelTok = parser.parseReference(firstTok, tag);
            if (firstLabelTok != null) {
                while (!parser.tokIsAny(INLINE_TAG_ENDERS) && parser.advance()) {
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
        public JdocInlineTag parse(String name, MainJdocParser parser) {
            return LINK.parse(name, parser);
        }
    },

    CODE("@code") {
        @Override
        public JdocInlineTag parse(String name, MainJdocParser parser) {
            parser.advance();
            String data = consumeInlineTag(parser);
            return new JdocLiteral(name, data);
        }
    },

    LITERAL("@literal") {
        @Override
        public JdocInlineTag parse(String name, MainJdocParser parser) {
            return CODE.parse(name, parser);
        }
    },

    /*
     TODO
      Displays constant values. When the {@value} tag is used without
      an argument in the documentation comment of a static field, it
      displays the value of that constant
    */

    VALUE("@value") {
        @Override
        public JdocInlineTag parse(String name, MainJdocParser parser) {
            parser.nextNonWs();
            JdocToken firstTok = parser.head().getPrevious();
            assert firstTok != null;

            JdocValue tag = new JdocValue(name);
            parser.parseReference(firstTok, tag);
            expectInlineTagEnd(parser, tag);
            return tag;
        }

    },

    SNIPPET("@snippet") {
        @Override
        public JdocInlineTag parse(String name, MainJdocParser parser) {
            parser.nextNonWs();
            JdocToken firstTok = parser.head().getPrevious();
            assert firstTok != null;

            String data = consumeInlineTag(parser);
            return new JdocSnippet(data);
        }

    },

    // TODO DocRoot tag

    INHERIT_DOC("@inheritDoc") {
        @Override
        public JdocInlineTag parse(String name, MainJdocParser parser) {
            JdocInheritDoc tag = new JdocInheritDoc(name);
            parser.advance();
            expectInlineTagEnd(parser, tag);
            return tag;
        }
    };


    static final InlineTagParser UNKNOWN_PARSER = new InlineTagParser() {
        @Override
        public String getName() {
            return "@";
        }

        @Override
        public JdocInlineTag parse(String name, MainJdocParser parser) {
            parser.advance();
            String data = consumeInlineTag(parser);
            return new JdocUnknownInlineTag(name, data);
        }
    };

    private static final EnumSet<JdocTokenType> INLINE_TAG_ENDERS = EnumSet.of(INLINE_TAG_END, TAG_NAME); // TAG_NAME is the start of a block tag
    private static final Map<String, InlineTagParser> LOOKUP = Arrays.stream(values()).collect(Collectors.toMap(KnownInlineTagParser::getName, p -> p));
    private final String name;

    KnownInlineTagParser(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    static @NonNull JdocInlineTag selectAndParse(String name, MainJdocParser parser) {
        InlineTagParser tagParser = LOOKUP.getOrDefault(name, UNKNOWN_PARSER);
        assert tagParser != null;
        return tagParser.parse(name, parser);
    }

    private static @NonNull String consumeInlineTag(MainJdocParser parser) {
        StringBuilder builder = new StringBuilder();
        parser.consumeUntil(it -> INLINE_TAG_ENDERS.contains(it.getKind()),
                            it -> it.getKind().isSignificant(),
                            tok -> builder.append(tok.getImage()));
        return builder.toString();
    }

    private static void expectInlineTagEnd(MainJdocParser parser, JdocInlineTag tag) {
        parser.skipWhitespace();
        if (!parser.tokIsAny(INLINE_TAG_ENDERS)) {
            tag.newError(EnumSet.of(INLINE_TAG_END), parser.head());
            while (!parser.tokIsAny(INLINE_TAG_ENDERS) && parser.advance()) {
                // skip
            }
        }
    }
}
