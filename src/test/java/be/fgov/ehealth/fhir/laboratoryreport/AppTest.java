package be.fgov.ehealth.fhir.laboratoryreport;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

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
    public void testAppWithFhirGeneration() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        App.commandLine(new String[] {"embed", "src/test/resources/example.json"}, new PrintStream(bos));

        String result = new String(bos.toByteArray(), StandardCharsets.UTF_8);

        assertTrue( result.startsWith("{") );
    }
}
