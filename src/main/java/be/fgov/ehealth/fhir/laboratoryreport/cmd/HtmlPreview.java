package be.fgov.ehealth.fhir.laboratoryreport.cmd;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.utilities.xhtml.XhtmlComposer;
import org.hl7.fhir.utilities.xhtml.XhtmlDocument;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;

public class HtmlPreview extends ThymeleafGeneratorProvider implements BundleProcessor {
    private final Visualization visualization;
    private final FhirContext ctx;

    public HtmlPreview(Visualization visualization, FhirContext ctx) {
        super("file:src/main/resources/narrative.properties");
        this.visualization = visualization;
        this.ctx = ctx;
    }

    @Override
    public Integer process(Bundle bundle) {
        final IParser parser = ctx.newJsonParser().setPrettyPrint(true);
        ctx.setNarrativeGenerator(this.getGenerator());
        final String intermediate = parser
                .setSuppressNarratives(false)
                .encodeResourceToString(bundle);

        //There must be a better way to do this
        final Bundle reparsed = parser.setSuppressNarratives(false).parseResource(Bundle.class, intermediate);

        final XhtmlComposer doc = new XhtmlComposer(false, true);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            doc.compose(bos, ((DiagnosticReport) reparsed.getEntry().get(0).getResource()).getText().getDiv());
        } catch (IOException e) {
            System.err.println(MessageFormat.format("Cannot generate HTML: {0}", e));
            return 1;
        }

        visualization.getOutput().println(new String(bos.toByteArray(), StandardCharsets.UTF_8));
        return 0;
    }
}
