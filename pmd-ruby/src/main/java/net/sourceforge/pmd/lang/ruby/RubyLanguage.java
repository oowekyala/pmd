/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ruby;

import net.sourceforge.pmd.lang.ruby.cpd.RubyTokenizer;
import net.sourceforge.pmd.lang.BaseLanguageModule;
import net.sourceforge.pmd.lang.CpdOnlyHandler;

/**
 * Language implementation for Ruby.
 *
 * @author Zev Blut zb@ubit.com
 */
public class RubyLanguage extends BaseLanguageModule {

    public static final String ID = "ruby";

    /**
     * Creates a new Ruby Language instance.
     */
    public RubyLanguage() {
        super("Ruby", "ruby", ID, "rb", "cgi", "class");
        addSingleVersion(new CpdOnlyHandler(RubyTokenizer::new));
    }
}
