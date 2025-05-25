/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.COMMENT_DATA;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.NonNull;

import net.sourceforge.pmd.lang.javadoc.ast.InlineTagParser.BlockTagParser;
import net.sourceforge.pmd.lang.javadoc.ast.JdocBlockTag.JdocSimpleBlockTag;
import net.sourceforge.pmd.lang.javadoc.ast.JdocBlockTag.JdocUnknownBlockTag;

/**
 * Lists all known inline tags and encapsulates the logic to parse them.
 *
 * @author Clément Fournier
 */
enum KnownBlockTagParser implements BlockTagParser {
    // those are only followed by regular comment data
    RETURN("@return"),
    AUTHOR("@author"),
    SINCE("@since"),
    DEPRECATED("@deprecated"),
    IMPL_NOTE("@implNote"),
    IMPL_SPEC("@implSpec"),

    SEE("@see") {
        @Override
        public JdocBlockTag parse(String name, MainJdocParser parser) {
            JdocSimpleBlockTag tag = new JdocSimpleBlockTag(name);
            JdocToken tokBeforeRef = parser.head();
            if (parser.nextNonWs() && parser.tokIs(COMMENT_DATA) && !parser.head().getImageCs().startsWith('"', 0)) {
                parser.parseReference(tokBeforeRef, tag);
            }
            return tag;
        }
    },

    // +1 name + comment data
    PARAM("@param"), // todo

    // +1 class ref + comment data
    EXCEPTION("@exception") {
        @Override
        public JdocBlockTag parse(String name, MainJdocParser parser) {
            JdocSimpleBlockTag tag = new JdocSimpleBlockTag(name);
            JdocToken tokBeforeRef = parser.head();
            if (parser.nextNonWs()) {
                parser.parseReference(tokBeforeRef, tag);
            }
            return tag;
        }
    },
    THROWS("@throws") {
        @Override
        public JdocBlockTag parse(String name, MainJdocParser parser) {
            return EXCEPTION.parse(name, parser);
        }
    },


    ;

    static final BlockTagParser UNKNOWN_PARSER = new BlockTagParser() {
        @Override
        public String getName() {
            return "@";
        }

        @Override
        public JdocBlockTag parse(String name, MainJdocParser parser) {
            parser.advance();
            return new JdocUnknownBlockTag(name);
        }
    };


    private static final Map<String, BlockTagParser> LOOKUP = Arrays.stream(values()).collect(Collectors.toMap(KnownBlockTagParser::getName, p -> p));
    private final String name;

    KnownBlockTagParser(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public JdocBlockTag parse(String name, MainJdocParser parser) {
        parser.nextNonWs();
        return new JdocSimpleBlockTag(JavadocNodeId.SIMPLE_BLOCK_TAG, name);
    }

    static @NonNull JdocBlockTag selectAndParse(String name, MainJdocParser parser) {
        BlockTagParser tagParser = LOOKUP.getOrDefault(name, UNKNOWN_PARSER);
        assert tagParser != null;
        return tagParser.parse(name, parser);
    }

}
