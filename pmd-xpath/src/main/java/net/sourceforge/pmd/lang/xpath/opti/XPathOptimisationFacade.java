/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.opti;

import java.io.StringReader;
import java.util.Map;

import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.LanguageVersionHandler;
import net.sourceforge.pmd.lang.xpath.ast.ASTXPathRoot;
import net.sourceforge.pmd.properties.PropertyDescriptor;


/**
 * @author Clément Fournier
 * @since 6.7.0
 */
public class XPathOptimisationFacade {

    private final LanguageVersion version;


    public XPathOptimisationFacade(LanguageVersion version) {
        this.version = version;
    }

    public XPathOptimisationFacade() {
        version = LanguageRegistry.findLanguageByTerseName("xpath").getDefaultVersion();
    }

    private ASTXPathRoot parse(String expression) {
        LanguageVersionHandler lvh = version.getLanguageVersionHandler();
        ASTXPathRoot root = (ASTXPathRoot) lvh.getParser(lvh.getDefaultParserOptions()).parse(":query:", new StringReader(expression));
        lvh.getSymbolFacade().start(root);
        return root;
    }


    public XPathQuery makeQuery(String expression,
                                Map<PropertyDescriptor<?>, Object> propertyMap) {

        return new XPathQueryImpl(parse(expression), propertyMap);
    }


}
