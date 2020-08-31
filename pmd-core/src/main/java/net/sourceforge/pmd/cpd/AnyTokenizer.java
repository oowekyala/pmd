/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.cpd;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.pmd.util.document.Chars;
import net.sourceforge.pmd.util.document.FileLocation;
import net.sourceforge.pmd.util.document.TextDocument;
import net.sourceforge.pmd.util.document.TextRegion;

/**
 * This class does a best-guess try-anything tokenization.
 *
 * @author jheintz
 */
public class AnyTokenizer implements Tokenizer {

    public static final String TOKENS = " \t!#$%^&*(){}-=+<>/\\`~;:";
    private static final Pattern SEPARATOR_PATTERN = Pattern.compile("\\s++|[!#$%^&*(){}\\-=+<>\\\\/`~;:]");

    @Override
    public void tokenize(TextDocument sourceCode, Tokens tokenEntries) {
        Chars text = sourceCode.getText();
        Matcher matcher = SEPARATOR_PATTERN.matcher(text);
        int lastMatchEnd = 0;
        try {
            while (matcher.find()) {
                Chars lastTok = text.subSequence(lastMatchEnd, matcher.start()).trim();
                if (lastTok.length() > 0) {
                    FileLocation loc = sourceCode.toLocation(TextRegion.fromBothOffsets(lastMatchEnd, matcher.start()));
                    tokenEntries.add(new TokenEntry(lastTok.toString(), sourceCode.getPathId(), loc.getBeginLine(), loc.getBeginColumn(), loc.getEndColumn()));
                }
                lastMatchEnd = matcher.end();
            }
        } finally {
            tokenEntries.add(TokenEntry.EOF);
        }
    }
}
