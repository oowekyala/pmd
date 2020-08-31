/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.lua;

import net.sourceforge.pmd.lang.BaseLanguageModule;
import net.sourceforge.pmd.lang.CpdOnlyHandler;
import net.sourceforge.pmd.lang.lua.cpd.LuaTokenizer;

/**
 * Language implementation for Lua
 */
public class LuaLanguage extends BaseLanguageModule {

    public static final String ID = "lua";

    /**
     * Creates a new Lua Language instance.
     */
    public LuaLanguage() {
        super("Lua", "lua", ID, "lua");
        addSingleVersion(new CpdOnlyHandler(LuaTokenizer::new));
    }
}
