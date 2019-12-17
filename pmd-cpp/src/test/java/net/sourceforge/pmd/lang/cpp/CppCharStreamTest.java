/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.cpp;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

import net.sourceforge.pmd.lang.ast.impl.javacc.CharStream;
import net.sourceforge.pmd.lang.cpp.internal.CppEscapeReader;

public class CppCharStreamTest {

    @Test
    public void testContinuationUnix() throws IOException {
        CharStream stream = CppEscapeReader.cppCharStream(new StringReader("a\\\nb"));
        assertStream(stream, "ab");
    }

    @Test
    public void testContinuationWindows() throws IOException {
        CharStream stream = CppEscapeReader.cppCharStream(new StringReader("a\\\r\nb"));
        assertStream(stream, "ab");
    }

    @Test
    public void testBackup() throws IOException {
        CharStream stream = CppEscapeReader.cppCharStream(new StringReader("a\\b\\\rc"));
        assertStream(stream, "a\\b\\\rc");
    }

    private void assertStream(CharStream stream, String token) throws IOException {
        char c = stream.markTokenStart();
        assertEquals(token.charAt(0), c);
        for (int i = 1; i < token.length(); i++) {
            c = stream.readChar();
            assertEquals(token.charAt(i), c);
        }
        assertEquals(token, stream.tokenImage());
        assertEquals(token, new String(stream.imageSuffix(token.length())));
    }
}
