/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Encodes behaviour like whether an element's end tag can be omitted.
 *
 * TODO support optional start tag
 *
 * See https://html.spec.whatwg.org/multipage/syntax.html#optional-tags
 */
enum HtmlTagBehaviour {
    UNKNOWN {
        @Override
        boolean shouldCloseBecauseParentIsEnded(String parentTag) {
            return false;
        }

        @Override
        boolean shouldCloseBecauseTagIsStarting(String startingTag) {
            return false;
        }
    },

    LI("li"),
    DT(false, "dt", "dd"),
    DD("dt", "dd"),
    RT("rt", "rp"),
    RP("rt", "rp"),
    OPTGROUP("optgroup"),
    OPTION("option", "optgroup"),
    COLGROUP("option", "optgroup"),

    TBODY("tbody", "tfoot"),
    TFOOT,
    TR("tr"),
    TD("td", "th"),
    TH("td", "th"),

    P("address", "article", "aside", "blockquote",
      "details", "div", "dl", "fieldset", "figcaption",
      "figure", "footer", "form",
      "h1", "h2", "h3", "h4", "h5", "h6",
      "header", "hgroup", "hr", "main", "menu", "nav",
      "ol", "p", "pre", "section", "table", "ul", "li") {

        private Set<String> exceptions = HtmlTagBehaviour.setOf("a", "audio", "del", "ins", "map", "noscript", "video");

        @Override
        boolean shouldCloseBecauseParentIsEnded(String parentTag) {
            return !exceptions.contains(parentTag);
        }
    },
    ;

    private static final Map<String, HtmlTagBehaviour> LOOKUP;
    private final String tagName;
    private final Set<String> autoclosedBy;
    private final boolean closedByClosedParent;


    static {
        Map<String, HtmlTagBehaviour> map = new HashMap<>();
        for (HtmlTagBehaviour value : values()) {
            map.put(value.getTagName(), value);
        }

        LOOKUP = Collections.unmodifiableMap(map);
    }


    HtmlTagBehaviour(String... autoclosedBy) {
        this(true, setOf(autoclosedBy));
    }

    HtmlTagBehaviour(boolean closedByClosedParent, String... autoclosedBy) {
        this(closedByClosedParent, setOf(autoclosedBy));
    }

    HtmlTagBehaviour(boolean closedByClosedParent, Set<String> autoclosedBy) {
        this.tagName = name().toLowerCase(Locale.ROOT);
        this.autoclosedBy = autoclosedBy;
        this.closedByClosedParent = closedByClosedParent;
    }

    boolean shouldCloseBecauseParentIsEnded(String parentTag) {
        return closedByClosedParent;
    }

    boolean shouldCloseBecauseTagIsStarting(String startingTag) {
        return autoclosedBy.contains(startingTag);
    }

    public String getTagName() {
        return tagName;
    }


    private static Set<String> setOf(String strings) {
        return Collections.singleton(strings);
    }

    private static Set<String> setOf(String... strings) {
        HashSet<String> hashSet = new HashSet<>(strings.length);
        Collections.addAll(hashSet, strings);
        return Collections.unmodifiableSet(hashSet);
    }


    public static HtmlTagBehaviour lookup(String tagName) {
        return LOOKUP.getOrDefault(tagName, UNKNOWN);
    }

}
