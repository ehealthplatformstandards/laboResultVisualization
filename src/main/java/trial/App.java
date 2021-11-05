package trial;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative.CustomThymeleafNarrativeGenerator;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r4.model.Bundle;

import java.io.IOException;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;

public class App 
{
    public static void main(String[] args ) throws IOException {
        final FhirContext ctx = FhirContext.forR4();
        final IParser parser = ctx.newJsonParser().setPrettyPrint(true);
        // example url: http://build.fhir.org/ig/hl7-be/hl7-be-fhir-laboratory-report/Bundle-antibiogramIsNegativeAsBundleCollection.html
        // raw url: http://build.fhir.org/ig/hl7-be/hl7-be-fhir-laboratory-report/Bundle-antibiogramIsNegativeAsBundleCollection.json
        final Bundle bundle = parser.parseResource(Bundle.class, new String(readAllBytes(get("src/main/resources/example.json"))));

        // construct custom narratives generator
        // "file:" can also be "classpath:"
        final String propFile = "file:src/main/resources/narrative.properties";
        final CustomThymeleafNarrativeGenerator gen = new CustomThymeleafNarrativeGenerator(propFile);

        // suppress provided narratives
        final String narrativesSuppressed = parser
                .setSuppressNarratives(true)
                .encodeResourceToString(bundle);

        // regenerate narratives from templates
        final Bundle narrativesGenerated = parser.parseResource(Bundle.class, narrativesSuppressed);
        ctx.setNarrativeGenerator(gen);//setNarrativeGenerator(new DefaultThymeleafNarrativeGenerator());
        final String output = parser
                .setSuppressNarratives(false)
                .encodeResourceToString(narrativesGenerated);

        System.out.println(output);
    }
}
