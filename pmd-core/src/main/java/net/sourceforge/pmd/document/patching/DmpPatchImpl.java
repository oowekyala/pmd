/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.document.patching;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch.Patch;

/** Patch implementation using google's diff-match-patch. */
class DmpPatchImpl implements TextPatch {

    private final LinkedList<Patch> patch;

    DmpPatchImpl(LinkedList<Patch> patch) {
        this.patch = patch;
    }

    @Override
    public List<String> toGnuFormat() {
        return patch.stream().map(Patch::toString).collect(Collectors.toList());
    }

    @Override
    public boolean isNoop() {
        return patch.isEmpty();
    }

    @Override
    public CharSequence apply(CharSequence base) {
        final Object[] res = new DiffMatchPatch().patchApply(patch, base.toString());
        return (CharSequence) res[0];
    }
}
