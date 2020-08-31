/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.cpd;

import java.io.IOException;

import net.sourceforge.pmd.properties.AbstractPropertySource;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import net.sourceforge.pmd.util.document.TextDocument;

public interface Tokenizer {


    PropertyDescriptor<Boolean> CASE_SENSITIVE =
        PropertyFactory.booleanProperty("caseSensitive")
                       .desc("Case sensitive matching (Apex only)")
                       .defaultValue(false)
                       .build();
    PropertyDescriptor<Boolean> IGNORE_LITERALS =
        PropertyFactory.booleanProperty("ignoreLiterals")
                       .desc("Anonymize literal values (Java only)")
                       .defaultValue(false)
                       .build();

    PropertyDescriptor<Boolean> IGNORE_IDENTIFIERS =
        PropertyFactory.booleanProperty("ignoreIdentifiers")
                       .desc("Anonymize identifiers (Java only)")
                       .defaultValue(false)
                       .build();

    PropertyDescriptor<Boolean> IGNORE_ANNOTATIONS =
        PropertyFactory.booleanProperty("ignoreAnnotations")
                       .desc("Remove annotations from the token stream (Java only)")
                       .defaultValue(false)
                       .build();

    PropertyDescriptor<Boolean> IGNORE_IMPORTS =
        PropertyFactory.booleanProperty("ignoreImports") // maybe it makes sense to generalize this to other languages
                       .desc("Remove imports from the token stream (C# only, which ignores using directives)")
                       .defaultValue(false)
                       .build();


    String NO_SKIP_BLOCKS = "";
    String DEFAULT_SKIP_BLOCKS_PATTERN = "#if 0|#endif";

    PropertyDescriptor<String> SKIP_PROC_DIRECTIVES =
        PropertyFactory.stringProperty("skipCppProcessorDirectives")
                       .desc("Remove code sections found between the given delimiters (separated by a pipe, |) (C++ only)")
                       .defaultValue(DEFAULT_SKIP_BLOCKS_PATTERN)
                       .build();

    final class CpdProperties extends AbstractPropertySource {

        public CpdProperties() {
            definePropertyDescriptor(SKIP_PROC_DIRECTIVES);
            definePropertyDescriptor(IGNORE_IMPORTS);
            definePropertyDescriptor(IGNORE_LITERALS);
            definePropertyDescriptor(IGNORE_ANNOTATIONS);
            definePropertyDescriptor(IGNORE_IDENTIFIERS);
            definePropertyDescriptor(CASE_SENSITIVE);
        }

        @Override
        protected String getPropertySourceType() {
            return "CPD tokenizer";
        }

        public <T> CpdProperties withProperty(PropertyDescriptor<T> descriptor, T value) {
            setProperty(descriptor, value);
            return this;
        }

        @Override
        public String getName() {
            return "none";
        }
    }

    default void setProperties(CpdProperties cpdProperties) {
        // to be overridden
    }

    default Tokenizer withProperties(CpdProperties cpdProperties) {
        setProperties(cpdProperties);
        return this;
    }

    void tokenize(TextDocument sourceCode, Tokens tokenEntries) throws IOException;
}
