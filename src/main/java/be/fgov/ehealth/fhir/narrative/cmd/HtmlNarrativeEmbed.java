package be.fgov.ehealth.fhir.narrative.cmd;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative.CustomThymeleafNarrativeGenerator;
import ca.uhn.fhir.narrative.INarrativeGenerator;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r4.model.Resource;

public class HtmlNarrativeEmbed implements ResourceProcessor {
    private final Visualization visualization;
    private final FhirContext ctx;
    private INarrativeGenerator generator;

    public HtmlNarrativeEmbed(Visualization visualization, FhirContext ctx) {
        this.generator = new CustomThymeleafNarrativeGenerator("classpath:/narratives/diagnosticreport/narrative.properties");
        this.visualization = visualization;
        this.ctx = ctx;
    }

    @Override
    public Integer process(Resource resource) {
        final IParser parser = ctx.newJsonParser().setPrettyPrint(true);
        ctx.setNarrativeGenerator(this.generator);
        final String output = parser
                .setSuppressNarratives(false)
                .encodeResourceToString(resource);

        visualization.getOutput().println(output);
        return 0;
    }
}
