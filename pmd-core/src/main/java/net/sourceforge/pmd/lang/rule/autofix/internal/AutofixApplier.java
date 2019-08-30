/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.rule.autofix.internal;

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch.Diff;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch.Operation;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch.Patch;

import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.ThreadSafeReportListener;
import net.sourceforge.pmd.document.Document;
import net.sourceforge.pmd.document.MutableDocument;
import net.sourceforge.pmd.document.ReplaceHandler;
import net.sourceforge.pmd.document.ReplaceHandler.SafeReplaceHandler;
import net.sourceforge.pmd.document.TextRegion;
import net.sourceforge.pmd.lang.rule.autofix.Autofix;

/**
 * @author Clément Fournier
 */
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
        List<Autofix> autofixes = ruleViolation.getAutofixes();
        if (autofixes.isEmpty()) {
            return;
        }
        if (!ruleViolation.getFilename().equals(id)) {
            // wrong file
            return;
        }

        // TODO don't block
        try {
            for (Autofix fix : autofixes) {
                final LinkedList<Patch> patches = fix.apply(patchMaker(doc.getText()));
                if (patches.isEmpty()) {
                    continue;
                }

                if (userConsents(patches)) {
                    final Object[] objects = new DiffMatchPatch().patchApply(patches, doc.getText().toString());
                    final MutableDocument<?> mutableDoc = doc.newMutableDoc(commitToFile);
                    mutableDoc.replace(doc.createRegion(0, doc.getText().length()), (String) objects[0]);
                    mutableDoc.commit();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean userConsents(LinkedList<Patch> patches) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("The fix consists of the following patches.");
        System.out.println("Review them (f,b) and discard (d) or accept them (y)");
        System.out.println(patches);
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
                if (idx < patches.size()) {
                    idx++;
                }
                // fallthrough
            case "b":
                if (idx > 0) {
                    idx--;
                }
                System.out.println(patches.get(idx));
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

    private static SafeReplaceHandler<LinkedList<Patch>> patchMaker(CharSequence originalBuffer) {
        return new SafeReplaceHandler<LinkedList<Patch>>() {

            final LinkedList<Diff> buffer = new LinkedList<>();
            String orig = originalBuffer.toString();

            @Override
            public void replace(TextRegion original, TextRegion mapped, String text) {
                buffer.add(new Diff(Operation.EQUAL, orig.substring(0, original.getStartOffset())));
                buffer.add(new Diff(Operation.DELETE, orig.substring(original.getStartOffset(), original.getEndOffset())));
                buffer.add(new Diff(Operation.INSERT, text));
                buffer.add(new Diff(Operation.EQUAL, orig.substring(original.getEndOffset())));
            }

            @Override
            public LinkedList<Patch> commit() {
                return new DiffMatchPatch().patchMake(orig, buffer);
            }
        };
    }
}
