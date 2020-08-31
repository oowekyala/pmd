/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.cpd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.sourceforge.pmd.annotation.Experimental;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.ast.TokenMgrError;
import net.sourceforge.pmd.util.database.DBMSMetadata;
import net.sourceforge.pmd.util.database.DBURI;
import net.sourceforge.pmd.util.database.SourceObject;
import net.sourceforge.pmd.util.document.TextDocument;
import net.sourceforge.pmd.util.document.io.PmdFiles;
import net.sourceforge.pmd.util.document.io.TextFile;

public class CPD {
    private static final Logger LOGGER = Logger.getLogger(CPD.class.getName());

    private final CPDConfiguration configuration;

    private final Map<String, TextDocument> source = new TreeMap<>();
    private CPDListener listener = new CPDNullListener();
    private final Tokens tokens = new Tokens();
    private MatchAlgorithm matchAlgorithm;
    private final Set<String> current = new HashSet<>();

    public CPD(CPDConfiguration theConfiguration) {
        configuration = theConfiguration;
        // before we start any tokenizing (add(File...)), we need to reset the
        // static TokenEntry status
        TokenEntry.clearImages();
    }

    public void setCpdListener(CPDListener cpdListener) {
        this.listener = cpdListener;
    }

    public void go() {
        matchAlgorithm = new MatchAlgorithm(source, tokens, configuration.getMinimumTileSize(), listener);
        matchAlgorithm.findMatches();
    }

    public Iterator<Match> getMatches() {
        return matchAlgorithm.matches();
    }

    public void addAllInDirectory(File dir) throws IOException {
        addDirectory(dir.toPath(), false);
    }

    public void addRecursively(File dir) throws IOException {
        addDirectory(dir.toPath(), true);
    }

    public void add(List<File> files) throws IOException {
        for (File f : files) {
            add(f);
        }
    }

    private void addDirectory(Path root, boolean recurse) throws IOException {
        if (!Files.exists(root)) {
            throw new FileNotFoundException(root.toString());
        }

        Predicate<Path> filter = configuration.filenameFilter();


        List<Path> paths;
        try (Stream<Path> walk = recurse ? Files.walk(root) : Files.list(root)) {
            paths = walk.filter(filter)
                        .sorted()
                        .collect(Collectors.toList());
        }

        for (Path path : paths) {
            add(path);
        }
    }

    public void add(File file) throws IOException {
        add(file.toPath());
    }

    public void add(Path path) throws IOException {

        if (!Files.isRegularFile(path)) {
            System.err.println("Skipping " + path + " since it appears to be a symlink or directory");
            return;
        }

        if (!Files.exists(path)) {
            System.err.println("Skipping " + path + " since it doesn't exist (broken symlink?)");
            return;
        }

        LanguageVersion version = configuration.getLanguage().getDefaultVersion();
        TextFile textFile = PmdFiles.forPath(path, Charset.forName(configuration.getEncoding()), version);
        add(textFile);
    }

    public void add(TextFile file) throws IOException {

        if (configuration.isSkipDuplicates()) {
            if (!current.add(file.getPathId())) {
                System.err.println("Skipping " + file.getPathId()
                                       + " since it appears to be a duplicate file and --skip-duplicate-files is set");
                return;
            }
        }

        add(TextDocument.create(file));
    }

    public void add(DBURI dburi) throws IOException {

        try {
            DBMSMetadata dbmsmetadata = new DBMSMetadata(dburi);

            List<SourceObject> sourceObjectList = dbmsmetadata.getSourceObjectList();
            LOGGER.log(Level.FINER, "Located {0} database source objects", sourceObjectList.size());

            for (SourceObject sourceObject : sourceObjectList) {
                // Add DBURI as a faux-file
                String falseFilePath = sourceObject.getPseudoFileName();
                LOGGER.log(Level.FINEST, "Adding database source object {0}", falseFilePath);

                SourceCode sourceCode = configuration.sourceCodeFor(dbmsmetadata.getSourceCode(sourceObject), falseFilePath);
                add(PmdFiles.cpdCompat(sourceCode));
            }
        } catch (Exception sqlException) {
            LOGGER.log(Level.SEVERE, "Problem with Input URI", sqlException);
            throw new IOException("Problem with DBURI: " + dburi, sqlException);
        }
    }

    @Experimental
    public void add(TextDocument sourceCode) throws IOException {
        if (configuration.isSkipLexicalErrors()) {
            addAndSkipLexicalErrors(sourceCode);
        } else {
            addAndThrowLexicalError(sourceCode);
        }
    }

    private void addAndThrowLexicalError(TextDocument sourceCode) throws IOException {

        configuration.tokenizer().tokenize(sourceCode, tokens);
        listener.addedFile(1, sourceCode.getPathId());
        source.put(sourceCode.getPathId(), sourceCode);
    }

    private void addAndSkipLexicalErrors(TextDocument sourceCode) throws IOException {
        TokenEntry.State savedTokenEntry = new TokenEntry.State(tokens.getTokens());
        try {
            addAndThrowLexicalError(sourceCode);
        } catch (TokenMgrError e) {
            System.err.println("Skipping " + sourceCode.getDisplayName() + ". Reason: " + e.getMessage());
            tokens.getTokens().clear();
            tokens.getTokens().addAll(savedTokenEntry.restore());
        }
    }

    /**
     * List names/paths of each source to be processed.
     *
     * @return names of sources to be processed
     */
    public List<String> getSourcePaths() {
        return new ArrayList<>(source.keySet());
    }

    public static void main(String[] args) {
        CPDCommandLineInterface.main(args);
    }
}
