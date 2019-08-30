/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.rule.autofix;


import java.io.IOException;

import net.sourceforge.pmd.document.ReplaceHandler;

public interface Autofix {

    <T> T apply(ReplaceHandler<T> handler) throws IOException;

}
