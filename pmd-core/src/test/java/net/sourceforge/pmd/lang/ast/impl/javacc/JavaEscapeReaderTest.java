/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ast.impl.javacc;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;

import java.io.EOFException;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import net.sourceforge.pmd.lang.ast.impl.javacc.io.CharSeqReader;
import net.sourceforge.pmd.lang.ast.impl.javacc.io.JavaEscapeReader;

/**
 * @author Clément Fournier
 */
public class JavaEscapeReaderTest {

    @Rule
    public ExpectedException expect = ExpectedException.none();


    @Test
    public void testNoEscapes() throws IOException {
        String foo = IOUtils.toString(fromCharSeq("foo"));

        assertEquals("foo", foo);
    }

    @Test
    public void testSimpleEscape() throws IOException {
        JavaEscapeReader reader = fromCharSeq("foo\\u0061a");
        String foo = IOUtils.toString(reader);

        assertEquals("fooaa", foo);
        assertEquals(0, reader.getInputOffset(0));
        assertEquals(1, reader.getInputOffset(1));
        assertEquals(2, reader.getInputOffset(2));
        assertEquals(3, reader.getInputOffset(3));
        // behold
        assertEquals(9, reader.getInputOffset(4));
    }

    @Test
    public void testMultipleUs() throws IOException {
        JavaEscapeReader reader = fromCharSeq("foo\\uuu0061g");
        String foo = IOUtils.toString(reader);

        assertEquals("fooag", foo);
        assertEquals(0, reader.getInputOffset(0));
        assertEquals(1, reader.getInputOffset(1));
        assertEquals(2, reader.getInputOffset(2));
        assertEquals(3, reader.getInputOffset(3));
        // behold
        assertEquals(11, reader.getInputOffset(4));
    }

    @Test
    public void testEvenBackslashes() throws IOException {
        JavaEscapeReader reader = fromCharSeq("foo\\\\u0061");
        String foo = IOUtils.toString(reader);

        assertEquals("foo\\\\u0061", foo);
        assertEquals(0, reader.getInputOffset(0));
        assertEquals(1, reader.getInputOffset(1));
        assertEquals(2, reader.getInputOffset(2));
        assertEquals(3, reader.getInputOffset(3));
        assertEquals(4, reader.getInputOffset(4));
    }


    @Test
    public void testMissingU() throws IOException {
        JavaEscapeReader reader = fromCharSeq("foo\\\\0061");
        String foo = IOUtils.toString(reader);

        assertEquals("foo\\\\0061", foo);
        assertEquals(0, reader.getInputOffset(0));
        assertEquals(1, reader.getInputOffset(1));
        assertEquals(2, reader.getInputOffset(2));
        assertEquals(3, reader.getInputOffset(3));
        assertEquals(4, reader.getInputOffset(4));
    }

    @Test
    public void testBufferOverflow() throws IOException {
        // 6 backslashes
        String input = "foo\\\\\\\\\\\\0061";
        JavaEscapeReader reader = new JavaEscapeReader(new CharSeqReader(input), 3);
        String foo = IOUtils.toString(reader);

        assertEquals(input, foo);
        assertEquals(0, reader.getInputOffset(0));
        assertEquals(1, reader.getInputOffset(1));
        assertEquals(2, reader.getInputOffset(2));
        assertEquals(3, reader.getInputOffset(3));
        assertEquals(4, reader.getInputOffset(4));
    }


    @Test
    public void testForgetBehaviour() throws IOException {

        final String str = StringUtils.repeat("\\u0061", 100);

        JavaEscapeReader reader = fromCharSeq(str);

        // read 'toRead' escapes, ie 'toRead * 6' input chars at least
        int toRead = 70;
        int read = reader.read(new char[toRead], 0, toRead);
        assertEquals(toRead, read);

        assertEquals(0, reader.getInputOffset(0));
        assertEquals(6, reader.getInputOffset(1));
        assertEquals(12, reader.getInputOffset(2));
        assertEquals(18, reader.getInputOffset(3));

        reader.releaseBefore(2);

        assertEquals(12, reader.getInputOffset(2));
        assertEquals(18, reader.getInputOffset(3));
        assertEquals(24, reader.getInputOffset(4));


    }


    @Test
    public void testInvalidUnicode() throws IOException {

        expect.expectMessage("Invalid escape sequence at line 1, column 4: \\u00k2");
        expect.expectCause(instanceOf(NumberFormatException.class));

        JavaEscapeReader reader = fromCharSeq("foo\\u00k2");
        IOUtils.toString(reader);
    }


    @Test
    public void testInvalidUnicodeEof() throws IOException {

        expect.expectMessage("Invalid escape sequence at line 1, column 4: \\u00");
        expect.expectCause(instanceOf(EOFException.class));

        JavaEscapeReader reader = fromCharSeq("foo\\u00");
        IOUtils.toString(reader);
    }

    @Test
    public void testInvalidUnicodeOkBackSlashPair() throws IOException {

        JavaEscapeReader reader = fromCharSeq("foo\\\\u00k2");
        String foo = IOUtils.toString(reader);

        assertEquals("foo\\\\u00k2", foo);
    }


    public static JavaEscapeReader fromCharSeq(CharSequence input) {
        return new JavaEscapeReader(new CharSeqReader(input));
    }
}
