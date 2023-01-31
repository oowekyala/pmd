/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.document;

/**
 * An object that can produce locations from text regions.
 */
public interface Locator {

    /**
     * Turn a text region into a {@link FileLocation}.
     *
     * @return A new file position
     *
     * @throws IndexOutOfBoundsException If the argument is not a valid region in the locator
     */
    FileLocation toLocation(TextRegion region);

}
