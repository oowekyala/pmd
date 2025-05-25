/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

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
    RETURN("@return"),
    AUTHOR("@author"),
    SINCE("@since"),
    DEPRECATED("@deprecated"),
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

    @NonNull
    static JdocBlockTag selectAndParse(String name, MainJdocParser parser) {
        BlockTagParser tagParser = LOOKUP.getOrDefault(name, UNKNOWN_PARSER);
        assert tagParser != null;
        return tagParser.parse(name, parser);
    }

}
