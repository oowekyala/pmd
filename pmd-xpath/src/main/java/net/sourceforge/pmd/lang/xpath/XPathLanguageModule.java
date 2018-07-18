/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath;

import net.sourceforge.pmd.lang.BaseLanguageModule;
import net.sourceforge.pmd.lang.xpath.rule.XPathRuleChainVisitor;


/**
 * @author sergey.gorbaty
 */
public class XPathLanguageModule extends BaseLanguageModule {

    public static final String NAME = "XML Path Language";
    public static final String SHORT_NAME = "XPath";
    public static final String TERSE_NAME = "xpath";


    public XPathLanguageModule() {
        super(NAME, SHORT_NAME, TERSE_NAME, XPathRuleChainVisitor.class, "page", "component");
        addVersion("3.0", new XPathHandler(), true);
    }
}
