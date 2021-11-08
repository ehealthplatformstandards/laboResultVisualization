package be.fgov.ehealth.fhir.laboratoryreport.cmd;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative.CustomThymeleafNarrativeGenerator;
import ca.uhn.fhir.narrative.INarrativeGenerator;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r4.model.Bundle;

public class HtmlNarrativeEmbed implements BundleProcessor {
    private final Visualization visualization;
    private final FhirContext ctx;
    private INarrativeGenerator generator;

    public HtmlNarrativeEmbed(Visualization visualization, FhirContext ctx) {
        this.generator = new CustomThymeleafNarrativeGenerator("file:src/main/resources/narrative.properties");
        this.visualization = visualization;
        this.ctx = ctx;
    }

    @Override
    public Integer process(Bundle bundle) {
        final IParser parser = ctx.newJsonParser().setPrettyPrint(true);
        ctx.setNarrativeGenerator(this.generator);
        final String output = parser
                .setSuppressNarratives(false)
                .encodeResourceToString(bundle);

        visualization.getOutput().println(output);
        return 0;
    }
}
