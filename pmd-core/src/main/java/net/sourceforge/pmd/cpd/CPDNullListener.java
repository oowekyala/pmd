/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.cpd;

public class CPDNullListener implements CPDListener {
    @Override
    public void addedFile(int fileCount, String fileId) {
        // does nothing - override it if necessary
    }

    @Override
    public void phaseUpdate(int phase) {
        // does nothing - override it if necessary
    }
}
