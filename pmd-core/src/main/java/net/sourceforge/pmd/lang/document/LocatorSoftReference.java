/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.document;

import java.io.IOException;
import java.lang.ref.SoftReference;

/**
 *
 */
final class LocatorSoftReference implements Locator {

    private final TextFile textFile;
    private SoftReference<TextDocument> docRef;
    private final long checksum;

    LocatorSoftReference(TextFile tf, TextDocument doc) {
        this.textFile = tf;
        this.checksum = doc.getCheckSum();
        this.docRef = new SoftReference<>(doc);
    }

    Locator refill() {
        TextDocument doc = docRef.get();
        if (doc != null) {
            return doc;
        }
        try {
            doc = TextDocument.create(textFile);
            docRef = new SoftReference<>(doc);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return doc;
    }

    @Override
    public FileLocation toLocation(TextRegion region) {
        return refill().toLocation(region);
    }
}
