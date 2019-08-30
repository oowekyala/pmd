/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.document.patching;


import java.util.LinkedList;
import java.util.List;

import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch.Diff;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch.Operation;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch.Patch;

import net.sourceforge.pmd.document.ReplaceHandler.SafeReplaceHandler;
import net.sourceforge.pmd.document.TextRegion;

public interface TextPatch {


    List<String> toGnuFormat();

    boolean isNull();


    CharSequence apply(CharSequence base);


    static SafeReplaceHandler<TextPatch> patchMaker(CharSequence originalBuffer) {
        return new SafeReplaceHandler<TextPatch>() {

            final LinkedList<Patch> result = new LinkedList<>();
            String orig = originalBuffer.toString();

            @Override
            public void replace(TextRegion original, TextRegion mapped, CharSequence text) {
                final LinkedList<Diff> buffer = new LinkedList<>();
                buffer.add(new Diff(Operation.EQUAL, orig.substring(0, original.getStartOffset())));
                buffer.add(new Diff(Operation.DELETE, orig.substring(original.getStartOffset(), original.getEndOffset())));
                buffer.add(new Diff(Operation.INSERT, text.toString()));
                buffer.add(new Diff(Operation.EQUAL, orig.substring(original.getEndOffset())));
                result.addAll(new DiffMatchPatch().patchMake(orig, buffer));
            }

            @Override
            public TextPatch commit() {
                return new DmpPatchImpl(result);
            }

        };
    }


}
