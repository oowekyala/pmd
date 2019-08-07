/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.lang.java;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.input.ReaderInputStream;

import net.sourceforge.pmd.lang.Parser;
import net.sourceforge.pmd.lang.ParserOptions;
import net.sourceforge.pmd.lang.TokenManager;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.ast.ParseException;
import net.sourceforge.pmd.lang.ast.RootNode;
import net.sourceforge.pmd.lang.java.ast.ASTPackage;
import net.sourceforge.pmd.lang.java.rule.TreeDumperRule;


public class JavaBinaryParser implements Parser {

    private final int jdkVersion;
    private final ParserOptions parserOptions;

    public JavaBinaryParser(int jdkVersion, ParserOptions parserOptions) {
        this.jdkVersion = jdkVersion;
        this.parserOptions = parserOptions;
    }


    @Override
    public ParserOptions getParserOptions() {
        return parserOptions;
    }

    @Override
    public TokenManager getTokenManager(String fileName, Reader source) {
        return null;
    }

    @Override
    public boolean canParse() {
        return true;
    }

    @Override
    public Node parse(String fileName, Reader source) throws ParseException {
        return new JavaLanguageParser(jdkVersion, parserOptions).parse(fileName, source);
    }

    @Override
    public Node parse(String fileName, InputStream source, Charset encoding) throws ParseException {
        if (fileName.endsWith(".javast")) {
            String packName = fileName.substring(0, fileName.length() - ".javast".length());


            try (DataInputStream stream = new DataInputStream(source)) {
                final List<RootNode> acus = TreeDumperRule.readPackageFile(stream);
                ASTPackage astPackage = new ASTPackage(packName);

                for (int i = acus.size() - 1; i >= 0; i--) {
                    astPackage.jjtAddChild(acus.get(i), i);
                }

                return astPackage;

            } catch (Exception e) {
                throw new ParseException(e);
            }
        }
        return Parser.super.parse(fileName, source, encoding);
    }

    @Override
    public Map<Integer, String> getSuppressMap() {
        return Collections.emptyMap();
    }
}
