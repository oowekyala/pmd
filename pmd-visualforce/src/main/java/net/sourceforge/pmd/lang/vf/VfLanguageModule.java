/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.vf;

import net.sourceforge.pmd.lang.BaseLanguageModule;


/**
 * @author sergey.gorbaty
 *
 */
public class VfLanguageModule extends BaseLanguageModule {

    public static final String NAME = "Salesforce VisualForce";
    public static final String TERSE_NAME = "vf";

    public VfLanguageModule() {
        super(NAME, "VisualForce", TERSE_NAME, "page", "component");
        addSingleVersion(new VfHandler());
    }
}
