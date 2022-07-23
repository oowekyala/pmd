/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.test.schema;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.io.StringReader;

import org.hamcrest.MatcherAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.xml.sax.InputSource;

import net.sourceforge.pmd.lang.PlainTextLanguage;
import net.sourceforge.pmd.lang.rule.MockRule;

/**
 * @author Clément Fournier
 */
public class TestSchemaParserTest {

    @Rule
    public final SystemErrRule errStreamCaptor = new SystemErrRule().muteForSuccessfulTests();


    @Test
    public void testSchemaSimple() throws IOException {
        String file = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                      + "<test-data\n"
                      + "        xmlns=\"http://pmd.sourceforge.net/rule-tests\"\n"
                      + "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                      + "        xsi:schemaLocation=\"http://pmd.sourceforge.net/rule-tests net/sourceforge/pmd/test/schema/rule-tests_1_0_0.xsd\">\n"
                      + "    <test-code>\n"
                      + "        <description>equality operators with Double.NaN</description>\n"
                      + "        <expected-problems>4</expected-problems>\n"
                      + "        <code><![CDATA[\n"
                      + "            public class Foo {\n"
                      + "            }\n"
                      + "            ]]></code>\n"
                      + "    </test-code>\n"
                      + "    <test-code>\n"
                      + "        <description>equality operators with Float.NaN</description>\n"
                      + "        <expected-problems>4</expected-problems>\n"
                      + "        <code><![CDATA[\n"
                      + "            public class Foo {\n"
                      + "            }\n"
                      + "            ]]></code>\n"
                      + "    </test-code>\n"
                      + "</test-data>\n";

        RuleTestCollection parsed = parseFile(file);

        assertEquals(2, parsed.getTests().size());

    }

    @Test
    public void testSchemaDeprecatedAttr() throws IOException {
        String file = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                      + "<test-data\n"
                      + "        xmlns=\"http://pmd.sourceforge.net/rule-tests\"\n"
                      + "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                      + "        xsi:schemaLocation=\"http://pmd.sourceforge.net/rule-tests net/sourceforge/pmd/test/schema/rule-tests_1_0_0.xsd\">\n"
                      + "    <test-code regressionTest='false'>\n"
                      + "        <description>equality operators with Double.NaN</description>\n"
                      + "        <expected-problems>4</expected-problems>\n"
                      + "        <code><![CDATA[\n"
                      + "            public class Foo {\n"
                      + "            }\n"
                      + "            ]]></code>\n"
                      + "    </test-code>\n"
                      + "</test-data>\n";

        errStreamCaptor.enableLog();
        RuleTestCollection parsed = parseFile(file);

        assertEquals(1, parsed.getTests().size());
        MatcherAssert.assertThat(errStreamCaptor.getLog(), containsString(" 6|     <test-code regressionTest='false'>\n"
                                                                          + "                   ^^^^^^^^^^^^^^ Attribute 'regressionTest' is deprecated, use 'ignored' with inverted value\n"));
    }

    @Test
    public void testUnknownProperty() throws IOException {
        String file = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<test-data\n"
                + "        xmlns=\"http://pmd.sourceforge.net/rule-tests\"\n"
                + "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                + "        xsi:schemaLocation=\"http://pmd.sourceforge.net/rule-tests net/sourceforge/pmd/test/schema/rule-tests_1_0_0.xsd\">\n"
                + "    <test-code>\n"
                + "        <description>equality operators with Double.NaN</description>\n"
                + "        <rule-property name='invalid_property'>foo</rule-property>\n"
                + "        <expected-problems>0</expected-problems>\n"
                + "        <code><![CDATA[\n"
                + "            public class Foo {\n"
                + "            }\n"
                + "            ]]></code>\n"
                + "    </test-code>\n"
                + "</test-data>\n";

        errStreamCaptor.enableLog();
        assertThrows(IllegalStateException.class, () -> parseFile(file));

        MatcherAssert.assertThat(errStreamCaptor.getLog(), containsString("  8|         <rule-property name='invalid_property'>foo</rule-property>\n"
              + "                            ^^^^ Unknown property, known property names are violationSuppressRegex, violationSuppressXPath, testIntProperty\n"));
    }

    @Test
    public void testLangProperties() throws IOException {
        String file = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<test-data\n"
                + "        xmlns=\"http://pmd.sourceforge.net/rule-tests\"\n"
                + "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                + "        xsi:schemaLocation=\"http://pmd.sourceforge.net/rule-tests net/sourceforge/pmd/test/schema/rule-tests_1_0_0.xsd\">\n"
                + "    <test-code>\n"
                + "        <description>equality operators with Double.NaN</description>\n"
                + "        <lang-properties>\n"
                + "            <property name='invalid_property'>foo</property>\n"
                + "        </lang-properties>\n"
                + "        <expected-problems>0</expected-problems>\n"
                + "        <code><![CDATA[\n"
                + "            public class Foo {\n"
                + "            }\n"
                + "            ]]></code>\n"
                + "    </test-code>\n"
                + "</test-data>\n";

        errStreamCaptor.enableLog();
        assertThrows(IllegalStateException.class, () -> parseFile(file));

        MatcherAssert.assertThat(errStreamCaptor.getLog(), containsString(
                "  9|             <property name='invalid_property'>foo</property>\n"
              + "                           ^^^^ Unknown property, known property names are auxClasspath\n"));
    }

    private RuleTestCollection parseFile(String file) throws IOException {
        MockRule mockRule = new MockRule();
        mockRule.setLanguage(PlainTextLanguage.getInstance());

        InputSource is = new InputSource();
        is.setSystemId("a/file.xml");
        is.setCharacterStream(new StringReader(file));

        return new TestSchemaParser().parse(mockRule, is);
    }


}
