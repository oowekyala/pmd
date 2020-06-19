/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 */
public class RulesetValidator {

    private static final Logger LOG = Logger.getLogger(RulesetValidator.class.getName());
    private static final String SCHEMA_ID = "https://pmd-code.org/ruleset/7.0.0";
    private static final String SCHEMA_LOCAL_PATH = "/ruleset_7_0_0.xsd";

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
        Path input = MigrationTool.getInput(args);

        try (Reader r = Files.newBufferedReader(input);
             InputStream schemaStream = RulesetValidator.class.getResourceAsStream(SCHEMA_LOCAL_PATH)) {

            Source schemaSource = new StreamSource(schemaStream);
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            LSResourceResolver resolver = new ClasspathResourceResolver(RulesetValidator.class);

            schemaFactory.setResourceResolver(resolver);
            schemaFactory.setErrorHandler(getErrorHandler());
            Schema schema = schemaFactory.newSchema(schemaSource);
            Validator validator = schema.newValidator();
            validator.setResourceResolver(resolver);

            validator.validate(new StreamSource(r));
        }
    }

    @NonNull
    public static ErrorHandler getErrorHandler() {
        return new ErrorHandler() {
            @Override
            public void warning(SAXParseException exception) throws SAXException {
                LOG.warning(exception.getMessage());
            }

            @Override
            public void error(SAXParseException exception) throws SAXException {
                LOG.severe(exception.getMessage());
            }

            @Override
            public void fatalError(SAXParseException exception) throws SAXException {
                LOG.severe(exception.getMessage());
            }
        };
    }

    private static class ClasspathResourceResolver implements LSResourceResolver {

        private final Class<?> ctx;

        private ClasspathResourceResolver(Class<?> ctx) {
            this.ctx = ctx;
        }

        @Override
        public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
            InputStream stream = ctx.getResourceAsStream("/" + systemId);
            if (stream == null) {
                return null;
            }

            LSInput domInput = new LsInputImpl(stream);
            domInput.setPublicId(publicId);
            domInput.setSystemId(systemId);
            domInput.setBaseURI(baseURI);
            return domInput;
        }


        // this is a nightmare
        private static class LsInputImpl implements LSInput {

            private InputStream inputStream;
            private Reader reader;
            private String systemId;
            private String publicId;
            private String baseURI;

            private String stringData;
            private boolean certifiedText;
            private String encoding;


            LsInputImpl(InputStream inputStream) {
                this.inputStream = inputStream;
            }

            @Override
            public Reader getCharacterStream() {
                return reader;
            }

            @Override
            public void setCharacterStream(Reader characterStream) {
                reader = characterStream;
            }

            @Override
            public InputStream getByteStream() {
                return inputStream;
            }

            @Override
            public void setByteStream(InputStream byteStream) {
                inputStream = byteStream;
            }

            @Override
            public String getStringData() {
                return stringData;
            }

            @Override
            public void setStringData(String stringData) {
                this.stringData = stringData;
            }

            @Override
            public String getSystemId() {
                return systemId;
            }

            @Override
            public void setSystemId(String systemId) {
                this.systemId = systemId;
            }

            @Override
            public String getPublicId() {
                return publicId;
            }

            @Override
            public void setPublicId(String publicId) {
                this.publicId = publicId;
            }

            @Override
            public String getBaseURI() {
                return baseURI;
            }

            @Override
            public void setBaseURI(String baseURI) {
                this.baseURI = baseURI;
            }

            @Override
            public boolean getCertifiedText() {
                return certifiedText;
            }

            @Override
            public String getEncoding() {
                return encoding;
            }

            @Override
            public void setEncoding(String encoding) {
                this.encoding = encoding;
            }

            public void setCertifiedText(boolean certifiedText) {
                this.certifiedText = certifiedText;
            }
        }
    }
}
