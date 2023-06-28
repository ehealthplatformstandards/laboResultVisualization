package be.fgov.ehealth.fhir.narrative.gen;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative.CustomThymeleafNarrativeGenerator;
import ca.uhn.fhir.narrative.INarrativeGenerator;
import ca.uhn.fhir.parser.IParser;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.model.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class ResourceHtmlGenerator {
    private INarrativeGenerator generator;

    public ResourceHtmlGenerator() {
        //this.generator = new CustomThymeleafNarrativeGenerator("classpath:/narratives/diagnosticreport/narrative.properties");
    }

    public ResourceHtmlGenerator(INarrativeGenerator generator) {
        this.generator = generator;
    }

    //TODO change for other messages
    public byte[] generateHtmlRepresentation(FhirContext fhirContext, Resource resource, String css) {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try(OutputStreamWriter sw = new OutputStreamWriter(bos, StandardCharsets.UTF_8)) {
            sw.append("<html><head><style>");
            sw.append(css != null ? css :
                    new String(IOUtils.toByteArray(Objects.requireNonNull(this.getClass().getResourceAsStream("/narratives/diagnosticreport/narrative.css"))), StandardCharsets.UTF_8)
            );
            sw.append("</style></head><body>\r\n");
            sw.append(generateDivRepresentation(fhirContext, resource, css));
            sw.append("</body></html>\r\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bos.toByteArray();
    }

    public String generateDivRepresentation(FhirContext fhirContext, Resource resource, String css) {
        IParser parser = null;

        switch (resource.getResourceType().toString()) {
            case "Bundle":
                generator = new CustomThymeleafNarrativeGenerator("classpath:/narratives/diagnosticreport/narrative.properties");
                fhirContext.setNarrativeGenerator(this.generator);
                parser = fhirContext.newJsonParser().setPrettyPrint(true).setSuppressNarratives(false);
                final Bundle bundleParse = parser.parseResource(Bundle.class, parser.encodeResourceToString(FhirNarrativeUtils.stripNarratives(resource)));

                return ((DiagnosticReport) bundleParse.getEntry().stream()
                        .filter(e -> e.getResource().fhirType().equals("DiagnosticReport")).findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Bundle must contain a DiagnosticReport"))
                        .getResource()).getText().getDiv().toString();


            case "Immunization":
                generator = new CustomThymeleafNarrativeGenerator("classpath:/narratives/immunization/narrative.properties");
                fhirContext.setNarrativeGenerator(this.generator);
                parser = fhirContext.newJsonParser().setPrettyPrint(true).setSuppressNarratives(false);
                final Immunization immunizationParse = parser.parseResource(Immunization.class, parser.encodeResourceToString(FhirNarrativeUtils.stripNarratives(resource)));

                return immunizationParse.getText().getDiv().toString();

            case "AllergyIntolerance":
                generator = new CustomThymeleafNarrativeGenerator("classpath:/narratives/allergy/narrative.properties");
                fhirContext.setNarrativeGenerator(this.generator);
                parser = fhirContext.newJsonParser().setPrettyPrint(true).setSuppressNarratives(false);
                final AllergyIntolerance allergyParse = parser.parseResource(AllergyIntolerance.class, parser.encodeResourceToString(FhirNarrativeUtils.stripNarratives(resource)));

                return allergyParse.getText().getDiv().toString();

            default:
                //
                return "";
        }


    }


}