/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.document;

/**
 * A locator that does not store the text of the file in memory
 * but just the line offsets.
 */
final class MiniLocator implements Locator {

    private final TextFile textFile;
    private final SourceCodePositioner positioner;


    MiniLocator(TextFile tf, SourceCodePositioner positioner) {
        this.textFile = tf;
        this.positioner = positioner;
    }


    @Override
    public FileLocation toLocation(TextRegion region) {
        return positioner.toLocation(region, textFile.getDisplayName());
    }
}
