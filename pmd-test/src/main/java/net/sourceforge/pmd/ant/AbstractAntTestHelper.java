/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.ant;

import static java.io.File.separator;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildFileRule;
import org.apache.tools.ant.Project;
import org.junit.Before;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.rules.TemporaryFolder;

import net.sourceforge.pmd.annotation.InternalApi;


/**
 * Quite an ugly classe, arguably useful for just 2 units test - nevertheless as
 * there is a workaround that must be shared by both tests (PMD and CPD's) I
 * felt compelled to move it to a single classes.
 *
 * @author Romain Pelisse &lt;belaran@gmail.com&gt;
 *
 */
public abstract class AbstractAntTestHelper {

    @Rule
    @InternalApi
    @Deprecated
    public final TemporaryFolder tempFolder = new TemporaryFolder();

    @Rule
    @InternalApi
    @Deprecated
    public final BuildFileRule buildRule = new BuildFileRule();

    @Rule
    @InternalApi
    @Deprecated
    public final SystemErrRule systemErrRule = new SystemErrRule().muteForSuccessfulTests();

    protected String pathToTestScript;
    protected String antTestScriptFilename;
    @InternalApi
    @Deprecated
    public String mvnWorkaround;

    public AbstractAntTestHelper() {
        mvnWorkaround = "pmd/ant/xml";
        if (new File("target/clover/test-classes").exists()) {
            pathToTestScript = "target/clover/test-classes/net/sourceforge/" + mvnWorkaround;
        } else {
            pathToTestScript = "target/test-classes/net/sourceforge/" + mvnWorkaround;
        }
    }

    @Before
    public void setUp() throws IOException {
        validatePostConstruct();
        // initialize Ant
        buildRule.configureProject(pathToTestScript + separator + antTestScriptFilename);

        // Each test case gets one temp file name, accessible with ${tmpfile}
        final File newFile = tempFolder.newFile();
        newFile.delete(); // It shouldn't exist yet, but we want a unique name
        buildRule.getProject().setProperty("tmpfile", newFile.getAbsolutePath());

        Project project = buildRule.getProject();
        if (!project.getBaseDir().toString().endsWith(mvnWorkaround)) {
            // when running from maven, the path needs to be adapted...
            // FIXME: this is more a workaround than a good solution...
            project.setBasedir(project.getBaseDir().toString() + separator + pathToTestScript);
        }
    }


    /**
     * Returns the current temporary file. Replaced by a fresh (inexistent)
     * file before each test.
     */
    public File currentTempFile() {
        String tmpname = buildRule.getProject().getProperty("tmpfile");
        return tmpname == null ? null : new File(tmpname);
    }


    private void validatePostConstruct() {
        if (pathToTestScript == null || "".equals(pathToTestScript) || antTestScriptFilename == null
                || "".equals(antTestScriptFilename) || mvnWorkaround == null || "".equals(mvnWorkaround)) {
            throw new IllegalStateException("Unit tests for Ant script badly initialized");
        }
    }

    public void executeTarget(String target) {
        buildRule.executeTarget(target);
        System.err.println(buildRule.getLog());
    }

    public void assertOutputContaining(String text) {
        assertThat(buildRule.getOutput(), containsString(text));
    }


    public void assertContains(String text, String toFind) {
        assertThat(text, containsString(toFind));
    }


    public void assertDoesntContain(String text, String toFind) {
        assertThat(text, not(containsString(toFind)));
    }
}
