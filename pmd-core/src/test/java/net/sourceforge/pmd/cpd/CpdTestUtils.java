/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.cpd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.pmd.lang.DummyLanguageModule;
import net.sourceforge.pmd.lang.document.FileId;
import net.sourceforge.pmd.lang.document.TextFile;

final class CpdTestUtils {

    public static final FileId FOO_FILE_ID = FileId.fromPathLikeString("/var/Foo.java");
    public static final FileId BAR_FILE_ID = FileId.fromPathLikeString("/var/Bar.java");

    private CpdTestUtils() {
        // utility class
    }

    static CPDReport makeReport(List<Match> matches) {
        return makeReport(matches, Collections.emptyMap());
    }

    static CPDReport makeReport(List<Match> matches, Map<FileId, Integer> numTokensPerFile) {
        Set<TextFile> textFiles = new HashSet<>();
        for (Match match : matches) {
            match.iterator().forEachRemaining(
                mark -> textFiles.add(
                    TextFile.forCharSeq(DUMMY_FILE_CONTENT,
                                        mark.getLocation().getFileId(),
                                        DummyLanguageModule.getInstance().getDefaultVersion())));
        }
        return new CPDReport(
            new SourceManager(new ArrayList<>(textFiles)),
            matches,
            numTokensPerFile
        );
    }

    static class CpdReportBuilder {

        private final Map<FileId, String> fileContents = new HashMap<>();
        final Tokens tokens = new Tokens();
        private final List<Match> matches = new ArrayList<>();
        private Map<FileId, Integer> numTokensPerFile = new HashMap<>();

        CpdReportBuilder setFileContent(FileId fileName, String content) {
            fileContents.put(fileName, content);
            return this;
        }

        public CpdReportBuilder setNumTokensPerFile(Map<FileId, Integer> numTokensPerFile) {
            this.numTokensPerFile = numTokensPerFile;
            return this;
        }

        public CpdReportBuilder recordNumTokens(FileId fileName, int numTokens) {
            this.numTokensPerFile.put(fileName, numTokens);
            return this;
        }

        CPDReport build() {
            Set<TextFile> textFiles = new HashSet<>();
            fileContents.forEach((fname, contents) -> textFiles.add(TextFile.forCharSeq(contents, fname, DummyLanguageModule.getInstance().getDefaultVersion())));
            return new CPDReport(
                new SourceManager(new ArrayList<>(textFiles)),
                matches,
                numTokensPerFile
            );

        }

        Mark createMark(String image, FileId fileName, int beginLine, int lineCount) {
            fileContents.putIfAbsent(fileName, DUMMY_FILE_CONTENT);
            return new Mark(tokens.addToken(image, fileName, beginLine, 1, beginLine + lineCount - 1, 1));
        }

        CpdReportBuilder addMatch(Match match) {
            fileContents.putIfAbsent(match.getFirstMark().getLocation().getFileId(), DUMMY_FILE_CONTENT);
            matches.add(match);
            return this;
        }

        CpdReportBuilder addMatch(Mark mark1, Mark mark2) {
            return addMatch(new Match.MatchBuilder().addMark(mark1).addMark(mark2).build());
        }

        Mark createMark(String image, FileId fileId, int beginLine, int lineCount, int beginColumn, int endColumn) {
            fileContents.putIfAbsent(fileId, DUMMY_FILE_CONTENT);
            final TokenEntry beginToken = tokens.addToken(image, fileId, beginLine, beginColumn, beginLine,
                                                          beginColumn + image.length());
            final TokenEntry endToken = tokens.addToken(image, fileId,
                                                        beginLine + lineCount - 1, beginColumn,
                                                        beginLine + lineCount - 1, endColumn);

            return new Mark(beginToken, endToken);
        }

    }

    public static final String DUMMY_FILE_CONTENT = generateDummyContent(60);

    static String generateDummyContent(int lengthInLines) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lengthInLines; i++) {
            for (int j = 0; j < 10; j++) {
                sb.append(i).append("_");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
