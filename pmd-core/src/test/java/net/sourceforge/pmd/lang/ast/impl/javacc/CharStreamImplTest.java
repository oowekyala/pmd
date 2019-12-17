/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ast.impl.javacc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.EOFException;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import net.sourceforge.pmd.lang.ast.impl.javacc.io.CharSeqReader;
import net.sourceforge.pmd.lang.ast.impl.javacc.io.OffsetAwareReader;

/**
 * @author Clément Fournier
 */
public class CharStreamImplTest {

    @Rule
    public ExpectedException expect = ExpectedException.none();

    @Test
    public void testReadZeroChars() throws IOException {


        OffsetAwareReader reader = Mockito.mock(OffsetAwareReader.class);

        when(reader.read()).thenReturn(-1);
        when(reader.read(any(), anyInt(), anyInt())).thenReturn(0);

        CharStream stream = new CharStreamImpl(reader, new JavaccTokenDocument(""));

        expect.expect(EOFException.class);

        try {
            stream.readChar();
        } catch (Exception e) {
            assertEquals(stream.getStartOffset(), 0);
            assertEquals(stream.getEndOffset(), 0);
            throw e;
        }
    }

    @Test
    public void testReadEofChars() throws IOException {


        OffsetAwareReader reader = Mockito.mock(OffsetAwareReader.class);

        when(reader.read()).thenReturn(-1);
        when(reader.read(any(), anyInt(), anyInt())).thenReturn(-1);

        CharStream stream = new CharStreamImpl(reader, new JavaccTokenDocument(""));

        expect.expect(EOFException.class);

        try {
            stream.readChar();
        } catch (Exception e) {
            assertEquals(stream.getStartOffset(), 0);
            assertEquals(stream.getEndOffset(), 0);
            throw e;
        }


        verify(reader).close();
    }

    @Test
    public void testMultipleEofReads() throws IOException {


        OffsetAwareReader reader = Mockito.mock(OffsetAwareReader.class);

        when(reader.read()).thenReturn(-1);
        when(reader.read(any(), anyInt(), anyInt())).thenReturn(-1);


        CharStream stream = new CharStreamImpl(reader, new JavaccTokenDocument(""));

        for (int i = 0; i < 3; i++) {
            try {
                stream.readChar();
                fail();
            } catch (EOFException ignored) {

            }
        }

        verify(reader).close(); // close only once
        verify(reader).read(any(), anyInt(), anyInt());

    }

    @Test
    public void testExceptionInReaderClose() throws IOException {


        OffsetAwareReader reader = Mockito.mock(OffsetAwareReader.class);


        Answer<Integer> ans = AdditionalAnswers.delegatesTo(new CharSeqReader("a"));

        when(reader.read(any(), anyInt(), anyInt())).thenAnswer(ans);

        doThrow(IOException.class).when(reader).close();

        CharStream stream = new CharStreamImpl(reader, new JavaccTokenDocument(""));

        assertEquals('a', stream.readChar());

        try {
            stream.readChar();
            fail();
        } catch (EOFException e) {
            assertEquals("should have 1 suppressed exception", 1, e.getSuppressed().length);
            assertTrue(e.getSuppressed()[0] instanceof IOException);
        }

        verify(reader).close(); // close only once
    }

    @Test
    public void testReadStuff() throws IOException {


        OffsetAwareReader reader = new CharSeqReader("abcd");

        CharStream stream = new CharStreamImpl(reader, new JavaccTokenDocument("abcd"));

        assertEquals('a', stream.readChar());
        assertEquals('b', stream.readChar());
        assertEquals('c', stream.readChar());
        assertEquals('d', stream.readChar());

        expect.expect(EOFException.class);
        stream.readChar();
    }

    @Test
    public void testReadBacktrack() throws IOException {


        OffsetAwareReader reader = new CharSeqReader("abcd");

        CharStream stream = new CharStreamImpl(reader, new JavaccTokenDocument("abcd"));

        assertEquals('a', stream.readChar());
        assertEquals('b', stream.readChar());
        assertEquals('c', stream.readChar());
        assertEquals('d', stream.readChar());


        stream.backup(2);
        assertEquals('c', stream.readChar());
        assertEquals('d', stream.readChar());


        expect.expect(EOFException.class);
        stream.readChar();
    }

    @Test
    public void testBacktrackTooMuch() throws IOException {

        OffsetAwareReader reader = new CharSeqReader("abcd");

        CharStream stream = new CharStreamImpl(reader, new JavaccTokenDocument("abcd"));

        assertEquals('a', stream.readChar());
        assertEquals('b', stream.readChar());
        assertEquals('c', stream.markTokenStart());
        assertEquals('d', stream.readChar());

        expect.expect(AssertionError.class);
        expect.expectMessage("only 2 are buffered");
        stream.backup(10);
    }
    @Test
    public void testBacktrackTooMuch2() throws IOException {

        OffsetAwareReader reader = new CharSeqReader("abcd");

        CharStream stream = new CharStreamImpl(reader, new JavaccTokenDocument("abcd"));

        assertEquals('a', stream.markTokenStart());
        assertEquals('b', stream.readChar());
        assertEquals('c', stream.readChar());
        assertEquals('d', stream.readChar());

        expect.expect(AssertionError.class);
        expect.expectMessage("only 4 are buffered");
        stream.backup(10);
    }

    @Test
    public void testBufferResize() throws IOException {


        OffsetAwareReader reader = new CharSeqReader("abcd");

        CharStream stream = new CharStreamImpl(reader, 2, new JavaccTokenDocument("abcd"));

        assertEquals('a', stream.readChar());
        assertEquals('b', stream.readChar());
        assertEquals('c', stream.readChar());
        assertEquals('d', stream.readChar());


        stream.backup(2);
        assertEquals('c', stream.readChar());
        assertEquals('d', stream.readChar());

        expect.expect(EOFException.class);
        stream.readChar();
    }

    @Test
    public void testWrongBufferSize() {

        OffsetAwareReader reader = mock(OffsetAwareReader.class);

        expect.expect(IllegalArgumentException.class);
        new CharStreamImpl(reader, 0, new JavaccTokenDocument("abcd"));
    }

    @Test
    public void testNegativeBufferSize() {

        OffsetAwareReader reader = mock(OffsetAwareReader.class);

        expect.expect(IllegalArgumentException.class);
        new CharStreamImpl(reader, -2, new JavaccTokenDocument("abcd"));
    }

    @Test
    public void testNullTokenDoc() {

        OffsetAwareReader reader = mock(OffsetAwareReader.class);

        expect.expect(NullPointerException.class);
        new CharStreamImpl(reader, 10, null);
    }

}
