/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.plsql;

import net.sourceforge.pmd.lang.BaseLanguageModule;

/**
 * Created by christoferdutz on 20.09.14.
 */
public class PLSQLLanguageModule extends BaseLanguageModule {

    public static final String NAME = "PL/SQL";
    public static final String TERSE_NAME = "plsql";

    public PLSQLLanguageModule() {
        super(NAME, "PLSQL", TERSE_NAME,
              "sql",
              "trg",  // Triggers
              "prc", "fnc", // Standalone Procedures and Functions
              "pld", // Oracle*Forms
              "pls", "plh", "plb", // Packages
              "pck", "pks", "pkh", "pkb", // Packages
              "typ", "tyb", // Object Types
              "tps", "tpb" // Object Types
        );
        addSingleVersion(new PLSQLHandler());
    }
}
