/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ast;

import net.sourceforge.pmd.lang.document.TextDocument;
import net.sourceforge.pmd.lang.document.TextRegion;
import net.sourceforge.pmd.lang.rule.xpath.NoAttribute;

/**
 * Refinement of {@link Node} for nodes that can provide the underlying
 * source text.
 *
 * @since 7.0.0
 */
public interface TextAvailableNode extends Node {


    /**
     * Returns the exact region of text delimiting the node in the
     * underlying text document. Note that {@link #getReportLocation()}
     * does not need to match this region. This region uses the translated
     * coordinate system, ie the coordinate system of {@link #getTextDocument()}.
     */
    TextRegion getTextRegion();

    /**
     * Returns the original source code underlying this node. In particular,
     * for a {@link RootNode}, returns the whole text of the file. Note the
     * difference between this method and {@code getTextDocument().getText().slice(getTextRegion())}.
     * The latter is {@link TextDocument#sliceTranslatedText(TextRegion)},
     * the former (this method) is {@link TextDocument#sliceOriginalText(TextRegion)}.
     */
    @NoAttribute
    CharSequence getText();


}
