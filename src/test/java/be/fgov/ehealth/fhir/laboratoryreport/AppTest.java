package be.fgov.ehealth.fhir.laboratoryreport;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.io.FilenameUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Unit test for simple App.
 */
public class AppTest
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testAppWithHtmlGeneration() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        App.commandLine(new String[] {"html", "src/test/resources/example.json"}, new PrintStream(bos));

        String result = new String(bos.toByteArray(), StandardCharsets.UTF_8);

        assertTrue( result.contains("<html") );
    }

    /**
     * Rigourous Test :-)
     */
    public void testAppWithHtmlGenerationOnCommunityTests() throws IOException {
        Files.walk(Paths.get("hl7-be-fhir-laboratory-report/testing"))
                .filter(Files::isRegularFile)
                .filter(f -> FilenameUtils.getExtension(f.toString()).equals("json"))
                .forEach(f -> {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    Path absolutePath = f.toAbsolutePath();

                    App.commandLine(new String[] {"html", absolutePath.toString()}, new PrintStream(bos));

                    byte[] bytes = bos.toByteArray();
                    String result = new String(bytes, StandardCharsets.UTF_8);

                    Path destinationPath = absolutePath.resolveSibling(absolutePath.getFileName().toString().replaceAll("\\.json$", ".html"));
                    try {
                        Files.write(destinationPath, bytes);
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                    assertTrue( result.contains("<html") );
                });
    }

    /**
     * Rigourous Test :-)
     */
    public void testAppWithFhirGeneration() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        App.commandLine(new String[] {"embed", "src/test/resources/example.json"}, new PrintStream(bos));

        String result = new String(bos.toByteArray(), StandardCharsets.UTF_8);

        assertTrue( result.startsWith("{") );
    }
}
