/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import net.sourceforge.pmd.util.AssertionUtil;

/**
 * @author Clément Fournier
 */
class FileIdTest {
    // note we can't hardcode the expected paths because they look different on win and nix

    @Test
    void testFromPath() {
        Path path = Paths.get("/a");
        Path absPath = path.toAbsolutePath();
        FileId fileId = FileId.fromPath(path);
        checkId(fileId, absPath.toString(), "a", path.toUri().toString(), path.toString());

        fileId = doSerializationRoundTrip(fileId);
        checkId(fileId, absPath.toString(), "a", path.toUri().toString(), path.toString());
    }


    @Test
    void testFromPathLikeString() {
        String str = "/a/b/c/x";
        FileId fileId = FileId.fromPathLikeString(str);
        Path absPath = Paths.get(str).toAbsolutePath();
        checkId(fileId, absPath.toString(), "x", "file://" + str, str);

        fileId = doSerializationRoundTrip(fileId);
        checkId(fileId, absPath.toString(), "x", "file://" + str, str);
    }


    @Test
    void testFromUri() {
        Path absPath = Paths.get("/a/b.c");
        String uriStr = absPath.toUri().toString();
        FileId fileId = FileId.fromURI(uriStr);
        checkId(fileId, absPath.toAbsolutePath().toString(), "b.c", uriStr, absPath.toAbsolutePath().toString());

        fileId = doSerializationRoundTrip(fileId);
        checkId(fileId, absPath.toAbsolutePath().toString(), "b.c", uriStr, absPath.toAbsolutePath().toString());
    }

    @Test
    void testFromUriForJar() {
        Path zipPath = Paths.get("/a/b.zip");
        String uriStr = "jar:" + zipPath.toUri() + "!/x/c.d";
        FileId fileId = FileId.fromURI(uriStr);
        String absLocalPath = "/x/c.d".replace('/', File.separatorChar);
        checkId(fileId, absLocalPath, "c.d", uriStr, "/x/c.d");
        checkId(fileId.getParentFsPath(), zipPath.toAbsolutePath().toString(), "b.zip", zipPath.toUri().toString(), zipPath.toAbsolutePath().toString());

        fileId = doSerializationRoundTrip(fileId);
        checkId(fileId, absLocalPath, "c.d", uriStr, "/x/c.d");
        checkId(fileId.getParentFsPath(), zipPath.toAbsolutePath().toString(), "b.zip", zipPath.toUri().toString(), zipPath.toAbsolutePath().toString());
    }

    @Test
    void testSerializationOfConstants() {
        assertSame(FileId.UNKNOWN, doSerializationRoundTrip(FileId.UNKNOWN));
        assertSame(FileId.STDIN, doSerializationRoundTrip(FileId.STDIN));
    }

    private FileId doSerializationRoundTrip(FileId fileId) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(fileId);
        } catch (IOException e) {
            throw AssertionUtil.shouldNotReachHere("Unexpected exception", e);
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (FileId) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw AssertionUtil.shouldNotReachHere("Unexpected exception", e);
        }
    }


    private static void checkId(FileId fileId, String absPath, String fileName, String uri, String originalPath) {
        assertNotNull(fileId);
        assertEquals(absPath, fileId.getAbsolutePath(), "absolute path");
        assertEquals(fileName, fileId.getFileName(), "file name");
        assertEquals(uri, fileId.getUriString(), "uri");
        assertEquals(originalPath, fileId.getOriginalPath(), "original path");
    }
}
