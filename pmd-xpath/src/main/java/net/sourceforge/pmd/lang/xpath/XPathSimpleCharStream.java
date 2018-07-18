/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath;

import java.io.Reader;

import net.sourceforge.pmd.lang.ast.SimpleCharStream;


/**
 * @author sergey.gorbaty
 *
 */
public class XPathSimpleCharStream extends SimpleCharStream {

    public XPathSimpleCharStream(Reader dstream) {
        super(dstream);
        tabSize = 4;
    }

}
