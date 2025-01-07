package be.fgov.ehealth.fhir.narrative;

import be.fgov.ehealth.fhir.narrative.gen.ResourceHtmlGenerator;
import be.fgov.ehealth.fhir.narrative.option.FhirValidator;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.Bundle;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.hl7.fhir.r4.model.FhirNarrativeUtils.stripNarratives;

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
     * Rigorous Test :-)
     */
    public void testAppWithHtmlGeneration() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        //App.commandLine(new String[] {"html", "src/test/resources/Bundle-hematologyStudiesAsBundleCollection.xml", "lab", "-d"}, new PrintStream(bos));
        //App.commandLine(new String[] {"html", "src/test/resources/example5.json", "immunization", "-d"}, new PrintStream(bos));
        //App.commandLine(new String[] {"html", "src/test/resources/81365677998_815333041_13.json", "lab", "-d"}, new PrintStream(bos));
        //App.commandLine(new String[] {"html", "src/test/resources/Bundle-hematologyStudiesAsBundleCollection.xml", "lab", "-d"}, new PrintStream(bos));
        //App.commandLine(new String[] {"html", "C:\\Users\\eh068\\Documents\\Bundle-hematologyStudiesAsBundleCollection.xml", "lab", "-d"}, new PrintStream(bos));
        //App.commandLine(new String[] {"html", "C:\\Users\\eh068\\Documents\\Immunization5.json", "immunization", "-d"}, new PrintStream(bos));
        App.commandLine(new String[] {"html", "C:\\Users\\eh068\\Documents\\1. Standards\\Fhir examples\\Referral\\Task1.json", "-d"}, new PrintStream(bos));


        String result = new String(bos.toByteArray(), StandardCharsets.UTF_8);

        System.out.println("result: " + result);

        createFile("C:\\Users\\eh068\\Documents\\LaboResultVisualizer\\laboResultVisualization\\TestVisu2.html", result);

        assertTrue( result.contains("<html") );
    }

    /**
     * Rigorous Test :-)
     */
    public void testValidation() throws Exception {
        PrintStream output = new PrintStream(new ByteArrayOutputStream());
        //FhirValidator fhirValidator = new FhirValidator(output, Collections.singletonList("https://build.fhir.org/ig/hl7-be/lab"));
        //FhirValidator fhirValidator = new FhirValidator(output, Collections.singletonList("https://www.ehealth.fgov.be/standards/fhir/lab"));
        //FhirValidator fhirValidator = new FhirValidator(output, Collections.singletonList("https://www.ehealth.fgov.be/standards/fhir/lab"));
        FhirValidator fhirValidator = new FhirValidator(output, Collections.singletonList("https://www.ehealth.fgov.be/standards/fhir/core"));
        Pair<Integer, String> validated = fhirValidator.validate(IOUtils.toByteArray(new FileInputStream("src/test/resources/example.json")));

        System.out.println(validated.getRight());
        System.out.println("left value: " + validated.getLeft().intValue());

        assertEquals(1, validated.getLeft().intValue());

        /*Long now = System.currentTimeMillis();
        fhirValidator.validate(IOUtils.toByteArray(new FileInputStream("src/test/resources/example.json")));
        fhirValidator.validate(IOUtils.toByteArray(new FileInputStream("src/test/resources/example.json")));
        fhirValidator.validate(IOUtils.toByteArray(new FileInputStream("src/test/resources/example.json")));
        validated = fhirValidator.validate(IOUtils.toByteArray(new FileInputStream("src/test/resources/example.json")));
        Long then = System.currentTimeMillis();
*/
        assertEquals(1, validated.getLeft().intValue());
        //assertTrue( then - now < 50000 );

    }

    /**
     * Rigorous Test :-)
     */
    public void testParallelValidation() throws Exception {
        PrintStream output = new PrintStream(new ByteArrayOutputStream());
        FhirValidator fhirValidator = new FhirValidator(output, Collections.singletonList("https://build.fhir.org/ig/hl7-be/lab"));

        List<Thread> threads = new ArrayList<>();
        for (int i=0;i<10;i++) {
            Thread thread = new Thread(() -> {
                Pair<Integer, String> validated = null;
                try {
                    validated = fhirValidator.validate(IOUtils.toByteArray(new FileInputStream("src/test/resources/example.json")));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                assertEquals(1, (int) validated.getLeft());
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
        App.commandLine(new String[] {"--validate=https://build.fhir.org/ig/hl7-be/lab", "html", "src/test/resources/example.json"}, new PrintStream(bos));

        String result = new String(bos.toByteArray(), StandardCharsets.UTF_8);

        assertTrue( result.contains("Success: 0 errors") );
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

    public void testBinaryInputFhirGeneration() throws UnsupportedEncodingException {
        byte[] fhirData = Base64.getDecoder().decode("ewogICJyZXNvdXJjZVR5cGUiOiAiQnVuZGxlIiwKICAiaWQiOiAiYW50aWJpb2dyYW1Jc05lZ2F0aXZlQXNCdW5kbGVDb2xsZWN0aW9uIiwKICAiaWRlbnRpZmllciI6IHsKICAgICJzeXN0ZW0iOiAidXJuOmlldGY6cmZjOjM5ODYiLAogICAgInZhbHVlIjogInVybjp1dWlkOjBjMzIwMWJkLTFjYmYtNGQ2NC1iMDRkLWNkOTE4N2E0YzZlMCIKICB9LAogICJ0eXBlIjogImNvbGxlY3Rpb24iLAogICJlbnRyeSI6IFsKICAgIHsKICAgICAgImZ1bGxVcmwiOiAidXJuOnV1aWQ6N2MxNmM5YzAtYzQ3MS00MDk4LWFlMTgtNDhhYmNhZGQ4ZDkwIiwKICAgICAgInJlc291cmNlIjogewogICAgICAgICJyZXNvdXJjZVR5cGUiOiAiRGlhZ25vc3RpY1JlcG9ydCIsCiAgICAgICAgImlkIjogImRpYWdub3N0aWNyZXBvcnQ2MCIsCiAgICAgICAgIm1ldGEiOiB7CiAgICAgICAgICAidmVyc2lvbklkIjogIjEiLAogICAgICAgICAgInByb2ZpbGUiOiBbCiAgICAgICAgICAgICJodHRwczovL3d3dy5laGVhbHRoLmZnb3YuYmUvc3RhbmRhcmRzL2ZoaXIvU3RydWN0dXJlRGVmaW5pdGlvbi9iZS1sYWJvcmF0b3J5LXJlcG9ydCIKICAgICAgICAgIF0KICAgICAgICB9LAogICAgICAgICJsYW5ndWFnZSI6ICJlbiIsCiAgICAgICAgInRleHQiOiB7CiAgICAgICAgICAic3RhdHVzIjogImdlbmVyYXRlZCIsCiAgICAgICAgICAiZGl2IjogIjxkaXYgeG1sbnM9XCJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hodG1sXCIgeG1sOmxhbmc9XCJlblwiIGxhbmc9XCJlblwiPlxuXHRcdFx0XHRcdFx0PHA+VGhpcyBleGFtcGxlIGlzIHByZXNlbnRlZCBhcyBhIENvbGxlY3Rpb24gYnVuZGxlIGZvciBlYXNlIG9mIHVuZGVyc3RhbmRpbmcuIEl0IG1ha2VzIG5vIGFzc3VtcHRpb24gdG93YXJkcyBhbnkgZmluYWwgaW1wbGVtZW50YXRpb24gb2YgdXNpbmcgRkhJUiB0ZWNobmljYWxseS4gPC9wPlxuXHRcdFx0XHRcdFx0PHA+RG93bmxvYWQgYW5kIG9wZW4gdGhpcyBhcyBYTUwgaW4gYW4gZWRpdG9yIHRvIHZpZXcgWE1MIGFubm90YXRpb25zLjwvcD5cblx0XHRcdFx0XHRcdDxwPkFMTCBMT0lOQywgU05PTUVELUNUIENPREVTIEVUQy4gQVJFIFVTRUQgRk9SIElMTFVTVFJBVElWRSBQVVJQT1NFUyBBTkQgRE8gTk9UIE5FQ0VTU0FSSUxZIFBSRVNFTlQgQSBDTElOSUNBTExZIENPUlJFQ1QgUkVBTCBMSUZFIExBQk9SQVRPUlkgUkVQT1JUPC9wPlxuXHRcdFx0XHRcdFx0PGgyPkEgbmFycmF0aXZlIFNIQUxMIGJlIGluY2x1ZGVkPC9oMj5cblx0XHRcdFx0XHRcdDxwPlNwZWNpZmljYWxseSwgdGhlIHN0cm9uZyByZWNvbW1lbmRhdGlvbiBvZiBITDcgY29uY2VybmluZyB0aGUgdXNlIG9mIHRoZSBuYXJyYXRpdmUgJnF1b3Q7dG8gc3VwcG9ydCBodW1hbi1jb25zdW1wdGlvbiBhcyBhIGZhbGxiYWNrJnF1b3Q7IGlzIGltcG9ydGFudCBpbiB0aGUgY29udGV4dCBvZiB0aGUgbGFib3JhdG9yeSByZXBvcnQuIEluZGVlZCwgdGhpcyAmcXVvdDtodW1hbi1jb25zdW1wdGlvbiZxdW90OyBhc3BlY3QgYWN0dWFsbHkgY29uY2VybnMgdGhlIGluY2x1c2lvbiBvZiBsaW1pdGVkIHhodG1sIGNvbnRlbnQgYW5kIGFzIHN1Y2ggYW4gZWZmb3J0bGVzcyBpbXBsZW1lbnRhdGlvbiB0byB2aXN1YWxpemUgdGhlIGNvbnRlbnQgb2YgdGhlIHJlcG9ydCBieSBjb25zdW1pbmcgc3lzdGVtcyBpcyBndWFyYW50dWVlZC4gQWxzbywgZm9yIHN5c3RlbXMgdGhhdCBtaWdodCBub3QgaW5pdGlhbGx5IGNob29zZSB0byBjb25zdW1lIHJlcG9ydHMgaW4gYSBzdHJ1Y3R1cmVkIHdheSwgdGhpcyBndWFyYW50dWVlcyBhIHdheSB0byBhdCBsZWFzdCB2aXN1YWxpemUgYW5kIHByZXNlbnQgdGhlIGNvbnRlbnQgdG8gdGhlIHVzZXIuPC9wPlxuXHRcdFx0XHRcdFx0PHA+VGhlIGV4YWN0IHByZXNlbnRhdGlvbiBpcyBsZWZ0IGF0IHRoZSBkaXNjcmV0aW9uIG9mIHRoZSBwcm92aWRpbmcgcGFydHkuPC9wPlxuXHRcdFx0XHRcdFx0PHByZT5TcGVjaW1lblx0XHRcdEludHViYXRpb24gQXNwaXJhdGVcbk1hY3Jvc2NvcGljIG9ic2VydmF0aW9uXHRcdE1hdGlnIHB1cnVsZW50XG5DdWx0dXJlXHRcdFx0XHRDb21tZW5zYWxlbiArLTwvcHJlPlxuXHRcdFx0XHRcdDwvZGl2PiIKICAgICAgICB9LAogICAgICAgICJpZGVudGlmaWVyIjogWwogICAgICAgICAgewogICAgICAgICAgICAic3lzdGVtIjogImh0dHA6Ly9hY21lLmNvbS9sYWIvcmVwb3J0cyIsCiAgICAgICAgICAgICJ2YWx1ZSI6ICIxMTUzMDIzMTAwMy4yMDE1MTEwNDEzMTgwMDAwMDAwIgogICAgICAgICAgfQogICAgICAgIF0sCiAgICAgICAgImJhc2VkT24iOiBbCiAgICAgICAgICB7CiAgICAgICAgICAgICJyZWZlcmVuY2UiOiAiU2VydmljZVJlcXVlc3Qvc2VydmljZXJlcXVlc3Q2MCIKICAgICAgICAgIH0KICAgICAgICBdLAogICAgICAgICJzdGF0dXMiOiAiZmluYWwiLAogICAgICAgICJjYXRlZ29yeSI6IFsKICAgICAgICAgIHsKICAgICAgICAgICAgImNvZGluZyI6IFsKICAgICAgICAgICAgICB7CiAgICAgICAgICAgICAgICAic3lzdGVtIjogImh0dHA6Ly90ZXJtaW5vbG9neS5obDcub3JnL0NvZGVTeXN0ZW0vdjItMDA3NCIsCiAgICAgICAgICAgICAgICAiY29kZSI6ICJMQUIiLAogICAgICAgICAgICAgICAgImRpc3BsYXkiOiAiTGFib3JhdG9yeSIKICAgICAgICAgICAgICB9LAogICAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgICJzeXN0ZW0iOiAiaHR0cDovL3Rlcm1pbm9sb2d5LmhsNy5vcmcvQ29kZVN5c3RlbS92Mi0wMDc0IiwKICAgICAgICAgICAgICAgICJjb2RlIjogIk1CIiwKICAgICAgICAgICAgICAgICJkaXNwbGF5IjogIk1pY3JvYmlvbG9neSIKICAgICAgICAgICAgICB9CiAgICAgICAgICAgIF0KICAgICAgICAgIH0KICAgICAgICBdLAogICAgICAgICJjb2RlIjogewogICAgICAgICAgImNvZGluZyI6IFsKICAgICAgICAgICAgewogICAgICAgICAgICAgICJzeXN0ZW0iOiAiaHR0cDovL2xvaW5jLm9yZyIsCiAgICAgICAgICAgICAgImNvZGUiOiAiMTg3NjktMCIsCiAgICAgICAgICAgICAgImRpc3BsYXkiOiAiTWljcm9iaWFsIHN1c2NlcHRpYmlsaXR5IHRlc3RzIFNldCIKICAgICAgICAgICAgfQogICAgICAgICAgXSwKICAgICAgICAgICJ0ZXh0IjogIk1pY3JvYmlhbCBzdXNjZXB0aWJpbGl0eSB0ZXN0cyIKICAgICAgICB9LAogICAgICAgICJzdWJqZWN0IjogewogICAgICAgICAgInJlZmVyZW5jZSI6ICJ1cm46dXVpZDo3YzE2YzljMC1jNDcxLTQwOTgtYWUxOC00OGFiY2FkZDhkOTYiCiAgICAgICAgfSwKICAgICAgICAiZWZmZWN0aXZlRGF0ZVRpbWUiOiAiMjAxNS0xMS0wM1QxMzoxODowMCswMTowMCIsCiAgICAgICAgImlzc3VlZCI6ICIyMDE1LTExLTA0VDEzOjE4OjAwKzAxOjAwIiwKICAgICAgICAicGVyZm9ybWVyIjogWwogICAgICAgICAgewogICAgICAgICAgICAicmVmZXJlbmNlIjogIk9yZ2FuaXphdGlvbi9vcmdhbml6YXRpb24xMCIKICAgICAgICAgIH0KICAgICAgICBdLAogICAgICAgICJyZXN1bHRzSW50ZXJwcmV0ZXIiOiBbCiAgICAgICAgICB7CiAgICAgICAgICAgICJyZWZlcmVuY2UiOiAiUHJhY3RpdGlvbmVyL3ByYWN0aXRpb25lcjExIgogICAgICAgICAgfQogICAgICAgIF0sCiAgICAgICAgInNwZWNpbWVuIjogWwogICAgICAgICAgewogICAgICAgICAgICAicmVmZXJlbmNlIjogIlNwZWNpbWVuL3NwZWNpbWVuNjAiCiAgICAgICAgICB9CiAgICAgICAgXSwKICAgICAgICAicmVzdWx0IjogWwogICAgICAgICAgewogICAgICAgICAgICAicmVmZXJlbmNlIjogInVybjp1dWlkOjdjMTZjOWMwLWM0NzEtNDA5OC1hZTA5LTQ4YWJjYWRkOWQ5MiIKICAgICAgICAgIH0sCiAgICAgICAgICB7CiAgICAgICAgICAgICJyZWZlcmVuY2UiOiAidXJuOnV1aWQ6N2MxNmM5YzAtYzQ3MS00MDk4LWFlMDktNDhhYmNhZGQ5ZDkzIgogICAgICAgICAgfQogICAgICAgIF0sCiAgICAgICAgImNvbmNsdXNpb24iOiAiVGhlIGNvbmNsdXN0aW9uIG9mIHRoaXMgZXhhbXBsZSIKICAgICAgfQogICAgfSwKICAgIHsKICAgICAgImZ1bGxVcmwiOiAidXJuOnV1aWQ6N2MxNmM5YzAtYzQ3MS00MDk4LWFlMTgtNDhhYmNhZGQ4ZDkxIiwKICAgICAgInJlc291cmNlIjogewogICAgICAgICJyZXNvdXJjZVR5cGUiOiAiU3BlY2ltZW4iLAogICAgICAgICJpZCI6ICJzcGVjaW1lbjYwIiwKICAgICAgICAibWV0YSI6IHsKICAgICAgICAgICJ2ZXJzaW9uSWQiOiAiMSIsCiAgICAgICAgICAicHJvZmlsZSI6IFsKICAgICAgICAgICAgImh0dHBzOi8vd3d3LmVoZWFsdGguZmdvdi5iZS9zdGFuZGFyZHMvZmhpci9TdHJ1Y3R1cmVEZWZpbml0aW9uL2JlLXNwZWNpbWVuLWxhYm9yYXRvcnkiCiAgICAgICAgICBdCiAgICAgICAgfSwKICAgICAgICAidGV4dCI6IHsKICAgICAgICAgICJzdGF0dXMiOiAiZ2VuZXJhdGVkIiwKICAgICAgICAgICJkaXYiOiAiPGRpdiB4bWxucz1cImh0dHA6Ly93d3cudzMub3JnLzE5OTkveGh0bWxcIj48cD48Yj5HZW5lcmF0ZWQgTmFycmF0aXZlPC9iPjwvcD48cD48Yj5pZGVudGlmaWVyPC9iPjogaWQ6IDE8L3A+PHA+PGI+c3RhdHVzPC9iPjogYXZhaWxhYmxlPC9wPjxwPjxiPnR5cGU8L2I+OiA8c3BhbiB0aXRsZT1cIkNvZGVzOiB7aHR0cDovL3Nub21lZC5pbmZvL3NjdCAxMTkyOTUwMDh9XCI+QSBzdHJpbmcgY2FuIGJlIGFkZGVkIHRvIG51YW5jZSBvciBleHBsYWluLjwvc3Bhbj48L3A+PHA+PGI+cmVjZWl2ZWRUaW1lPC9iPjogMjAxNS0xMS0wNDwvcD48cD48Yj5ub3RlPC9iPjogU29tZSBleHRyYSByZWxldmFudCBpbmZvcm1hdGlvbiBjb25jZXJuaW5nIHRoZSBzcGVjaW1lbjwvcD48L2Rpdj4iCiAgICAgICAgfSwKICAgICAgICAiaWRlbnRpZmllciI6IFsKICAgICAgICAgIHsKICAgICAgICAgICAgInN5c3RlbSI6ICJodHRwczovL3d3dy5HVEwtTEFCTy5iZS8iLAogICAgICAgICAgICAidmFsdWUiOiAiMSIKICAgICAgICAgIH0KICAgICAgICBdLAogICAgICAgICJzdGF0dXMiOiAiYXZhaWxhYmxlIiwKICAgICAgICAidHlwZSI6IHsKICAgICAgICAgICJjb2RpbmciOiBbCiAgICAgICAgICAgIHsKICAgICAgICAgICAgICAic3lzdGVtIjogImh0dHA6Ly9zbm9tZWQuaW5mby9zY3QiLAogICAgICAgICAgICAgICJjb2RlIjogIjExOTI5NTAwOCIsCiAgICAgICAgICAgICAgImRpc3BsYXkiOiAiQXNwaXJhdGUiCiAgICAgICAgICAgIH0KICAgICAgICAgIF0sCiAgICAgICAgICAidGV4dCI6ICJBIHN0cmluZyBjYW4gYmUgYWRkZWQgdG8gbnVhbmNlIG9yIGV4cGxhaW4uIgogICAgICAgIH0sCiAgICAgICAgInJlY2VpdmVkVGltZSI6ICIyMDE1LTExLTA0IiwKICAgICAgICAibm90ZSI6IFsKICAgICAgICAgIHsKICAgICAgICAgICAgInRleHQiOiAiU29tZSBleHRyYSByZWxldmFudCBpbmZvcm1hdGlvbiBjb25jZXJuaW5nIHRoZSBzcGVjaW1lbiIKICAgICAgICAgIH0KICAgICAgICBdCiAgICAgIH0KICAgIH0sCiAgICB7CiAgICAgICJmdWxsVXJsIjogInVybjp1dWlkOjdjMTZjOWMwLWM0NzEtNDA5OC1hZTA5LTQ4YWJjYWRkOWQ5MiIsCiAgICAgICJyZXNvdXJjZSI6IHsKICAgICAgICAicmVzb3VyY2VUeXBlIjogIk9ic2VydmF0aW9uIiwKICAgICAgICAiaWQiOiAibWFjNjAiLAogICAgICAgICJtZXRhIjogewogICAgICAgICAgInByb2ZpbGUiOiBbCiAgICAgICAgICAgICJodHRwczovL3d3dy5laGVhbHRoLmZnb3YuYmUvc3RhbmRhcmRzL2ZoaXIvU3RydWN0dXJlRGVmaW5pdGlvbi9iZS1vYnNlcnZhdGlvbi1sYWJvcmF0b3J5IgogICAgICAgICAgXQogICAgICAgIH0sCiAgICAgICAgInRleHQiOiB7CiAgICAgICAgICAic3RhdHVzIjogImdlbmVyYXRlZCIsCiAgICAgICAgICAiZGl2IjogIjxkaXYgeG1sbnM9XCJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hodG1sXCI+PHA+PGI+R2VuZXJhdGVkIE5hcnJhdGl2ZTwvYj48L3A+PHA+PC9wPjxwPjxiPmNvZGU8L2I+OiA8c3BhbiB0aXRsZT1cIkNvZGVzOiB7aHR0cDovL2xvaW5jLm9yZyA3NDU3NC01fVwiPk1hY3Jvc2NvcGljIG9ic2VydmF0aW9uPC9zcGFuPjwvcD48cD48Yj5zdWJqZWN0PC9iPjogPGEgaHJlZj1cIiNQYXRpZW50X3BhdGllbnQxXCI+U2VlIGFib3ZlIChQYXRpZW50L3BhdGllbnQxKTwvYT48L3A+PHA+PGI+ZWZmZWN0aXZlPC9iPjogTm92IDQsIDIwMTUsIDI6MTY6MDAgUE08L3A+PHA+PGI+aXNzdWVkPC9iPjogTm92IDQsIDIwMTUsIDI6MTY6MDAgUE08L3A+PHA+PGI+cGVyZm9ybWVyPC9iPjogPGEgaHJlZj1cIiNQcmFjdGl0aW9uZXJfcHJhY3RpdGlvbmVyMTFcIj5TZWUgYWJvdmUgKFByYWN0aXRpb25lci9wcmFjdGl0aW9uZXIxMSk8L2E+PC9wPjxwPjxiPnZhbHVlPC9iPjogbWF0aWcgcHVydWxlbnQ8L3A+PC9kaXY+IgogICAgICAgIH0sCiAgICAgICAgInN0YXR1cyI6ICJmaW5hbCIsCiAgICAgICAgImNvZGUiOiB7CiAgICAgICAgICAiY29kaW5nIjogWwogICAgICAgICAgICB7CiAgICAgICAgICAgICAgInN5c3RlbSI6ICJodHRwOi8vbG9pbmMub3JnIiwKICAgICAgICAgICAgICAiY29kZSI6ICI3NDU3NC01IiwKICAgICAgICAgICAgICAiZGlzcGxheSI6ICJNYWNyb3Njb3BpYyBvYnNlcnZhdGlvbiBbSW50ZXJwcmV0YXRpb25dIGluIFVuc3BlY2lmaWVkIHNwZWNpbWVuIE5hcnJhdGl2ZSIKICAgICAgICAgICAgfQogICAgICAgICAgXSwKICAgICAgICAgICJ0ZXh0IjogIk1hY3Jvc2NvcGljIG9ic2VydmF0aW9uIgogICAgICAgIH0sCiAgICAgICAgInN1YmplY3QiOiB7CiAgICAgICAgICAicmVmZXJlbmNlIjogIlBhdGllbnQvcGF0aWVudDEiCiAgICAgICAgfSwKICAgICAgICAiZWZmZWN0aXZlRGF0ZVRpbWUiOiAiMjAxNS0xMS0wNFQwOToxNjowMC0wNTowMCIsCiAgICAgICAgImlzc3VlZCI6ICIyMDE1LTExLTA0VDA5OjE2OjAwLTA1OjAwIiwKICAgICAgICAicGVyZm9ybWVyIjogWwogICAgICAgICAgewogICAgICAgICAgICAicmVmZXJlbmNlIjogIlByYWN0aXRpb25lci9wcmFjdGl0aW9uZXIxMSIKICAgICAgICAgIH0KICAgICAgICBdLAogICAgICAgICJ2YWx1ZVN0cmluZyI6ICJtYXRpZyBwdXJ1bGVudCIKICAgICAgfQogICAgfSwKICAgIHsKICAgICAgImZ1bGxVcmwiOiAidXJuOnV1aWQ6N2MxNmM5YzAtYzQ3MS00MDk4LWFlMDktNDhhYmNhZGQ5ZDkzIiwKICAgICAgInJlc291cmNlIjogewogICAgICAgICJyZXNvdXJjZVR5cGUiOiAiT2JzZXJ2YXRpb24iLAogICAgICAgICJpZCI6ICJjdWx0NjAiLAogICAgICAgICJtZXRhIjogewogICAgICAgICAgInByb2ZpbGUiOiBbCiAgICAgICAgICAgICJodHRwczovL3d3dy5laGVhbHRoLmZnb3YuYmUvc3RhbmRhcmRzL2ZoaXIvU3RydWN0dXJlRGVmaW5pdGlvbi9iZS1vYnNlcnZhdGlvbi1sYWJvcmF0b3J5IgogICAgICAgICAgXQogICAgICAgIH0sCiAgICAgICAgInRleHQiOiB7CiAgICAgICAgICAic3RhdHVzIjogImdlbmVyYXRlZCIsCiAgICAgICAgICAiZGl2IjogIjxkaXYgeG1sbnM9XCJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hodG1sXCI+PHA+PGI+R2VuZXJhdGVkIE5hcnJhdGl2ZTwvYj48L3A+PHA+PC9wPjxwPjxiPmNvZGU8L2I+OiA8c3BhbiB0aXRsZT1cIkNvZGVzOiB7aHR0cDovL2xvaW5jLm9yZyA0MzQxMS04fVwiPkN1bHR1cmU8L3NwYW4+PC9wPjxwPjxiPnN1YmplY3Q8L2I+OiA8YSBocmVmPVwiI1BhdGllbnRfcGF0aWVudDFcIj5TZWUgYWJvdmUgKFBhdGllbnQvcGF0aWVudDEpPC9hPjwvcD48cD48Yj5lZmZlY3RpdmU8L2I+OiBOb3YgNCwgMjAxNSwgMjoxNjowMCBQTTwvcD48cD48Yj5pc3N1ZWQ8L2I+OiBOb3YgNCwgMjAxNSwgMjoxNjowMCBQTTwvcD48cD48Yj5wZXJmb3JtZXI8L2I+OiA8YSBocmVmPVwiI1ByYWN0aXRpb25lcl9wcmFjdGl0aW9uZXIxMVwiPlNlZSBhYm92ZSAoUHJhY3RpdGlvbmVyL3ByYWN0aXRpb25lcjExKTwvYT48L3A+PHA+PGI+dmFsdWU8L2I+OiBDb21tZW5zYWxlbiArLTwvcD48L2Rpdj4iCiAgICAgICAgfSwKICAgICAgICAic3RhdHVzIjogImZpbmFsIiwKICAgICAgICAiY29kZSI6IHsKICAgICAgICAgICJjb2RpbmciOiBbCiAgICAgICAgICAgIHsKICAgICAgICAgICAgICAic3lzdGVtIjogImh0dHA6Ly9sb2luYy5vcmciLAogICAgICAgICAgICAgICJjb2RlIjogIjQzNDExLTgiLAogICAgICAgICAgICAgICJkaXNwbGF5IjogIkJhY3RlcmlhIGlkZW50aWZpZWQgaW4gQXNwaXJhdGUgYnkgQ3VsdHVyZSIKICAgICAgICAgICAgfQogICAgICAgICAgXSwKICAgICAgICAgICJ0ZXh0IjogIkN1bHR1cmUiCiAgICAgICAgfSwKICAgICAgICAic3ViamVjdCI6IHsKICAgICAgICAgICJyZWZlcmVuY2UiOiAiUGF0aWVudC9wYXRpZW50MSIKICAgICAgICB9LAogICAgICAgICJlZmZlY3RpdmVEYXRlVGltZSI6ICIyMDE1LTExLTA0VDA5OjE2OjAwLTA1OjAwIiwKICAgICAgICAiaXNzdWVkIjogIjIwMTUtMTEtMDRUMDk6MTY6MDAtMDU6MDAiLAogICAgICAgICJwZXJmb3JtZXIiOiBbCiAgICAgICAgICB7CiAgICAgICAgICAgICJyZWZlcmVuY2UiOiAiUHJhY3RpdGlvbmVyL3ByYWN0aXRpb25lcjExIgogICAgICAgICAgfQogICAgICAgIF0sCiAgICAgICAgInZhbHVlU3RyaW5nIjogIkNvbW1lbnNhbGVuICstIgogICAgICB9CiAgICB9LAogICAgewogICAgICAiZnVsbFVybCI6ICJ1cm46dXVpZDo3YzE2YzljMC1jNDcxLTQwOTgtYWUxOC00OGFiY2FkZDhkOTQiLAogICAgICAicmVzb3VyY2UiOiB7CiAgICAgICAgInJlc291cmNlVHlwZSI6ICJPcmdhbml6YXRpb24iLAogICAgICAgICJpZCI6ICJvcmdhbml6YXRpb24xMCIsCiAgICAgICAgIm1ldGEiOiB7CiAgICAgICAgICAicHJvZmlsZSI6IFsKICAgICAgICAgICAgImh0dHBzOi8vd3d3LmVoZWFsdGguZmdvdi5iZS9zdGFuZGFyZHMvZmhpci9TdHJ1Y3R1cmVEZWZpbml0aW9uL2JlLW9yZ2FuaXphdGlvbiIKICAgICAgICAgIF0KICAgICAgICB9LAogICAgICAgICJ0ZXh0IjogewogICAgICAgICAgInN0YXR1cyI6ICJnZW5lcmF0ZWQiLAogICAgICAgICAgImRpdiI6ICI8ZGl2IHhtbG5zPVwiaHR0cDovL3d3dy53My5vcmcvMTk5OS94aHRtbFwiPjxwPjxiPkdlbmVyYXRlZCBOYXJyYXRpdmU8L2I+PC9wPjxwPjxiPmlkZW50aWZpZXI8L2I+OiBNZWRpY2FsIExpY2Vuc2UgbnVtYmVyOiA4MTE2NTM0Mzk5OCAoT0ZGSUNJQUwpPC9wPjxwPjxiPmFjdGl2ZTwvYj46IHRydWU8L3A+PHA+PGI+dHlwZTwvYj46IDxzcGFuIHRpdGxlPVwiQ29kZXM6IHtodHRwczovL3d3dy5laGVhbHRoLmZnb3YuYmUvc3RhbmRhcmRzL2ZoaXIvQ29kZVN5c3RlbS9jZC1oY3BhcnR5IG9yZ2xhYm9yYXRvcnl9XCI+SW5kZXBlbmRlbnQgbGFib3JhdG9yeTwvc3Bhbj48L3A+PHA+PGI+bmFtZTwvYj46IEdUTCAtIEdlbmVyYWwgVGVzdGluZyBMYWJvcmF0b3J5PC9wPjxwPjxiPnRlbGVjb208L2I+OiBwaDogMzIyNjc1MTk5KFdPUkspPC9wPjxwPjxiPmFkZHJlc3M8L2I+OiBWdWxjYW5zc3RyYWF0IDEyMCwgMTAwMCBCcnVzc2VsKFdPUkspPC9wPjwvZGl2PiIKICAgICAgICB9LAogICAgICAgICJpZGVudGlmaWVyIjogWwogICAgICAgICAgewogICAgICAgICAgICAidXNlIjogIm9mZmljaWFsIiwKICAgICAgICAgICAgInR5cGUiOiB7CiAgICAgICAgICAgICAgImNvZGluZyI6IFsKICAgICAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgICAgInN5c3RlbSI6ICJodHRwOi8vdGVybWlub2xvZ3kuaGw3Lm9yZy9Db2RlU3lzdGVtL3YyLTAyMDMiLAogICAgICAgICAgICAgICAgICAiY29kZSI6ICJNRCIsCiAgICAgICAgICAgICAgICAgICJkaXNwbGF5IjogIk1lZGljYWwgTGljZW5zZSBudW1iZXIiCiAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgICAgXQogICAgICAgICAgICB9LAogICAgICAgICAgICAic3lzdGVtIjogImh0dHBzOi8vd3d3LmVoZWFsdGguZmdvdi5iZS9zdGFuZGFyZHMvZmhpci9OYW1pbmdTeXN0ZW0vbmloZGkiLAogICAgICAgICAgICAidmFsdWUiOiAiODExNjUzNDM5OTgiCiAgICAgICAgICB9CiAgICAgICAgXSwKICAgICAgICAiYWN0aXZlIjogdHJ1ZSwKICAgICAgICAidHlwZSI6IFsKICAgICAgICAgIHsKICAgICAgICAgICAgImNvZGluZyI6IFsKICAgICAgICAgICAgICB7CiAgICAgICAgICAgICAgICAic3lzdGVtIjogImh0dHBzOi8vd3d3LmVoZWFsdGguZmdvdi5iZS9zdGFuZGFyZHMvZmhpci9Db2RlU3lzdGVtL2NkLWhjcGFydHkiLAogICAgICAgICAgICAgICAgImNvZGUiOiAib3JnbGFib3JhdG9yeSIKICAgICAgICAgICAgICB9CiAgICAgICAgICAgIF0KICAgICAgICAgIH0KICAgICAgICBdLAogICAgICAgICJuYW1lIjogIkdUTCAtIEdlbmVyYWwgVGVzdGluZyBMYWJvcmF0b3J5IiwKICAgICAgICAidGVsZWNvbSI6IFsKICAgICAgICAgIHsKICAgICAgICAgICAgInN5c3RlbSI6ICJwaG9uZSIsCiAgICAgICAgICAgICJ2YWx1ZSI6ICIzMjI2NzUxOTkiLAogICAgICAgICAgICAidXNlIjogIndvcmsiCiAgICAgICAgICB9CiAgICAgICAgXSwKICAgICAgICAiYWRkcmVzcyI6IFsKICAgICAgICAgIHsKICAgICAgICAgICAgImV4dGVuc2lvbiI6IFsKICAgICAgICAgICAgICB7CiAgICAgICAgICAgICAgICAidXJsIjogImh0dHA6Ly9obDcub3JnL2ZoaXIvU3RydWN0dXJlRGVmaW5pdGlvbi9sYW5ndWFnZSIsCiAgICAgICAgICAgICAgICAidmFsdWVDb2RlIjogIm5sIgogICAgICAgICAgICAgIH0KICAgICAgICAgICAgXSwKICAgICAgICAgICAgInVzZSI6ICJ3b3JrIiwKICAgICAgICAgICAgInR5cGUiOiAiYm90aCIsCiAgICAgICAgICAgICJ0ZXh0IjogIlZ1bGNhbnNzdHJhYXQgMTIwLCAxMDAwIEJydXNzZWwiLAogICAgICAgICAgICAibGluZSI6IFsKICAgICAgICAgICAgICAiVnVsY2Fuc3N0cmFhdCAxMjAiCiAgICAgICAgICAgIF0sCiAgICAgICAgICAgICJfbGluZSI6IFsKICAgICAgICAgICAgICB7CiAgICAgICAgICAgICAgICAiZXh0ZW5zaW9uIjogWwogICAgICAgICAgICAgICAgICB7CiAgICAgICAgICAgICAgICAgICAgInVybCI6ICJodHRwOi8vaGw3Lm9yZy9maGlyL1N0cnVjdHVyZURlZmluaXRpb24vaXNvMjEwOTAtQURYUC1zdHJlZXROYW1lIiwKICAgICAgICAgICAgICAgICAgICAidmFsdWVTdHJpbmciOiAiVnVsY2Fuc3N0cmFhdCIKICAgICAgICAgICAgICAgICAgfSwKICAgICAgICAgICAgICAgICAgewogICAgICAgICAgICAgICAgICAgICJ1cmwiOiAiaHR0cDovL2hsNy5vcmcvZmhpci9TdHJ1Y3R1cmVEZWZpbml0aW9uL2lzbzIxMDkwLUFEWFAtaG91c2VOdW1iZXIiLAogICAgICAgICAgICAgICAgICAgICJ2YWx1ZVN0cmluZyI6ICIxMjAiCiAgICAgICAgICAgICAgICAgIH0KICAgICAgICAgICAgICAgIF0KICAgICAgICAgICAgICB9CiAgICAgICAgICAgIF0sCiAgICAgICAgICAgICJjaXR5IjogIkJydXNzZWwiLAogICAgICAgICAgICAicG9zdGFsQ29kZSI6ICIxMDAwIiwKICAgICAgICAgICAgImNvdW50cnkiOiAiQkUiCiAgICAgICAgICB9CiAgICAgICAgXQogICAgICB9CiAgICB9LAogICAgewogICAgICAiZnVsbFVybCI6ICJ1cm46dXVpZDo3YzE2YzljMC1jNDcxLTQwOTgtYWUxOC00OGFiY2FkZDhkOTUiLAogICAgICAicmVzb3VyY2UiOiB7CiAgICAgICAgInJlc291cmNlVHlwZSI6ICJQcmFjdGl0aW9uZXIiLAogICAgICAgICJpZCI6ICJwcmFjdGl0aW9uZXIxMSIsCiAgICAgICAgIm1ldGEiOiB7CiAgICAgICAgICAicHJvZmlsZSI6IFsKICAgICAgICAgICAgImh0dHBzOi8vd3d3LmVoZWFsdGguZmdvdi5iZS9zdGFuZGFyZHMvZmhpci9TdHJ1Y3R1cmVEZWZpbml0aW9uL2JlLXByYWN0aXRpb25lciIKICAgICAgICAgIF0KICAgICAgICB9LAogICAgICAgICJ0ZXh0IjogewogICAgICAgICAgInN0YXR1cyI6ICJnZW5lcmF0ZWQiLAogICAgICAgICAgImRpdiI6ICI8ZGl2IHhtbG5zPVwiaHR0cDovL3d3dy53My5vcmcvMTk5OS94aHRtbFwiPjxwPjxiPkdlbmVyYXRlZCBOYXJyYXRpdmU8L2I+PC9wPjxwPjxiPmlkZW50aWZpZXI8L2I+OiBpZDogNTU0NDg4OTk3IChPRkZJQ0lBTCk8L3A+PHA+PGI+bmFtZTwvYj46IE5pY29sYXMgRGF2b3VzdCAoT0ZGSUNJQUwpPC9wPjxwPjxiPnRlbGVjb208L2I+OiA8YSBocmVmPVwibWFpbHRvOm5pY29sYXMuZGF2b3VzdEBndGwuYmVcIj5uaWNvbGFzLmRhdm91c3RAZ3RsLmJlPC9hPiwgcGg6IDAyMjY3NTE5OChXT1JLKSwgZmF4OiAwMjI2NzUyMDkoV09SSyk8L3A+PC9kaXY+IgogICAgICAgIH0sCiAgICAgICAgImlkZW50aWZpZXIiOiBbCiAgICAgICAgICB7CiAgICAgICAgICAgICJ1c2UiOiAib2ZmaWNpYWwiLAogICAgICAgICAgICAic3lzdGVtIjogImh0dHBzOi8vd3d3LmVoZWFsdGguZmdvdi5iZS9zdGFuZGFyZHMvZmhpci9OYW1pbmdTeXN0ZW0vbmloZGkiLAogICAgICAgICAgICAidmFsdWUiOiAiNTU0NDg4OTk3IgogICAgICAgICAgfQogICAgICAgIF0sCiAgICAgICAgIm5hbWUiOiBbCiAgICAgICAgICB7CiAgICAgICAgICAgICJ1c2UiOiAib2ZmaWNpYWwiLAogICAgICAgICAgICAiZmFtaWx5IjogIkRhdm91c3QiLAogICAgICAgICAgICAiZ2l2ZW4iOiBbCiAgICAgICAgICAgICAgIk5pY29sYXMiCiAgICAgICAgICAgIF0KICAgICAgICAgIH0KICAgICAgICBdLAogICAgICAgICJ0ZWxlY29tIjogWwogICAgICAgICAgewogICAgICAgICAgICAic3lzdGVtIjogImVtYWlsIiwKICAgICAgICAgICAgInZhbHVlIjogIm5pY29sYXMuZGF2b3VzdEBndGwuYmUiLAogICAgICAgICAgICAidXNlIjogIndvcmsiCiAgICAgICAgICB9LAogICAgICAgICAgewogICAgICAgICAgICAic3lzdGVtIjogInBob25lIiwKICAgICAgICAgICAgInZhbHVlIjogIjAyMjY3NTE5OCIsCiAgICAgICAgICAgICJ1c2UiOiAid29yayIKICAgICAgICAgIH0sCiAgICAgICAgICB7CiAgICAgICAgICAgICJzeXN0ZW0iOiAiZmF4IiwKICAgICAgICAgICAgInZhbHVlIjogIjAyMjY3NTIwOSIsCiAgICAgICAgICAgICJ1c2UiOiAid29yayIKICAgICAgICAgIH0KICAgICAgICBdCiAgICAgIH0KICAgIH0sCiAgICB7CiAgICAgICJmdWxsVXJsIjogInVybjp1dWlkOjdjMTZjOWMwLWM0NzEtNDA5OC1hZTE4LTQ4YWJjYWRkOGQ5NiIsCiAgICAgICJyZXNvdXJjZSI6IHsKICAgICAgICAicmVzb3VyY2VUeXBlIjogIlBhdGllbnQiLAogICAgICAgICJpZCI6ICJwYXRpZW50MSIsCiAgICAgICAgIm1ldGEiOiB7CiAgICAgICAgICAicHJvZmlsZSI6IFsKICAgICAgICAgICAgImh0dHBzOi8vd3d3LmVoZWFsdGguZmdvdi5iZS9zdGFuZGFyZHMvZmhpci9TdHJ1Y3R1cmVEZWZpbml0aW9uL2JlLXBhdGllbnQiCiAgICAgICAgICBdCiAgICAgICAgfSwKICAgICAgICAidGV4dCI6IHsKICAgICAgICAgICJzdGF0dXMiOiAiZ2VuZXJhdGVkIiwKICAgICAgICAgICJkaXYiOiAiPGRpdiB4bWxucz1cImh0dHA6Ly93d3cudzMub3JnLzE5OTkveGh0bWxcIj48cD48Yj5HZW5lcmF0ZWQgTmFycmF0aXZlPC9iPjwvcD48cD48Yj5pZGVudGlmaWVyPC9iPjogaWQ6IDc5MTIxMTM3NzQwIChPRkZJQ0lBTCk8L3A+PHA+PGI+YWN0aXZlPC9iPjogdHJ1ZTwvcD48cD48Yj5uYW1lPC9iPjogSm9zZXBoaW5lIE5lc3NhIExhIFBhcmFkaXNpbyA8L3A+PHA+PGI+dGVsZWNvbTwvYj46IDxhIGhyZWY9XCJtYWlsdG86bmVzc2EubGFwYXJhZGlzaW9AYmVsZ2l1bS5iZVwiPm5lc3NhLmxhcGFyYWRpc2lvQGJlbGdpdW0uYmU8L2E+LCA8YSBocmVmPVwidGVsOiszMjI0NzY3OTI5NzlcIj4rMzIyNDc2NzkyOTc5PC9hPiwgPGEgaHJlZj1cInRlbDorMzIyNjcxODY1NVwiPiszMjI2NzE4NjU1PC9hPiwgPGEgaHJlZj1cInRlbDorMzIyNDc2Nzk5XCI+KzMyMjQ3Njc5OTwvYT48L3A+PHA+PGI+Z2VuZGVyPC9iPjogZmVtYWxlPC9wPjxwPjxiPmJpcnRoRGF0ZTwvYj46IDE5NzktMTItMTE8L3A+PHA+PGI+YWRkcmVzczwvYj46IFNsb29yZGVsbGUgNDIsIDExNjAgT3VkZXJnZW0oSE9NRSk8L3A+PC9kaXY+IgogICAgICAgIH0sCiAgICAgICAgImlkZW50aWZpZXIiOiBbCiAgICAgICAgICB7CiAgICAgICAgICAgICJ1c2UiOiAib2ZmaWNpYWwiLAogICAgICAgICAgICAic3lzdGVtIjogImh0dHBzOi8vd3d3LmVoZWFsdGguZmdvdi5iZS9zdGFuZGFyZHMvZmhpci9OYW1pbmdTeXN0ZW0vc3NpbiIsCiAgICAgICAgICAgICJ2YWx1ZSI6ICI3OTEyMTEzNzc0MCIKICAgICAgICAgIH0KICAgICAgICBdLAogICAgICAgICJhY3RpdmUiOiB0cnVlLAogICAgICAgICJuYW1lIjogWwogICAgICAgICAgewogICAgICAgICAgICAiZmFtaWx5IjogIkxhIFBhcmFkaXNpbyIsCiAgICAgICAgICAgICJnaXZlbiI6IFsKICAgICAgICAgICAgICAiSm9zZXBoaW5lIiwKICAgICAgICAgICAgICAiTmVzc2EiCiAgICAgICAgICAgIF0KICAgICAgICAgIH0KICAgICAgICBdLAogICAgICAgICJ0ZWxlY29tIjogWwogICAgICAgICAgewogICAgICAgICAgICAic3lzdGVtIjogImVtYWlsIiwKICAgICAgICAgICAgInZhbHVlIjogIm5lc3NhLmxhcGFyYWRpc2lvQGJlbGdpdW0uYmUiCiAgICAgICAgICB9LAogICAgICAgICAgewogICAgICAgICAgICAic3lzdGVtIjogInBob25lIiwKICAgICAgICAgICAgInZhbHVlIjogIiszMjI0NzY3OTI5NzkiLAogICAgICAgICAgICAidXNlIjogIm1vYmlsZSIKICAgICAgICAgIH0sCiAgICAgICAgICB7CiAgICAgICAgICAgICJzeXN0ZW0iOiAicGhvbmUiLAogICAgICAgICAgICAidmFsdWUiOiAiKzMyMjY3MTg2NTUiLAogICAgICAgICAgICAidXNlIjogImhvbWUiCiAgICAgICAgICB9LAogICAgICAgICAgewogICAgICAgICAgICAic3lzdGVtIjogInBob25lIiwKICAgICAgICAgICAgInZhbHVlIjogIiszMjI0NzY3OTkiLAogICAgICAgICAgICAidXNlIjogIndvcmsiCiAgICAgICAgICB9CiAgICAgICAgXSwKICAgICAgICAiZ2VuZGVyIjogImZlbWFsZSIsCiAgICAgICAgImJpcnRoRGF0ZSI6ICIxOTc5LTEyLTExIiwKICAgICAgICAiX2JpcnRoRGF0ZSI6IHsKICAgICAgICAgICJleHRlbnNpb24iOiBbCiAgICAgICAgICAgIHsKICAgICAgICAgICAgICAidXJsIjogImh0dHA6Ly9obDcub3JnL2ZoaXIvU3RydWN0dXJlRGVmaW5pdGlvbi9wYXRpZW50LWJpcnRoVGltZSIsCiAgICAgICAgICAgICAgInZhbHVlRGF0ZVRpbWUiOiAiMTk3OS0xMi0xMVQxMzoyODoxNy0wNTowMCIKICAgICAgICAgICAgfQogICAgICAgICAgXQogICAgICAgIH0sCiAgICAgICAgImFkZHJlc3MiOiBbCiAgICAgICAgICB7CiAgICAgICAgICAgICJ1c2UiOiAiaG9tZSIsCiAgICAgICAgICAgICJ0eXBlIjogImJvdGgiLAogICAgICAgICAgICAidGV4dCI6ICJTbG9vcmRlbGxlIDQyLCAxMTYwIE91ZGVyZ2VtIiwKICAgICAgICAgICAgImxpbmUiOiBbCiAgICAgICAgICAgICAgIlNsb29yZGVsbGUgNDIiCiAgICAgICAgICAgIF0sCiAgICAgICAgICAgICJjaXR5IjogIk91ZGVyZ2VtIiwKICAgICAgICAgICAgInBvc3RhbENvZGUiOiAiMTE2MCIsCiAgICAgICAgICAgICJjb3VudHJ5IjogIkJFIgogICAgICAgICAgfQogICAgICAgIF0KICAgICAgfQogICAgfSwKICAgIHsKICAgICAgImZ1bGxVcmwiOiAidXJuOnV1aWQ6N2MxNmM5YzAtYzQ3MS00MDk4LWFlMTgtNDhhYmNhZGQ4ZDk3IiwKICAgICAgInJlc291cmNlIjogewogICAgICAgICJyZXNvdXJjZVR5cGUiOiAiU2VydmljZVJlcXVlc3QiLAogICAgICAgICJpZCI6ICJzZXJ2aWNlcmVxdWVzdDYwIiwKICAgICAgICAidGV4dCI6IHsKICAgICAgICAgICJzdGF0dXMiOiAiZ2VuZXJhdGVkIiwKICAgICAgICAgICJkaXYiOiAiPGRpdiB4bWxucz1cImh0dHA6Ly93d3cudzMub3JnLzE5OTkveGh0bWxcIj48cD48Yj5HZW5lcmF0ZWQgTmFycmF0aXZlPC9iPjwvcD48cD48Yj5pZGVudGlmaWVyPC9iPjogaWQ6IDEyMzM0NTQ2PC9wPjxwPjxiPnN0YXR1czwvYj46IGFjdGl2ZTwvcD48cD48Yj5pbnRlbnQ8L2I+OiBvcmRlcjwvcD48cD48Yj5jYXRlZ29yeTwvYj46IDxzcGFuIHRpdGxlPVwiQ29kZXM6IHtodHRwOi8vc25vbWVkLmluZm8vc2N0IDEwODI1MjAwN31cIj5MYWJvcmF0b3J5IHByb2NlZHVyZTwvc3Bhbj48L3A+PHA+PGI+c3ViamVjdDwvYj46IDxhIGhyZWY9XCIjUGF0aWVudF9wYXRpZW50MVwiPlNlZSBhYm92ZSAoUGF0aWVudC9wYXRpZW50MSk8L2E+PC9wPjxwPjxiPmF1dGhvcmVkT248L2I+OiBOb3YgMSwgMjAxNSwgMTo0MTowMCBQTTwvcD48cD48Yj5yZXF1ZXN0ZXI8L2I+OiA8YSBocmVmPVwiI1ByYWN0aXRpb25lcl9wcmFjdGl0aW9uZXIxXCI+U2VlIGFib3ZlIChQcmFjdGl0aW9uZXIvcHJhY3RpdGlvbmVyMSk8L2E+PC9wPjxwPjxiPnNwZWNpbWVuPC9iPjogPGEgaHJlZj1cIiNTcGVjaW1lbl9zcGVjaW1lbjYwXCI+U2VlIGFib3ZlIChTcGVjaW1lbi9zcGVjaW1lbjYwKTwvYT48L3A+PC9kaXY+IgogICAgICAgIH0sCiAgICAgICAgImlkZW50aWZpZXIiOiBbCiAgICAgICAgICB7CiAgICAgICAgICAgICJzeXN0ZW0iOiAiaHR0cHM6Ly93d3cuR1RMLUxBQk8uYmUvb3JkZXJpbmdzeXN0ZW0iLAogICAgICAgICAgICAidmFsdWUiOiAiMTIzMzQ1NDYiCiAgICAgICAgICB9CiAgICAgICAgXSwKICAgICAgICAic3RhdHVzIjogImFjdGl2ZSIsCiAgICAgICAgImludGVudCI6ICJvcmRlciIsCiAgICAgICAgImNhdGVnb3J5IjogWwogICAgICAgICAgewogICAgICAgICAgICAiY29kaW5nIjogWwogICAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgICJzeXN0ZW0iOiAiaHR0cDovL3Nub21lZC5pbmZvL3NjdCIsCiAgICAgICAgICAgICAgICAiY29kZSI6ICIxMDgyNTIwMDciLAogICAgICAgICAgICAgICAgImRpc3BsYXkiOiAiTGFib3JhdG9yeSBwcm9jZWR1cmUiCiAgICAgICAgICAgICAgfQogICAgICAgICAgICBdCiAgICAgICAgICB9CiAgICAgICAgXSwKICAgICAgICAic3ViamVjdCI6IHsKICAgICAgICAgICJyZWZlcmVuY2UiOiAiUGF0aWVudC9wYXRpZW50MSIKICAgICAgICB9LAogICAgICAgICJhdXRob3JlZE9uIjogIjIwMTUtMTEtMDFUMTQ6NDE6MDArMDE6MDAiLAogICAgICAgICJyZXF1ZXN0ZXIiOiB7CiAgICAgICAgICAicmVmZXJlbmNlIjogIlByYWN0aXRpb25lci9wcmFjdGl0aW9uZXIxIgogICAgICAgIH0sCiAgICAgICAgInNwZWNpbWVuIjogWwogICAgICAgICAgewogICAgICAgICAgICAicmVmZXJlbmNlIjogIlNwZWNpbWVuL3NwZWNpbWVuNjAiCiAgICAgICAgICB9CiAgICAgICAgXQogICAgICB9CiAgICB9LAogICAgewogICAgICAiZnVsbFVybCI6ICJ1cm46dXVpZDo3YzE2YzljMC1jNDcxLTQwOTgtYWUxOC00OGFiY2FkZDhkOTgiLAogICAgICAicmVzb3VyY2UiOiB7CiAgICAgICAgInJlc291cmNlVHlwZSI6ICJQcmFjdGl0aW9uZXIiLAogICAgICAgICJpZCI6ICJwcmFjdGl0aW9uZXIxIiwKICAgICAgICAibWV0YSI6IHsKICAgICAgICAgICJwcm9maWxlIjogWwogICAgICAgICAgICAiaHR0cHM6Ly93d3cuZWhlYWx0aC5mZ292LmJlL3N0YW5kYXJkcy9maGlyL1N0cnVjdHVyZURlZmluaXRpb24vYmUtcHJhY3RpdGlvbmVyIgogICAgICAgICAgXQogICAgICAgIH0sCiAgICAgICAgInRleHQiOiB7CiAgICAgICAgICAic3RhdHVzIjogImdlbmVyYXRlZCIsCiAgICAgICAgICAiZGl2IjogIjxkaXYgeG1sbnM9XCJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hodG1sXCI+PHA+PGI+R2VuZXJhdGVkIE5hcnJhdGl2ZTwvYj48L3A+PHA+PGI+aWRlbnRpZmllcjwvYj46IGlkOiAxODc0OTcwNDQ3NyAoT0ZGSUNJQUwpPC9wPjxwPjxiPm5hbWU8L2I+OiBLYXRoZXJpbmUgUHVsYXNraSAoT0ZGSUNJQUwpPC9wPjxwPjxiPnRlbGVjb208L2I+OiA8YSBocmVmPVwibWFpbHRvOmthdGhlcmluZS5wdWxhc2tpQGVudGVycHJpc2Vob3NwaXRhbC5iZVwiPmthdGhlcmluZS5wdWxhc2tpQGVudGVycHJpc2Vob3NwaXRhbC5iZTwvYT4sIHBoOiAwMjI2NzUxOTgoV09SSyksIGZheDogMDIyNjc1MjA5KFdPUkspPC9wPjxwPjxiPmFkZHJlc3M8L2I+OiBKdXBpdGVybGFhbiA1LCAxODUzIEdyaW1iZXJnZW4oSE9NRSk8L3A+PC9kaXY+IgogICAgICAgIH0sCiAgICAgICAgImlkZW50aWZpZXIiOiBbCiAgICAgICAgICB7CiAgICAgICAgICAgICJ1c2UiOiAib2ZmaWNpYWwiLAogICAgICAgICAgICAic3lzdGVtIjogImh0dHBzOi8vd3d3LmVoZWFsdGguZmdvdi5iZS9zdGFuZGFyZHMvZmhpci9OYW1pbmdTeXN0ZW0vbmloZGkiLAogICAgICAgICAgICAidmFsdWUiOiAiMTg3NDk3MDQ0NzciCiAgICAgICAgICB9CiAgICAgICAgXSwKICAgICAgICAibmFtZSI6IFsKICAgICAgICAgIHsKICAgICAgICAgICAgInVzZSI6ICJvZmZpY2lhbCIsCiAgICAgICAgICAgICJmYW1pbHkiOiAiUHVsYXNraSIsCiAgICAgICAgICAgICJnaXZlbiI6IFsKICAgICAgICAgICAgICAiS2F0aGVyaW5lIgogICAgICAgICAgICBdLAogICAgICAgICAgICAic3VmZml4IjogWwogICAgICAgICAgICAgICJNRCIKICAgICAgICAgICAgXQogICAgICAgICAgfQogICAgICAgIF0sCiAgICAgICAgInRlbGVjb20iOiBbCiAgICAgICAgICB7CiAgICAgICAgICAgICJzeXN0ZW0iOiAiZW1haWwiLAogICAgICAgICAgICAidmFsdWUiOiAia2F0aGVyaW5lLnB1bGFza2lAZW50ZXJwcmlzZWhvc3BpdGFsLmJlIiwKICAgICAgICAgICAgInVzZSI6ICJ3b3JrIgogICAgICAgICAgfSwKICAgICAgICAgIHsKICAgICAgICAgICAgInN5c3RlbSI6ICJwaG9uZSIsCiAgICAgICAgICAgICJ2YWx1ZSI6ICIwMjI2NzUxOTgiLAogICAgICAgICAgICAidXNlIjogIndvcmsiCiAgICAgICAgICB9LAogICAgICAgICAgewogICAgICAgICAgICAic3lzdGVtIjogImZheCIsCiAgICAgICAgICAgICJ2YWx1ZSI6ICIwMjI2NzUyMDkiLAogICAgICAgICAgICAidXNlIjogIndvcmsiCiAgICAgICAgICB9CiAgICAgICAgXSwKICAgICAgICAiYWRkcmVzcyI6IFsKICAgICAgICAgIHsKICAgICAgICAgICAgInVzZSI6ICJob21lIiwKICAgICAgICAgICAgInRleHQiOiAiSnVwaXRlcmxhYW4gNSwgMTg1MyBHcmltYmVyZ2VuIiwKICAgICAgICAgICAgImxpbmUiOiBbCiAgICAgICAgICAgICAgIkp1cGl0ZXJsYWFuIDUiCiAgICAgICAgICAgIF0sCiAgICAgICAgICAgICJjaXR5IjogIkdyaW1iZXJnZW4iLAogICAgICAgICAgICAicG9zdGFsQ29kZSI6ICIxODUzIiwKICAgICAgICAgICAgImNvdW50cnkiOiAiQkUiCiAgICAgICAgICB9CiAgICAgICAgXQogICAgICB9CiAgICB9CiAgXQp9Cg==");
        FhirContext ctx = FhirContext.forR4();
        IParser parser = ctx.newJsonParser();
        Bundle stripped = (Bundle) stripNarratives(parser.parseResource(Bundle.class, new String(fhirData, StandardCharsets.UTF_8)));

        String result = new ResourceHtmlGenerator().generateDivRepresentation(ctx, stripped, null);
        assertTrue( result.startsWith("<div") );
    }

    private void createFile(String fileName, String input) throws IOException {

        File file = new File(fileName);
        Writer output = new BufferedWriter(new FileWriter(file));
        output.write(input);
        output.close();

        file.createNewFile();
    }

    static String getResourceFileAsString(String fileName) throws IOException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try (InputStream is = classLoader.getResourceAsStream(fileName)) {
            if (is == null) return null;
            try (InputStreamReader isr = new InputStreamReader(is);
                 BufferedReader reader = new BufferedReader(isr)) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        }
    }
}
