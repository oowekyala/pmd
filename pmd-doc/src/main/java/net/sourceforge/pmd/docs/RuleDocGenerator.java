/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.docs;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetLoadException;
import net.sourceforge.pmd.RuleSetLoader;
import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.rule.RuleReference;
import net.sourceforge.pmd.lang.rule.XPathRule;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.util.IOUtil;

public class RuleDocGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(RuleDocGenerator.class);

    private static final String GENERATED_WARNING = "<!-- DO NOT EDIT THIS FILE. This file is generated from file ${source}. -->";
    private static final String GENERATED_WARNING_NO_SOURCE = "<!-- DO NOT EDIT THIS FILE. This file is generated. -->";

    private static final String LANGUAGE_INDEX_FILENAME_PATTERN = "docs/pages/pmd/rules/${language.tersename}.md";
    private static final String LANGUAGE_INDEX_PERMALINK_PATTERN = "pmd_rules_${language.tersename}.html";
    private static final String RULESET_INDEX_FILENAME_PATTERN = "docs/pages/pmd/rules/${language.tersename}/${ruleset.name}.md";
    private static final String RULESET_INDEX_PERMALINK_PATTERN = "pmd_rules_${language.tersename}_${ruleset.name}.html";

    private static final String DEPRECATION_LABEL_SMALL = "<span style=\"border-radius: 0.25em; color: #fff; padding: 0.2em 0.6em 0.3em; display: inline; background-color: #d9534f; font-size: 75%;\">Deprecated</span> ";
    private static final String DEPRECATION_LABEL = "<span style=\"border-radius: 0.25em; color: #fff; padding: 0.2em 0.6em 0.3em; display: inline; background-color: #d9534f;\">Deprecated</span>";
    private static final String DEPRECATED_RULE_PROPERTY_MARKER = "deprecated!";

    private static final String GITHUB_SOURCE_LINK = "https://github.com/pmd/pmd/blob/master/";

    /** Maintains mapping from pmd terse language name to rouge highlighter language. */
    private static final Map<String, String> LANGUAGE_HIGHLIGHT_MAPPER = new HashMap<>();

    static {
        LANGUAGE_HIGHLIGHT_MAPPER.put("ecmascript", "javascript");
        LANGUAGE_HIGHLIGHT_MAPPER.put("pom", "xml");
        LANGUAGE_HIGHLIGHT_MAPPER.put("apex", "java");
        LANGUAGE_HIGHLIGHT_MAPPER.put("plsql", "sql");
    }

    private final Path root;
    private final FileWriter writer;

    /** Caches rule class name to java source file mapping. */
    private final Map<String, String> allRules = new HashMap<>();
    /** Caches ruleset to ruleset xml file mapping. */
    private final Map<String, String> allRulesets = new HashMap<>();


    public RuleDocGenerator(FileWriter writer, Path root) {
        this.writer = Objects.requireNonNull(writer, "A file writer must be provided");
        this.root = Objects.requireNonNull(root, "Root directory must be provided");

        Path docsDir = root.resolve("docs");
        if (!Files.exists(docsDir) || !Files.isDirectory(docsDir)) {
            throw new IllegalArgumentException("Couldn't find \"docs\" subdirectory");
        }
    }

    public void generate(List<RuleSet> registeredRulesets, List<String> additionalRulesets) throws IOException {
        Map<Language, List<RuleSet>> sortedRulesets;
        Map<Language, List<RuleSet>> sortedAdditionalRulesets;
        sortedRulesets = sortRulesets(registeredRulesets);
        sortedAdditionalRulesets = sortRulesets(resolveAdditionalRulesets(additionalRulesets));
        determineRuleClassSourceFiles(sortedRulesets);
        generateLanguageIndex(sortedRulesets, sortedAdditionalRulesets);
        generateRuleSetIndex(sortedRulesets);

        generateSidebar(sortedRulesets);
    }

    private void generateSidebar(Map<Language, List<RuleSet>> sortedRulesets) throws IOException {
        SidebarGenerator generator = new SidebarGenerator(writer, root);
        generator.generateSidebar(sortedRulesets);
    }

    private List<RuleSet> resolveAdditionalRulesets(List<String> additionalRulesets) {
        if (additionalRulesets == null) {
            return Collections.emptyList();
        }

        List<RuleSet> rulesets = new ArrayList<>();
        RuleSetLoader ruleSetLoader = new RuleSetLoader().warnDeprecated(false);
        for (String filename : additionalRulesets) {
            try {
                // do not take rulesets from pmd-test or pmd-core
                if (!filename.contains("pmd-test") && !filename.contains("pmd-core")) {
                    rulesets.add(ruleSetLoader.loadFromResource(filename));
                } else {
                    LOG.debug("Ignoring ruleset {}", filename);
                }
            } catch (RuleSetLoadException e) {
                // ignore rulesets, we can't read
                LOG.warn("ruleset file {} ignored ({})", filename, e.getMessage(), e);
            }
        }
        return rulesets;
    }

    private Path getAbsoluteOutputPath(String filename) {
        return root.resolve(IOUtil.normalizePath(filename));
    }

    private Map<Language, List<RuleSet>> sortRulesets(List<RuleSet> rulesets) {
        SortedMap<Language, List<RuleSet>> rulesetsByLanguage = rulesets.stream().collect(Collectors.groupingBy(RuleDocGenerator::getRuleSetLanguage,
                                                                                                                TreeMap::new,
                                                                                                                Collectors.toCollection(ArrayList::new)));

        for (List<RuleSet> rulesetsOfOneLanguage : rulesetsByLanguage.values()) {
            rulesetsOfOneLanguage.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
        }
        return rulesetsByLanguage;
    }

    /**
     * Rulesets could potentially contain rules from various languages.
     * But for built-in rulesets, all rules within one ruleset belong to
     * one language. So we take the language of the first rule.
     * @param ruleset
     * @return the terse name of the ruleset's language
     */
    private static Language getRuleSetLanguage(RuleSet ruleset) {
        Collection<Rule> rules = ruleset.getRules();
        if (rules.isEmpty()) {
            throw new RuntimeException("Ruleset " + ruleset.getFileName() + " is empty!");
        }
        return rules.iterator().next().getLanguage();
    }

    /**
     * Writes for each language an index file, which lists the rulesets, the rules
     * and links to the ruleset pages.
     * @param rulesets all registered/built-in rulesets
     * @param sortedAdditionalRulesets additional rulesets
     * @throws IOException
     */
    private void generateLanguageIndex(Map<Language, List<RuleSet>> rulesets, Map<Language, List<RuleSet>> sortedAdditionalRulesets) throws IOException {
        for (Map.Entry<Language, List<RuleSet>> entry : rulesets.entrySet()) {
            String languageTersename = entry.getKey().getTerseName();
            String filename = LANGUAGE_INDEX_FILENAME_PATTERN
                    .replace("${language.tersename}", languageTersename);
            Path path = getAbsoluteOutputPath(filename);

            List<String> lines = new LinkedList<>();
            lines.add("---");
            lines.add("title: " + entry.getKey().getName() + " Rules");
            lines.add("tags: [rule_references, " + languageTersename + "]");
            lines.add("summary: Index of all built-in rules available for " + entry.getKey().getName());
            lines.add("language_name: " + entry.getKey().getName());
            lines.add("permalink: " + LANGUAGE_INDEX_PERMALINK_PATTERN.replace("${language.tersename}", languageTersename));
            lines.add("folder: pmd/rules");
            lines.add("editmepath: false");
            lines.add("---");
            lines.add(GENERATED_WARNING_NO_SOURCE);

            for (RuleSet ruleset : entry.getValue()) {
                lines.add("## " + ruleset.getName());
                lines.add("");
                lines.add("{% include callout.html content=\"" + getRuleSetDescriptionSingleLine(ruleset) + "\" %}");
                lines.add("");

                for (Rule rule : getSortedRules(ruleset)) {
                    String link = RULESET_INDEX_PERMALINK_PATTERN
                            .replace("${language.tersename}", languageTersename)
                            .replace("${ruleset.name}", RuleSetUtils.getRuleSetFilename(ruleset));
                    if (rule instanceof RuleReference) {
                        RuleReference ref = (RuleReference) rule;
                        if (ruleset.getFileName().equals(ref.getRuleSetReference().getRuleSetFileName())) {
                            // rule renamed within same ruleset
                            lines.add("*   [" + rule.getName() + "](" + link + "#" + rule.getName().toLowerCase(Locale.ROOT) + "): "
                                    + DEPRECATION_LABEL_SMALL
                                    + "The rule has been renamed. Use instead "
                                    + "[" + ref.getRule().getName() + "](" + link + "#" + ref.getRule().getName().toLowerCase(Locale.ROOT) + ").");
                        } else {
                            // rule moved to another ruleset...
                            String otherLink = RULESET_INDEX_PERMALINK_PATTERN
                                    .replace("${language.tersename}", languageTersename)
                                    .replace("${ruleset.name}", RuleSetUtils.getRuleSetFilename(ref.getRuleSetReference().getRuleSetFileName()));
                            lines.add("*   [" + rule.getName() + "](" + link + "#" + rule.getName().toLowerCase(Locale.ROOT) + "): "
                                    + DEPRECATION_LABEL_SMALL
                                    + "The rule has been moved to another ruleset. Use instead "
                                    + "[" + ref.getRule().getName() + "](" + otherLink + "#" + ref.getRule().getName().toLowerCase(Locale.ROOT) + ").");
                        }
                    } else {
                        link += "#" + rule.getName().toLowerCase(Locale.ROOT);
                        lines.add("*   [" + rule.getName() + "](" + link + "): "
                                + (rule.isDeprecated() ? DEPRECATION_LABEL_SMALL : "")
                                + getShortRuleDescription(rule));
                    }
                }
                lines.add("");
            }

            List<RuleSet> additionalRulesetsForLanguage = sortedAdditionalRulesets.get(entry.getKey());
            if (additionalRulesetsForLanguage != null) {
                lines.add("## Additional rulesets");
                lines.add("");

                for (RuleSet ruleset : additionalRulesetsForLanguage) {
                    boolean deprecated = RuleSetUtils.isRuleSetDeprecated(ruleset);

                    String rulesetName = ruleset.getName() + " (`" + RuleSetUtils.getRuleSetClasspath(ruleset) + "`)";

                    if (!deprecated) {
                        lines.add("*   " + rulesetName + ":");
                        lines.add("");
                        lines.add("    " + getRuleSetDescriptionSingleLine(ruleset));
                        lines.add("");
                    } else {
                        lines.add("*   " + rulesetName + ":");
                        lines.add("");
                        lines.add("    " + DEPRECATION_LABEL_SMALL + " This ruleset is for backwards compatibility.");
                        lines.add("");
                    }

                    lines.add("    It contains the following rules:");
                    lines.add("");
                    StringBuilder rules = new StringBuilder();
                    for (Rule rule : getSortedRules(ruleset)) {
                        if (rules.length() == 0) {
                            rules.append("    ");
                        } else {
                            rules.append(", ");
                        }

                        Rule resolvedRule = RuleSetUtils.resolveRuleReferences(rule);
                        if (resolvedRule instanceof RuleReference) {
                            // Note: deprecated rulesets contain by definition only rule references
                            RuleReference ref = (RuleReference) resolvedRule;
                            String otherLink = RULESET_INDEX_PERMALINK_PATTERN
                                    .replace("${language.tersename}", languageTersename)
                                    .replace("${ruleset.name}", RuleSetUtils.getRuleSetFilename(ref.getRuleSetReference().getRuleSetFileName()));

                            rules.append("[").append(ref.getName()).append("](");
                            rules.append(otherLink).append("#").append(ref.getRule().getName().toLowerCase(Locale.ROOT)).append(")");
                        } else {
                            rules.append(rule.getName());
                        }
                    }
                    lines.add(rules.toString());
                    lines.add("");
                }
                lines.add("");
            }

            System.out.println("Generated " + path);
            writer.write(path, lines);
        }
    }

    /**
     * Shortens and escapes (for markdown) some special characters. Otherwise the shortened text
     * could contain some unfinished sequences.
     * @param rule
     * @return
     */
    private static String getShortRuleDescription(Rule rule) {
        String htmlEscaped = StringEscapeUtils.escapeHtml4(
            StringUtils.abbreviate(
                StringUtils.stripToEmpty(
                    rule.getDescription()
                        .replaceAll("\n+|\r+", " ")
                        .replaceAll("\\|", "\\\\|")
                        .replaceAll("`", "'")
                        .replaceAll("\\*", "")),
                100));
        return EscapeUtils.preserveRuleTagQuotes(htmlEscaped);
    }

    private static String getRuleSetDescriptionSingleLine(RuleSet ruleset) {
        String description = ruleset.getDescription();
        description = StringEscapeUtils.escapeHtml4(description);
        description = description.replaceAll("\\n|\\r", " ");
        description = StringUtils.stripToEmpty(description);
        return EscapeUtils.preserveRuleTagQuotes(description);
    }

    private static List<String> toLines(String s) {
        return Arrays.asList(s.split("\r\n|\n"));
    }

    /**
     * Generates for each ruleset a page. The page contains the details for each rule.
     *
     * @param rulesets all rulesets
     * @throws IOException
     */
    private void generateRuleSetIndex(Map<Language, List<RuleSet>> rulesets) throws IOException {
        for (Map.Entry<Language, List<RuleSet>> entry : rulesets.entrySet()) {
            Language language = entry.getKey();
            String languageTersename = language.getTerseName();
            String languageName = language.getName();
            for (RuleSet ruleset : entry.getValue()) {
                String rulesetFilename = RuleSetUtils.getRuleSetFilename(ruleset);
                String filename = RULESET_INDEX_FILENAME_PATTERN
                    .replace("${language.tersename}", languageTersename)
                    .replace("${ruleset.name}", rulesetFilename);

                Path path = getAbsoluteOutputPath(filename);

                String permalink = RULESET_INDEX_PERMALINK_PATTERN
                        .replace("${language.tersename}", languageTersename)
                        .replace("${ruleset.name}", rulesetFilename);
                String ruleSetSourceFilepath = "../" + allRulesets.get(ruleset.getFileName());

                List<String> lines = new LinkedList<>();
                lines.add("---");
                lines.add("title: " + ruleset.getName());
                lines.add("summary: " + getRuleSetDescriptionSingleLine(ruleset));
                lines.add("permalink: " + permalink);
                lines.add("folder: pmd/rules/" + languageTersename);
                lines.add("sidebaractiveurl: /" + LANGUAGE_INDEX_PERMALINK_PATTERN.replace("${language.tersename}", languageTersename));
                lines.add("editmepath: " + ruleSetSourceFilepath);
                lines.add("keywords: " + getRuleSetKeywords(ruleset));
                lines.add("language: " + languageName);
                lines.add("---");
                lines.add(GENERATED_WARNING.replace("${source}", ruleSetSourceFilepath));

                for (Rule rule : getSortedRules(ruleset)) {
                    lines.add("## " + rule.getName());
                    lines.add("");

                    if (rule instanceof RuleReference) {
                        RuleReference ref = (RuleReference) rule;
                        if (ruleset.getFileName().equals(ref.getRuleSetReference().getRuleSetFileName())) {
                            // rule renamed within same ruleset
                            lines.add(DEPRECATION_LABEL);
                            lines.add("");
                            lines.add("This rule has been renamed. Use instead: ["
                                    + ref.getRule().getName() + "](" + "#" + ref.getRule().getName().toLowerCase(Locale.ROOT) + ")");
                            lines.add("");
                        } else {
                            // rule moved to another ruleset
                            String otherLink = RULESET_INDEX_PERMALINK_PATTERN
                                    .replace("${language.tersename}", languageTersename)
                                    .replace("${ruleset.name}", RuleSetUtils.getRuleSetFilename(ref.getRuleSetReference().getRuleSetFileName()));
                            lines.add(DEPRECATION_LABEL);
                            lines.add("");
                            lines.add("The rule has been moved to another ruleset. Use instead: ["
                                    + ref.getRule().getName() + "](" + otherLink + "#" + ref.getRule().getName().toLowerCase(Locale.ROOT) + ")");
                            lines.add("");
                        }
                    }

                    if (rule.isDeprecated()) {
                        lines.add(DEPRECATION_LABEL);
                        lines.add("");
                    }
                    if (rule.getSince() != null) {
                        lines.add("**Since:** PMD " + rule.getSince());
                        lines.add("");
                    }
                    lines.add("**Priority:** " + rule.getPriority() + " (" + rule.getPriority().getPriority() + ")");
                    lines.add("");

                    if (rule.getMinimumLanguageVersion() != null) {
                        lines.add("**Minimum Language Version:** "
                                + rule.getLanguage().getName() + " " + rule.getMinimumLanguageVersion().getVersion());
                        lines.add("");
                    }

                    lines.addAll(EscapeUtils.escapeLines(toLines(stripIndentation(rule.getDescription()))));
                    lines.add("");

                    XPathRule xpathRule = asXPathRule(rule);
                    if (xpathRule != null) {
                        lines.add("**This rule is defined by the following XPath expression:**");
                        lines.add("``` xpath");
                        lines.addAll(toLines(StringUtils.stripToEmpty(xpathRule.getXPathExpression())));
                        lines.add("```");
                    } else {
                        lines.add("**This rule is defined by the following Java class:** "
                                + "[" + rule.getRuleClass() + "]("
                                + GITHUB_SOURCE_LINK + allRules.get(rule.getRuleClass())
                                + ")");
                    }
                    lines.add("");

                    if (!rule.getExamples().isEmpty()) {
                        lines.add("**Example(s):**");
                        lines.add("");
                        for (String example : rule.getExamples()) {
                            lines.add("``` " + mapLanguageForHighlighting(languageTersename));
                            lines.addAll(toLines("{%raw%}" + StringUtils.stripToEmpty(example) + "{%endraw%}"));
                            lines.add("```");
                            lines.add("");
                        }
                    }

                    List<PropertyDescriptor<?>> properties = new ArrayList<>(rule.getPropertyDescriptors());
                    // filter out standard properties
                    properties.remove(Rule.VIOLATION_SUPPRESS_REGEX_DESCRIPTOR);
                    properties.remove(Rule.VIOLATION_SUPPRESS_XPATH_DESCRIPTOR);
                    properties.remove(XPathRule.XPATH_DESCRIPTOR);
                    properties.remove(XPathRule.VERSION_DESCRIPTOR);

                    if (!properties.isEmpty()) {
                        lines.add("**This rule has the following properties:**");
                        lines.add("");
                        lines.add("|Name|Default Value|Description|");
                        lines.add("|----|-------------|-----------|");
                        for (PropertyDescriptor<?> propertyDescriptor : properties) {
                            String description = propertyDescriptor.description();
                            final boolean isDeprecated = isDeprecated(propertyDescriptor);
                            if (isDeprecated) {
                                description = description.substring(DEPRECATED_RULE_PROPERTY_MARKER.length());
                            }

                            String defaultValue = determineDefaultValueAsString(propertyDescriptor, rule, true);

                            lines.add("|"
                                    + EscapeUtils.escapeMarkdown(StringEscapeUtils.escapeHtml4(propertyDescriptor.name()))
                                    + "|"
                                    + EscapeUtils.escapeMarkdown(defaultValue)
                                    + "|"
                                    + EscapeUtils.escapeMarkdown((isDeprecated ? DEPRECATION_LABEL_SMALL : "") + StringEscapeUtils.escapeHtml4(description))
                                    + "|"
                            );
                        }
                        lines.add("");
                    }

                    if (properties.isEmpty()) {
                        lines.add("**Use this rule by referencing it:**");
                    } else {
                        lines.add("**Use this rule with the default properties by just referencing it:**");
                    }
                    lines.add("``` xml");
                    lines.add("<rule ref=\"category/" + languageTersename + "/" + rulesetFilename + ".xml/" + rule.getName() + "\" />");
                    lines.add("```");
                    lines.add("");

                    if (properties.stream().anyMatch(it -> !isDeprecated(it))) {
                        lines.add("**Use this rule and customize it:**");
                        lines.add("``` xml");
                        lines.add("<rule ref=\"category/" + languageTersename + "/" + rulesetFilename + ".xml/" + rule.getName() + "\">");
                        lines.add("    <properties>");
                        for (PropertyDescriptor<?> propertyDescriptor : properties) {
                            if (!isDeprecated(propertyDescriptor)) {
                                String defaultValue = determineDefaultValueAsString(propertyDescriptor, rule, false);
                                lines.add("        <property name=\"" + propertyDescriptor.name() + "\" value=\""
                                              + defaultValue + "\" />");
                            }
                        }
                        lines.add("    </properties>");
                        lines.add("</rule>");
                        lines.add("```");
                        lines.add("");
                    }
                }

                writer.write(path, lines);
                System.out.println("Generated " + path);
            }
        }
    }

    private XPathRule asXPathRule(Rule rule) {
        if (rule instanceof XPathRule) {
            return (XPathRule) rule;
        } else if (rule instanceof RuleReference && ((RuleReference) rule).getRule() instanceof XPathRule) {
            return (XPathRule) ((RuleReference) rule).getRule();
        }
        return null;
    }

    private static boolean isDeprecated(PropertyDescriptor<?> propertyDescriptor) {
        return propertyDescriptor.description() != null
            && propertyDescriptor.description().toLowerCase(Locale.ROOT).startsWith(DEPRECATED_RULE_PROPERTY_MARKER);
    }

    private <T> String determineDefaultValueAsString(PropertyDescriptor<T> propertyDescriptor, Rule rule, boolean pad) {
        String defaultValue = "";
        T realDefaultValue = rule.getProperty(propertyDescriptor);

        if (realDefaultValue != null) {
            defaultValue = propertyDescriptor.serializer().toString(realDefaultValue);
            if (pad && realDefaultValue instanceof Collection) {
                // surround the delimiter with spaces, so that the browser can wrap
                // the value nicely
                defaultValue = defaultValue.replaceAll(",", " , ");
            }
        }
        defaultValue = StringEscapeUtils.escapeHtml4(defaultValue);
        return defaultValue;
    }

    private static String stripIndentation(String description) {
        if (description == null || description.isEmpty()) {
            return "";
        }

        String stripped = StringUtils.stripStart(description, "\n\r");
        stripped = StringUtils.stripEnd(stripped, "\n\r ");

        int indentation = 0;
        int strLen = stripped.length();
        while (Character.isWhitespace(stripped.charAt(indentation)) && indentation < strLen) {
            indentation++;
        }

        String[] lines = stripped.split("\\n");
        String prefix = StringUtils.repeat(' ', indentation);
        StringBuilder result = new StringBuilder(stripped.length());

        if (StringUtils.isNotEmpty(prefix)) {
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (i > 0) {
                    result.append(StringUtils.LF);
                }
                result.append(StringUtils.removeStart(line, prefix));
            }
        } else {
            result.append(stripped);
        }
        return result.toString();
    }

    /**
     * Simply maps PMD languages to rouge languages
     *
     * @param languageTersename
     * @return
     * @see <a href="https://github.com/jneen/rouge/wiki/List-of-supported-languages-and-lexers">List of supported languages</a>
     */
    private static String mapLanguageForHighlighting(String languageTersename) {
        if (LANGUAGE_HIGHLIGHT_MAPPER.containsKey(languageTersename)) {
            return LANGUAGE_HIGHLIGHT_MAPPER.get(languageTersename);
        }
        return languageTersename;
    }

    private String getRuleSetKeywords(RuleSet ruleset) {
        List<String> ruleNames = new LinkedList<>();
        for (Rule rule : ruleset.getRules()) {
            ruleNames.add(rule.getName());
        }
        return ruleset.getName() + ", " + StringUtils.join(ruleNames, ", ");
    }

    private List<Rule> getSortedRules(RuleSet ruleset) {
        List<Rule> sortedRules = new ArrayList<>(ruleset.getRules());
        Collections.sort(sortedRules, new Comparator<Rule>() {
            @Override
            public int compare(Rule o1, Rule o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });
        return sortedRules;
    }

    /**
     * Walks through the root directory once to get all rule source file path names and ruleset names.
     * This provides the information for the "editme" links.
     *
     * @param sortedRulesets all the rulesets and rules
     */
    private void determineRuleClassSourceFiles(Map<Language, List<RuleSet>> sortedRulesets) {
        // first collect all the classes, we need to resolve and the rulesets
        // this also provides a default fallback path, which is used in unit tests.
        // if the actual file is found during walkFileTree, then the default fallback path
        // is replaced by a correct path.
        for (List<RuleSet> rulesets : sortedRulesets.values()) {
            for (RuleSet ruleset : rulesets) {
                String rulesetFilename = RuleSetUtils.normalizeForwardSlashes(StringUtils.chomp(ruleset.getFileName()));
                allRulesets.put(ruleset.getFileName(), rulesetFilename);
                for (Rule rule : ruleset.getRules()) {
                    String ruleClass = rule.getRuleClass();
                    String relativeSourceFilename = ruleClass.replaceAll("\\.", Matcher.quoteReplacement(File.separator))
                            + ".java";
                    allRules.put(ruleClass, RuleSetUtils.normalizeForwardSlashes(relativeSourceFilename));
                }
            }
        }

        // then go and search the actual files
        try {
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    String path = file.toString();

                    if (path.contains("src")) {
                        String foundRuleClass = null;
                        for (Map.Entry<String, String> entry : allRules.entrySet()) {
                            if (path.endsWith(entry.getValue())) {
                                foundRuleClass = entry.getKey();
                                break;
                            }
                        }
                        if (foundRuleClass != null) {
                            Path foundPath = root.relativize(file);
                            allRules.put(foundRuleClass, RuleSetUtils.normalizeForwardSlashes(foundPath.toString()));
                        }

                        String foundRuleset = null;
                        for (Map.Entry<String, String> entry : allRulesets.entrySet()) {
                            if (path.endsWith(entry.getValue())) {
                                foundRuleset = entry.getKey();
                                break;
                            }
                        }
                        if (foundRuleset != null) {
                            Path foundPath = root.relativize(file);
                            allRulesets.put(foundRuleset, RuleSetUtils.normalizeForwardSlashes(foundPath.toString()));
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
