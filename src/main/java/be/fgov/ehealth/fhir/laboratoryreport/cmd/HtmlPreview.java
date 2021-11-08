package be.fgov.ehealth.fhir.laboratoryreport.cmd;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative.CustomThymeleafNarrativeGenerator;
import ca.uhn.fhir.narrative.INarrativeGenerator;
import ca.uhn.fhir.parser.IParser;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.utilities.xhtml.XhtmlComposer;
import org.hl7.fhir.utilities.xhtml.XhtmlDocument;

import java.awt.Desktop;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Objects;

public class HtmlPreview implements BundleProcessor {
    private final Visualization visualization;
    private final FhirContext ctx;
    private INarrativeGenerator generator;

    public HtmlPreview(Visualization visualization, FhirContext ctx) {
        this.generator = new CustomThymeleafNarrativeGenerator("file:src/main/resources/narrative.properties");
        this.visualization = visualization;
        this.ctx = ctx;
    }

    @Override
    public Integer process(Bundle bundle) {
        final IParser parser = ctx.newJsonParser().setPrettyPrint(true);
        ctx.setNarrativeGenerator(this.generator);
        final String intermediate = parser
                .setSuppressNarratives(false)
                .encodeResourceToString(bundle);

        //There must be a better way to do this
        final Bundle reparsed = parser.setSuppressNarratives(false).parseResource(Bundle.class, intermediate);

        final XhtmlComposer doc = new XhtmlComposer(false, true);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try(OutputStreamWriter sw = new OutputStreamWriter(bos, StandardCharsets.UTF_8)) {
            sw.append("<html><head><style>");
            sw.append(new String(IOUtils.toByteArray(Objects.requireNonNull(this.getClass().getResourceAsStream("/narratives/narrative.css")))));
            sw.append("</style></head><body>\r\n");
            sw.append(((DiagnosticReport) reparsed.getEntry().stream()
                    .filter(e -> e.getResource().fhirType().equals("DiagnosticReport")).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Bundle must contain a DiagnosticReport"))
                    .getResource()).getText().getDiv().toString());
            sw.append("</body></html>\r\n");
        } catch (IOException e) {
            e.printStackTrace();
        }


        byte[] bytes = bos.toByteArray();
        visualization.getOutput().println(new String(bytes, StandardCharsets.UTF_8));

        if (visualization.display) {
            try {
                File tmpFile = File.createTempFile("be.fgov.ehealth.fhir.laboratoryreport", ".html");
                try (OutputStream os = new BufferedOutputStream(new FileOutputStream(tmpFile))) {
                    os.write(bytes);
                }
                Desktop.getDesktop().browse(tmpFile.toURI());
            } catch (IOException e) {
                //Ignore
            }
        }
        return 0;
    }
}
