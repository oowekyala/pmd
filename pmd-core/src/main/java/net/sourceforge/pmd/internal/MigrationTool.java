/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.internal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;

public class MigrationTool {

    private static final String STYLESHEET_PATH = "/ruleset_200_to_700_transform.xslt";

    private static final XsltExecutable XSLT;


    static {

        Processor saxon = new Processor(false);
        XsltCompiler compiler = saxon.newXsltCompiler();
        XsltExecutable compiled;
        try (InputStream in = MigrationTool.class.getResourceAsStream(STYLESHEET_PATH)) {
            Source xsltSource = new SAXSource(new InputSource(in));
            compiled = compiler.compile(xsltSource);
        } catch (IOException | SaxonApiException e) {
            e.printStackTrace();
            compiled = null;
        }
        XSLT = compiled;
    }


    public static void main(String[] args) {
        Path input;
        if (args.length == 0) {
            bail("I need a filename");
        }
        input = Paths.get(args[0]);

        if (!Files.exists(input) || Files.isDirectory(input)) {
            bail("Not a regular file:" + input);
        }


        Processor saxon = new Processor(false);
        XsltCompiler compiler = saxon.newXsltCompiler();
        XsltExecutable compiled;
        try (InputStream in = MigrationTool.class.getResourceAsStream(STYLESHEET_PATH)) {
            compiled = compiler.compile(new StreamSource(in));
        } catch (IOException | SaxonApiException e) {
            throw new RuntimeException("Error while loading the stylesheet", e);
        }

        Serializer out = saxon.newSerializer(System.out);
        Xslt30Transformer transformer = compiled.load30();

        try (InputStream stream = Files.newInputStream(input)) {
            Source in = new StreamSource(stream);
            transformer.applyTemplates(in, out);
        } catch (IOException | SaxonApiException e) {
            throw new RuntimeException("Cannot open stream to output file");
        }

    }

    public static void bail(String s) {
        System.err.println(s);
        System.exit(1);
    }
}
