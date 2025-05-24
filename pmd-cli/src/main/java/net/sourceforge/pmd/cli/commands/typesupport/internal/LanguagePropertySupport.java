/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.cli.commands.typesupport.internal;

import static net.sourceforge.pmd.cli.commands.typesupport.internal.LanguagePropertySupport.LanguagePropertySpec;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

import net.sourceforge.pmd.AbstractConfiguration;
import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.LanguagePropertyBundle;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertySource;

import picocli.CommandLine;
import picocli.CommandLine.ITypeConverter;

public class LanguagePropertySupport implements ITypeConverter<LanguagePropertySpec>, Iterable<String> {

    public static final String PARAM_LABEL = "<lang:name=value>";
    public static final String DESCRIPTION =
            "Set a property of a language.";

    private final LanguageTypeSupport languageTypeSupport;

    public LanguagePropertySupport(LanguageTypeSupport languageTypeSupport) {
        this.languageTypeSupport = languageTypeSupport;
    }

    public static final class PmdLanguagePropertySupport extends LanguagePropertySupport {
        public PmdLanguagePropertySupport() {
            super(new PmdLanguageTypeSupport());
        }
    }

    public static final class CpdLanguagePropertySupport extends LanguagePropertySupport {
        public CpdLanguagePropertySupport() {
            super(new CpdLanguageTypeSupport());
        }
    }

    @Override
    public @NonNull Iterator<String> iterator() {
        return languageTypeSupport.getLanguageRegistry().getLanguages().stream()
                .flatMap(lang -> {
                    LanguagePropertyBundle bundle = lang.newPropertyBundle();
                    return bundle.getPropertyDescriptors().stream()
                            .map(PropertyDescriptor::name)
                            .map(it -> lang.getId() + ":" + it);
                })
                .iterator();
    }

    @Override
    public LanguagePropertySpec convert(String value) {
        String[] parts = value.split(":");
        if (parts.length != 2
            || StringUtils.isBlank(parts[0])
            || StringUtils.isBlank(parts[1])) {
            throw new CommandLine.TypeConversionException(
                    "Expected property name "
                    + "in the format `<langId>:<name>`, "
                    + "eg `java:auxClasspath`, but got " + value);
        }
        LanguagePropertySpec spec = new LanguagePropertySpec();
        if ("*".equals(parts[0])) {
            // set property on all languages that have it
            for (String langId : languageTypeSupport) {
                Language lang = languageTypeSupport.convert(langId);
                PropertyDescriptor<?> desc = lang.newPropertyBundle().getPropertyDescriptor(parts[1]);
                if (desc != null) {
                    spec.addLang(lang, desc);
                }
            }
        } else {
            // may throw if language not known
            Language lang = languageTypeSupport.convert(parts[0]);
            LanguagePropertyBundle bundle = lang.newPropertyBundle();
            PropertyDescriptor<?> desc = bundle.getPropertyDescriptor(parts[1]);
            if (desc == null) {
                throw new CommandLine.TypeConversionException(
                        "Language property named `" + parts[1]
                        + "` does not exist on language `" + parts[0] + "`. "
                        + "Available properties are " + getAvailableProperties(bundle));
            }
            spec.addLang(lang, desc);
        }

        return spec;
    }

    private static String getAvailableProperties(LanguagePropertyBundle bundle) {
        return bundle.getPropertyDescriptors().stream()
                .map(PropertyDescriptor::name)
                .sorted()
                .collect(Collectors.joining(", "));
    }

    public static final class LanguagePropertySpec {
        public final Map<Language, PropertyDescriptor<?>> langToProperties = new HashMap<>();

        private LanguagePropertySpec() {
        }

        void addLang(Language language, PropertyDescriptor<?> desc) {
            langToProperties.put(language, desc);
        }

        public void configure(AbstractConfiguration config, String value) {
            langToProperties.forEach((lang, prop) -> {

                LanguagePropertyBundle bundle = config.getLanguageProperties(lang);
                assert bundle.hasDescriptor(prop);

                setPropertyCapture(bundle, prop, value);
            });
        }

        public void validate(CommandLine command, String value)
                throws CommandLine.ParameterException {
            langToProperties.forEach((lang, prop) -> {

                try {
                    prop.serializer().fromString(value);
                } catch (IllegalArgumentException e) {
                    throw new CommandLine.ParameterException(command,
                            "Value `" + value + "` is invalid for property `"
                            + lang.getId() + ":" + prop.name() + "`: " + e.getMessage());
                }
            });
        }


        private <T> void setPropertyCapture(PropertySource rule,
                                            PropertyDescriptor<T> descriptor,
                                            String valueStr) {
            T value = descriptor.serializer().fromString(valueStr);
            rule.setProperty(descriptor, value);
        }
    }
}
