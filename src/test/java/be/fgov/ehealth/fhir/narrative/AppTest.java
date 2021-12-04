package be.fgov.ehealth.fhir.narrative;

import be.fgov.ehealth.fhir.narrative.option.FhirValidator;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
     * Rigorous Test :-)
     */
    public void testValidation() throws Exception {
        PrintStream output = new PrintStream(new ByteArrayOutputStream());
        FhirValidator fhirValidator = new FhirValidator(output, Collections.singletonList("https://build.fhir.org/ig/hl7-be/hl7-be-fhir-laboratory-report"));
        Pair<Integer, String> validated = fhirValidator.validate(IOUtils.toByteArray(new FileInputStream("src/test/resources/example.json")));
        assertTrue( validated.getLeft() == 2 );

        Long now = System.currentTimeMillis();
        fhirValidator.validate(IOUtils.toByteArray(new FileInputStream("src/test/resources/example.json")));
        fhirValidator.validate(IOUtils.toByteArray(new FileInputStream("src/test/resources/example.json")));
        fhirValidator.validate(IOUtils.toByteArray(new FileInputStream("src/test/resources/example.json")));
        validated = fhirValidator.validate(IOUtils.toByteArray(new FileInputStream("src/test/resources/example.json")));
        Long then = System.currentTimeMillis();

        assertTrue( validated.getLeft() == 2 );
        assertTrue( then - now < 5000 );

    }

    /**
     * Rigorous Test :-)
     */
    public void testParallelValidation() throws Exception {
        PrintStream output = new PrintStream(new ByteArrayOutputStream());
        FhirValidator fhirValidator = new FhirValidator(output, Collections.singletonList("https://build.fhir.org/ig/hl7-be/hl7-be-fhir-laboratory-report"));

        List<Thread> threads = new ArrayList<>();
        for (int i=0;i<10;i++) {
            Thread thread = new Thread(() -> {
                Pair<Integer, String> validated = null;
                try {
                    validated = fhirValidator.validate(IOUtils.toByteArray(new FileInputStream("src/test/resources/example.json")));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                assertTrue(validated.getLeft() == 2);
            });
            threads.add(thread);
            thread.start();
        }
        for (Thread t : threads) {
            t.join();
        }
    }

    /**
     * Rigorous Test :-)
     */
    public void testAppWithHtmlGenerationAndValidation() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        App.commandLine(new String[] {"--validate=https://build.fhir.org/ig/hl7-be/hl7-be-fhir-laboratory-report", "html", "src/test/resources/example.json"}, new PrintStream(bos));

        String result = new String(bos.toByteArray(), StandardCharsets.UTF_8);

        assertTrue( result.contains("  Load FHIR v4.0 from hl7.fhir.r4.core#4.0.1") );
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

                    System.out.println("Converting "+absolutePath);

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
     * Rigorous Test :-)
     */
    public void testAppWithFhirGeneration() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        App.commandLine(new String[] {"embed", "src/test/resources/example.json"}, new PrintStream(bos));

        String result = new String(bos.toByteArray(), StandardCharsets.UTF_8);

        assertTrue( result.startsWith("{") );
    }
}
