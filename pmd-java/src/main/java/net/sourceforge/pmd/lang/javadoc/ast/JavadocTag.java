/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public enum JavadocTag {

    AUTHOR("author", "Authors of the source code, in chronological order"),
    SINCE("since", "Version of the source code that this item was introduced, can be a number or a date"),
    VERSION("version", "Current version number of the source code"),
    DEPRECATED("deprecated", "Indicates that an item is a member of the deprecated API"),
    PARAM("param", " "),
    THROWS("throws", " "),
    RETURN("returns", " "),
    SEE("see", " ");

    /*
    POST("post", " "),
    PRE("pre", " "),
    RETURN("return", " "),
    INV("inv", " "),
    INVARIANT("invariant", " "),
    PATTERN("pattern", " "),
    SERIAL("serial", " "),
    SERIAL_DATA("serialData", " "),
    SERIAL_FIELD("serialField", " "),
    GENERATED("generated", " "),
    GENERATED_BY("generatedBy", " ");
    */

    private static final Map<String, JavadocTag> TAGS_BY_ID = new HashMap<>();

    public final String label;
    public final String description;

    static {
        for (JavadocTag tag : values()) {
            TAGS_BY_ID.put(tag.label, tag);
        }
    }

    JavadocTag(String theLabel, String theDescription) {
        label = theLabel;
        description = theDescription;
    }

    public static JavadocTag tagFor(String id) {
        return TAGS_BY_ID.get(id);
    }

    public static Set<String> allTagIds() {
        return TAGS_BY_ID.keySet();
    }
}
