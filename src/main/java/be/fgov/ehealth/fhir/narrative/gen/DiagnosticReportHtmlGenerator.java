package be.fgov.ehealth.fhir.narrative.gen;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative.CustomThymeleafNarrativeGenerator;
import ca.uhn.fhir.narrative.INarrativeGenerator;
import ca.uhn.fhir.parser.IParser;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.FhirNarrativeUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class DiagnosticReportHtmlGenerator {
    private final INarrativeGenerator generator;

    public DiagnosticReportHtmlGenerator() {
        this.generator = new CustomThymeleafNarrativeGenerator("classpath:/narratives/diagnosticreport/narrative.properties");
    }

    public DiagnosticReportHtmlGenerator(INarrativeGenerator generator) {
        this.generator = generator;
    }

    public byte[] generateHtmlRepresentation(FhirContext fhirContext, Bundle bundle, String css) {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try(OutputStreamWriter sw = new OutputStreamWriter(bos, StandardCharsets.UTF_8)) {
            sw.append("<html><head><style>");
            sw.append(css != null ? css :
                    new String(IOUtils.toByteArray(Objects.requireNonNull(this.getClass().getResourceAsStream("/narratives/diagnosticreport/narrative.css"))), StandardCharsets.UTF_8)
            );
            sw.append("</style></head><body>\r\n");
            sw.append(generateDivRepresentation(fhirContext, bundle, css));
            sw.append("</body></html>\r\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bos.toByteArray();
    }

    public String generateDivRepresentation(FhirContext fhirContext, Bundle bundle, String css) {
        final IParser parser = fhirContext.newJsonParser().setPrettyPrint(true).setSuppressNarratives(false);
        fhirContext.setNarrativeGenerator(this.generator);
        //There must be a better way to do this
        final Bundle reparsed = parser.parseResource(Bundle.class, parser.encodeResourceToString(FhirNarrativeUtils.stripNarratives(bundle)));

        return ((DiagnosticReport) reparsed.getEntry().stream()
                    .filter(e -> e.getResource().fhirType().equals("DiagnosticReport")).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Bundle must contain a DiagnosticReport"))
                    .getResource()).getText().getDiv().toString();
    }

}
