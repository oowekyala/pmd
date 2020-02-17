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

import org.checkerframework.checker.nullness.qual.NonNull;

import net.sourceforge.pmd.lang.javadoc.ast.HtmlTagBehaviour.TagBehaviourImpl.AutocloseBehaviour;
import net.sourceforge.pmd.lang.javadoc.ast.HtmlTagBehaviour.TagBehaviourImpl.VoidBehaviour;


/**
 * Encodes behaviour like whether an element's end tag can be omitted.
 *
 * TODO support optional start tag?
 *
 * See https://html.spec.whatwg.org/multipage/syntax.html#optional-tags
 */
enum HtmlTagBehaviour {
    UNKNOWN(autoclosedBy(false)),

    AREA(voidElement()),
    BASE(voidElement()),
    BR(voidElement()),
    COL(voidElement()),
    EMBED(voidElement()),
    HR(voidElement()),
    IMG(voidElement()),
    INPUT(voidElement()),
    LINK(voidElement()),
    META(voidElement()),
    PARAM(voidElement()),
    SOURCE(voidElement()),
    TRACK(voidElement()),
    WBR(voidElement()),


    LI(autoclosedBy("li")),
    DT(autoclosedBy(false, "dt", "dd")),
    DD(autoclosedBy("dt", "dd")),
    RT(autoclosedBy("rt", "rp")),
    RP(autoclosedBy("rt", "rp")),
    OPTGROUP(autoclosedBy("optgroup")),
    OPTION(autoclosedBy("option", "optgroup")),
    COLGROUP(autoclosedBy("option", "optgroup")),

    TBODY(autoclosedBy("tbody", "tfoot")),
    TFOOT,
    TR(autoclosedBy("tr")),
    TD(autoclosedBy("td", "th")),
    TH(autoclosedBy("td", "th")),

    P(autoclosedBy("address", "article", "aside", "blockquote",
                   "details", "div", "dl", "fieldset", "figcaption",
                   "figure", "footer", "form",
                   "h1", "h2", "h3", "h4", "h5", "h6",
                   "header", "hgroup", "hr", "main", "menu", "nav",
                   "ol", "p", "pre", "section", "table", "ul", "li")) {

        private Set<String> exceptions = HtmlTagBehaviour.setOf("a", "audio", "del", "ins", "map", "noscript", "video");

        @Override
        boolean shouldCloseBecauseParentIsEnded(String parentTag) {
            return !exceptions.contains(HtmlTagBehaviour.toLower(parentTag));
        }
    },
    ;

    private static final Map<String, HtmlTagBehaviour> LOOKUP;
    private final String tagName;
    private final TagBehaviourImpl impl;


    static {
        Map<String, HtmlTagBehaviour> map = new HashMap<>();
        for (HtmlTagBehaviour value : values()) {
            map.put(value.getTagName(), value);
        }

        LOOKUP = Collections.unmodifiableMap(map);
    }


    HtmlTagBehaviour(String... autoclosedBy) {
        this(autoclosedBy(autoclosedBy));
    }

    HtmlTagBehaviour(boolean closedByClosedParent, String... autoclosedBy) {
        this(autoclosedBy(closedByClosedParent, autoclosedBy));
    }

    HtmlTagBehaviour(TagBehaviourImpl impl) {
        this.tagName = toLower(name());
        this.impl = impl;
    }

    @NonNull
    private static String toLower(String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    boolean shouldCloseBecauseParentIsEnded(String parentTag) {
        return impl.shouldCloseBecauseParentIsEnded(parentTag);
    }

    boolean shouldCloseBecauseTagIsStarting(String startingTag) {
        return impl.shouldCloseBecauseTagIsStarting(startingTag);
    }

    boolean isVoid() {
        return impl.isVoid();
    }

    public String getTagName() {
        return tagName;
    }


    private static Set<String> setOf(String... strings) {
        HashSet<String> hashSet = new HashSet<>(strings.length);
        Collections.addAll(hashSet, strings);
        return Collections.unmodifiableSet(hashSet);
    }

    /** Case insensitive. */
    public static HtmlTagBehaviour lookup(String tagName) {
        return LOOKUP.getOrDefault(toLower(tagName), UNKNOWN);
    }


    static TagBehaviourImpl autoclosedBy(String... closers) {
        return autoclosedBy(true, closers);
    }

    static TagBehaviourImpl voidElement() {
        return VoidBehaviour.INSTANCE;
    }

    static TagBehaviourImpl autoclosedBy(boolean closedByClosedParent, String... closers) {
        return new AutocloseBehaviour(closedByClosedParent, setOf(closers));
    }

    static abstract class TagBehaviourImpl {


        boolean shouldCloseBecauseParentIsEnded(String parentTag) {
            return false;
        }


        boolean shouldCloseBecauseTagIsStarting(String startingTag) {
            return false;
        }


        boolean isVoid() {
            return false;
        }


        static class VoidBehaviour extends TagBehaviourImpl {

            private static final VoidBehaviour INSTANCE = new VoidBehaviour();

            @Override
            boolean isVoid() {
                return true;
            }
        }

        static class AutocloseBehaviour extends TagBehaviourImpl {

            private final Set<String> autoclosedBy;
            private final boolean closedByClosedParent;

            AutocloseBehaviour(boolean closedByClosedParent, Set<String> autoclosedBy) {
                this.autoclosedBy = autoclosedBy;
                this.closedByClosedParent = closedByClosedParent;
            }

            @Override
            public boolean shouldCloseBecauseParentIsEnded(String parentTag) {
                return closedByClosedParent;
            }

            @Override
            public boolean shouldCloseBecauseTagIsStarting(String startingTag) {
                return autoclosedBy.contains(toLower(startingTag));
            }

        }
    }


}
