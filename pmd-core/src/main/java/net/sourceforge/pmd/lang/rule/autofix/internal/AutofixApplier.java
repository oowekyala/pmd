/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.rule.autofix.internal;

import java.util.List;
import java.util.Scanner;

import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.ThreadSafeReportListener;
import net.sourceforge.pmd.document.Document;
import net.sourceforge.pmd.document.MutableDocument;
import net.sourceforge.pmd.document.MutableDocument.SafeMutableDocument;
import net.sourceforge.pmd.document.ReplaceHandler;
import net.sourceforge.pmd.document.patching.TextPatch;
import net.sourceforge.pmd.lang.ast.TextAvailableNode;
import net.sourceforge.pmd.lang.rule.autofix.Autofix;
import net.sourceforge.pmd.lang.rule.autofix.TreeEditSession;

/** Simple interactive prototype, this blocks the run on every violation though. */
public class AutofixApplier implements ThreadSafeReportListener {

    private final Document doc;
    private final ReplaceHandler<?> commitToFile;
    private final String id;

    public AutofixApplier(Document doc, ReplaceHandler<?> commitToFile, String id) {
        this.doc = doc;
        this.commitToFile = commitToFile;
        this.id = id;
    }


    @Override
    public void ruleViolationAdded(RuleViolation ruleViolation) {
        // we assume that concurrent violations come from different files so cannot overlap
        List<? extends Autofix<?>> autofixes = ruleViolation.getAutofixes();
        if (autofixes.isEmpty()) {
            return;
        }
        if (!ruleViolation.getFilename().equals(id)) {
            // wrong file
            return;
        }

        // TODO don't block
        try {
            for (Autofix<?> fix : autofixes) {
                applyFix(ruleViolation, fix);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private <N extends TextAvailableNode> void applyFix(RuleViolation ruleViolation, Autofix<N> fix) throws java.io.IOException {
        final SafeMutableDocument<TextPatch> mutableDoc = doc.newMutableDoc(TextPatch.patchMaker(doc.getText()));
        @SuppressWarnings("unchecked")
        final TreeEditSession<N> session = (TreeEditSession<N>) fix.getLanguageVersion().getLanguageVersionHandler().newTreeEditSession(mutableDoc);
        if (session == null) {
            // abandoned, language version doesn't support this
            return;
        }
        fix.getImpl().apply(session);
        final TextPatch patch = session.commit();
        if (patch.isNoop()) {
            return;
        }

        if (userConsents(ruleViolation, patch)) {
            // apply the patch with a real commit handler that writes to disk
            final MutableDocument<?> committing = doc.newMutableDoc(commitToFile);
            committing.replace(doc.createRegion(0, doc.getText().length()), patch.apply(doc.getText()));
            committing.commit();
        }
    }

    private boolean userConsents(RuleViolation violation, TextPatch patches) {
        Scanner scanner = new Scanner(System.in);

        final List<String> gnus = patches.toGnuFormat();

        System.out.println(violation.getFilename());
        System.out.println("* " + violation.getDescription());
        System.out.println("The fix consists of the following patches.");
        System.out.println("Review them (f,b) and discard (d) or accept them (y)");
        System.out.println(gnus.get(0));
        System.out.println("Available: [f,b,y,d]?");
        boolean doStage;
        int idx = 0;
        out:
        while (true) {
            final String next = scanner.next();
            switch (next) {
            case "y":
                doStage = true;
                break out;
            case "f":
                if (idx < gnus.size()) {
                    idx++;
                }
                // fallthrough
            case "b":
                if (idx > 0) {
                    idx--;
                }
                System.out.println(gnus.get(idx));
                System.out.println("Available: [f,b,y,d]?");
                break;
            case "d":
                doStage = false;
                break out;
            default:
                System.out.println("Available: [f,b,y,d]?");
            }
        }

        return doStage;
    }
}
