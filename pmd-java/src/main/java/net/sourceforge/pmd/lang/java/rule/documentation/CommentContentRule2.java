/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.documentation;

import static net.sourceforge.pmd.properties.PropertyFactory.booleanProperty;
import static net.sourceforge.pmd.properties.PropertyFactory.stringListProperty;
import static net.sourceforge.pmd.util.CollectionUtil.listOf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.Comment;
import net.sourceforge.pmd.lang.java.ast.JavaVisitorBase;
import net.sourceforge.pmd.lang.rule.RuleBehavior;
import net.sourceforge.pmd.lang.rule.RuleDescriptor;
import net.sourceforge.pmd.properties.PropertyDescriptor;

/**
 * A rule that checks for illegal words in the comment text.
 *
 * TODO implement regex option
 *
 * @author Brian Remedios
 */
public class CommentContentRule2 implements RuleBehavior {

    public static final PropertyDescriptor<Boolean> CASE_SENSITIVE_DESCRIPTOR =
        booleanProperty("caseSensitive").defaultValue(false).desc("Case sensitive").build();

    public static final PropertyDescriptor<List<String>> DISSALLOWED_TERMS_DESCRIPTOR =
        stringListProperty("disallowedTerms")
            .desc("Illegal terms or phrases")
            .defaultValues("idiot", "jerk").build(); // TODO make blank property? or add more defaults?


    @Override
    public List<? extends PropertyDescriptor<?>> declaredProperties() {
        return listOf(CASE_SENSITIVE_DESCRIPTOR, DISSALLOWED_TERMS_DESCRIPTOR);
    }

    @Override
    public RuleAnalyser initialize(RuleDescriptor descriptor, Language language, RuleInitializationWarner warner) throws DysfunctionalRuleException {

        List<String> originalBadWords = descriptor.getProperty(DISSALLOWED_TERMS_DESCRIPTOR);

        if (originalBadWords.isEmpty()) {
            throw warner.fatalConfigError("No disallowed terms specified");
        }

        boolean caseSensitive = descriptor.getProperty(CASE_SENSITIVE_DESCRIPTOR);
        List<String> currentBadWords;
        if (caseSensitive) {
            currentBadWords = originalBadWords;
        } else {
            currentBadWords = new ArrayList<>();
            for (String badWord : originalBadWords) {
                currentBadWords.add(badWord.toUpperCase(Locale.ROOT));
            }
        }


        return new FullTreeVisitorAnalyser(new MyVisitor(currentBadWords, caseSensitive, originalBadWords));
    }

    private static class MyVisitor extends JavaVisitorBase<RuleContext, Void> {

        private final List<String> currentBadWords;
        private final boolean caseSensitive;
        private final List<String> originalBadWords;

        public MyVisitor(List<String> currentBadWords, boolean caseSensitive, List<String> originalBadWords) {
            this.currentBadWords = currentBadWords;
            this.caseSensitive = caseSensitive;
            this.originalBadWords = originalBadWords;
        }

        @Override
        public Void visit(ASTCompilationUnit cUnit, RuleContext data) {

            for (Comment comment : cUnit.getComments()) {
                List<String> badWords = illegalTermsIn(comment);
                if (badWords.isEmpty()) {
                    continue;
                }

                data.addViolationWithPosition(cUnit, comment.getBeginLine(), comment.getEndLine(), errorMsgFor(badWords, data));
            }

            return null;
        }


        private List<String> illegalTermsIn(Comment comment) {

            if (currentBadWords.isEmpty()) {
                return Collections.emptyList();
            }

            String commentText = comment.getFilteredComment();
            if (StringUtils.isBlank(commentText)) {
                return Collections.emptyList();
            }

            if (!caseSensitive) {
                commentText = commentText.toUpperCase(Locale.ROOT);
            }

            List<String> foundWords = new ArrayList<>();

            for (int i = 0; i < currentBadWords.size(); i++) {
                if (commentText.contains(currentBadWords.get(i))) {
                    foundWords.add(originalBadWords.get(i));
                }
            }

            return foundWords;
        }

        private String errorMsgFor(List<String> badWords, RuleContext data) {
            if (badWords.size() == 1) {
                return data.getDefaultMessage() + ": Invalid term: '" + badWords.get(0) + "'";
            } else {
                String prefix = data.getDefaultMessage() + ": Invalid terms: '";
                return badWords.stream().collect(Collectors.joining("', '", prefix, "'"));
            }
        }
    }
}
