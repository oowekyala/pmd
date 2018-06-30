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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleSetNotFoundException;
import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.rule.RuleReference;
import net.sourceforge.pmd.lang.rule.XPathRule;
import net.sourceforge.pmd.properties.MultiValuePropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyDescriptor;

public class RuleDocGenerator {
    private static final Logger LOG = Logger.getLogger(RuleDocGenerator.class.getName());

    private static final String LANGUAGE_INDEX_FILENAME_PATTERN = "docs/pages/pmd/rules/${language.tersename}.md";
    private static final String LANGUAGE_INDEX_PERMALINK_PATTERN = "pmd_rules_${language.tersename}.html";
    private static final String RULESET_INDEX_FILENAME_PATTERN = "docs/pages/pmd/rules/${language.tersename}/${ruleset.name}.md";
    private static final String RULESET_INDEX_PERMALINK_PATTERN = "pmd_rules_${language.tersename}_${ruleset.name}.html";

    private static final String REQUIRED_PROPERTY_LABEL = tooltip("The rule requires this property to be set in the XML", label("danger", "None"));
    private static final String DEPRECATION_LABEL_SMALL = label("danger", "Deprecated");
    private static final String DEPRECATION_LABEL = label("danger big-label", "Deprecated");
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

    public RuleDocGenerator(FileWriter writer, Path root) {
        this.writer = Objects.requireNonNull(writer, "A file writer must be provided");
        this.root = Objects.requireNonNull(root, "Root directory must be provided");

        Path docsDir = root.resolve("docs");
        if (!Files.exists(docsDir) || !Files.isDirectory(docsDir)) {
            throw new IllegalArgumentException("Couldn't find \"docs\" subdirectory");
        }
    }

    public void generate(Iterator<RuleSet> registeredRulesets, List<String> additionalRulesets) {
        Map<Language, List<RuleSet>> sortedRulesets;
        Map<Language, List<RuleSet>> sortedAdditionalRulesets;
        try {
            sortedRulesets = sortRulesets(registeredRulesets);
            sortedAdditionalRulesets = sortRulesets(resolveAdditionalRulesets(additionalRulesets));
            generateLanguageIndex(sortedRulesets, sortedAdditionalRulesets);
            generateRuleSetIndex(sortedRulesets);

            generateSidebar(sortedRulesets);

        } catch (RuleSetNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void generateSidebar(Map<Language, List<RuleSet>> sortedRulesets) throws IOException {
        SidebarGenerator generator = new SidebarGenerator(writer, root);
        generator.generateSidebar(sortedRulesets);
    }

    private Iterator<RuleSet> resolveAdditionalRulesets(List<String> additionalRulesets) throws RuleSetNotFoundException {
        if (additionalRulesets == null) {
            return Collections.emptyIterator();
        }

        List<RuleSet> rulesets = new ArrayList<>();
        RuleSetFactory ruleSetFactory = new RuleSetFactory();
        for (String filename : additionalRulesets) {
            try {
                // do not take rulesets from pmd-test or pmd-core
                if (!filename.contains("pmd-test") && !filename.contains("pmd-core")) {
                    rulesets.add(ruleSetFactory.createRuleSet(filename));
                } else {
                    LOG.fine("Ignoring ruleset " + filename);
                }
            } catch (IllegalArgumentException e) {
                // ignore rulesets, we can't read
                LOG.log(Level.WARNING, "ruleset file " + filename + " ignored (" + e.getMessage() + ")", e);
            }
        }
        return rulesets.iterator();
    }

    private Path getAbsoluteOutputPath(String filename) {
        return root.resolve(FilenameUtils.normalize(filename));
    }

    private Map<Language, List<RuleSet>> sortRulesets(Iterator<RuleSet> rulesets) throws RuleSetNotFoundException {
        SortedMap<Language, List<RuleSet>> rulesetsByLanguage = new TreeMap<>();

        while (rulesets.hasNext()) {
            RuleSet ruleset = rulesets.next();
            Language language = getRuleSetLanguage(ruleset);

            if (!rulesetsByLanguage.containsKey(language)) {
                rulesetsByLanguage.put(language, new ArrayList<RuleSet>());
            }
            rulesetsByLanguage.get(language).add(ruleset);
        }

        for (List<RuleSet> rulesetsOfOneLanguage : rulesetsByLanguage.values()) {
            Collections.sort(rulesetsOfOneLanguage, new Comparator<RuleSet>() {
                @Override
                public int compare(RuleSet o1, RuleSet o2) {
                    return o1.getName().compareToIgnoreCase(o2.getName());
                }
            });
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
            lines.add("---");

            for (RuleSet ruleset : entry.getValue()) {
                lines.add("## " + ruleset.getName());
                lines.add("");
                lines.add("{% include callout.html content=\"" + getRuleSetDescriptionSingleLine(ruleset).replaceAll("\"", "'") + "\" %}");
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
        return StringUtils.abbreviate(
                StringUtils.stripToEmpty(rule.getDescription().replaceAll("\n|\r", "")
                        .replaceAll("\\|", "\\\\|")
                        .replaceAll("`", "'")
                        .replaceAll("\\*", "")), 100);
    }

    private static String getRuleSetDescriptionSingleLine(RuleSet ruleset) {
        String description = ruleset.getDescription();
        description = description.replaceAll("\\n|\\r", " ");
        description = StringUtils.stripToEmpty(description);
        return description;
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

                List<String> lines = new LinkedList<>();
                lines.add("---");
                lines.add("title: " + ruleset.getName());
                lines.add("summary: " + getRuleSetDescriptionSingleLine(ruleset));
                lines.add("permalink: " + permalink);
                lines.add("folder: pmd/rules/" + languageTersename);
                lines.add("sidebaractiveurl: /" + LANGUAGE_INDEX_PERMALINK_PATTERN.replace("${language.tersename}", languageTersename));
                lines.add("editmepath: ../" + getRuleSetSourceFilepath(ruleset));
                lines.add("keywords: " + getRuleSetKeywords(ruleset));
                lines.add("language: " + languageName);
                lines.add("---");

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

                    lines.addAll(toLines(stripIndentation(rule.getDescription())));
                    lines.add("");

                    if (rule instanceof XPathRule || rule instanceof RuleReference && ((RuleReference) rule).getRule() instanceof XPathRule) {
                        lines.add("**This rule is defined by the following XPath expression:**");
                        lines.add("``` xpath");
                        lines.addAll(toLines(StringUtils.stripToEmpty(rule.getProperty(XPathRule.XPATH_DESCRIPTOR))));
                        lines.add("```");
                    } else {
                        lines.add("**This rule is defined by the following Java class:** "
                                + "[" + rule.getRuleClass() + "]("
                                + GITHUB_SOURCE_LINK + getRuleClassSourceFilepath(rule.getRuleClass())
                                + ")");
                    }
                    lines.add("");

                    if (!rule.getExamples().isEmpty()) {
                        lines.add("**Example(s):**");
                        lines.add("");
                        for (String example : rule.getExamples()) {
                            lines.add("``` " + mapLanguageForHighlighting(languageTersename));
                            lines.addAll(toLines(StringUtils.stripToEmpty(example)));
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
                        lines.add("|Name|Default Value|Description|Multivalued|");
                        lines.add("|----|-------------|-----------|-----------|");
                        for (PropertyDescriptor<?> propertyDescriptor : properties) {
                            String description = propertyDescriptor.description();
                            if (description != null && description.toLowerCase(Locale.ROOT).startsWith(DEPRECATED_RULE_PROPERTY_MARKER)) {
                                description = DEPRECATION_LABEL_SMALL
                                        + description.substring(DEPRECATED_RULE_PROPERTY_MARKER.length());
                            }

                            String defaultValue = "";
                            if (propertyDescriptor.hasNoDefaultValue()) {
                                defaultValue = REQUIRED_PROPERTY_LABEL;
                            } else if (propertyDescriptor.defaultValue() != null) {
                                if (propertyDescriptor.isMultiValue()) {
                                    @SuppressWarnings("unchecked") // multi valued properties are using a List
                                    MultiValuePropertyDescriptor<List<?>> multiPropertyDescriptor = (MultiValuePropertyDescriptor<List<?>>) propertyDescriptor;
                                    defaultValue = multiPropertyDescriptor.asDelimitedString(multiPropertyDescriptor.defaultValue());

                                    // surround the delimiter with spaces, so that the browser can wrap
                                    // the value nicely
                                    defaultValue = defaultValue.replaceAll(Pattern.quote(
                                            String.valueOf(multiPropertyDescriptor.multiValueDelimiter())),
                                            " " + multiPropertyDescriptor.multiValueDelimiter() + " ");
                                } else {
                                    defaultValue = String.valueOf(propertyDescriptor.defaultValue());
                                }
                            }

                            String multiValued = "no";
                            if (propertyDescriptor.isMultiValue()) {
                                MultiValuePropertyDescriptor<?> multiValuePropertyDescriptor =
                                        (MultiValuePropertyDescriptor<?>) propertyDescriptor;
                                multiValued = "yes. Delimiter is '"
                                        + multiValuePropertyDescriptor.multiValueDelimiter() + "'.";
                            }

                            lines.add("|" + propertyDescriptor.name()
                                + "|" + defaultValue.replace("|", "\\|")
                                + "|" + description
                                + "|" + multiValued.replace("|", "\\|")
                                + "|");
                        }
                        lines.add("");
                    }

                    lines.add("**Use this rule by referencing it:**");
                    lines.add("``` xml");
                    lines.add("<rule ref=\"category/" + languageTersename + "/" + rulesetFilename + ".xml/" + rule.getName() + "\" />");
                    lines.add("```");
                    lines.add("");
                }

                writer.write(path, lines);
                System.out.println("Generated " + path);
            }
        }
    }


    private static String tooltip(String tooltipText, String wrappedElement) {
        return "<a href='#' data-toggle='tooltip' data-original-title='" + tooltipText + "'>" + wrappedElement + "</a>";
    }


    private static String label(String labelClass, String labelTitle) {
        return "<span class='label label-" + labelClass + "'>" + labelTitle + "</span>";
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
     * Searches for the source file of the given ruleset. This provides the information
     * for the "editme" link.
     *
     * @param ruleset the ruleset to search for.
     * @return
     * @throws IOException
     */
    private String getRuleSetSourceFilepath(RuleSet ruleset) throws IOException {
        final String rulesetFilename = FilenameUtils.normalize(StringUtils.chomp(ruleset.getFileName()));
        final List<Path> foundPathResult = new LinkedList<>();

        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String path = file.toString();
                if (path.contains("src") && path.endsWith(rulesetFilename)) {
                    foundPathResult.add(file);
                    return FileVisitResult.TERMINATE;
                }
                return super.visitFile(file, attrs);
            }
        });

        if (!foundPathResult.isEmpty()) {
            Path foundPath = foundPathResult.get(0);
            foundPath = root.relativize(foundPath);
            // Note: the path is normalized to unix path separators, so that the editme link
            // uses forward slashes
            return FilenameUtils.normalize(foundPath.toString(), true);
        }

        return StringUtils.chomp(ruleset.getFileName());
    }

    private String getRuleClassSourceFilepath(String ruleClass) throws IOException {
        final String relativeSourceFilename = ruleClass.replaceAll("\\.", Matcher.quoteReplacement(File.separator))
                + ".java";
        final List<Path> foundPathResult = new LinkedList<>();

        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String path = file.toString();
                if (path.contains("src") && path.endsWith(relativeSourceFilename)) {
                    foundPathResult.add(file);
                    return FileVisitResult.TERMINATE;
                }
                return super.visitFile(file, attrs);
            }
        });

        if (!foundPathResult.isEmpty()) {
            Path foundPath = foundPathResult.get(0);
            foundPath = root.relativize(foundPath);
            return FilenameUtils.normalize(foundPath.toString(), true);
        }

        return FilenameUtils.normalize(relativeSourceFilename, true);
    }
}
