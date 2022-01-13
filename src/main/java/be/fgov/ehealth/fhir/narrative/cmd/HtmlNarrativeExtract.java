package be.fgov.ehealth.fhir.narrative.cmd;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative.CustomThymeleafNarrativeGenerator;
import ca.uhn.fhir.narrative.INarrativeGenerator;
import ca.uhn.fhir.parser.IParser;
import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.FhirNarrativeUtils;

public class HtmlNarrativeExtract implements BundleProcessor {
    private final Visualization visualization;

    public HtmlNarrativeExtract(Visualization visualization) {
        this.visualization = visualization;
    }

    @Override
    public Integer process(Bundle bundle) {
        FhirNarrativeUtils.collectNarratives(bundle).forEach(x -> {
            visualization.getOutput().println(x.getLeft());
            visualization.getOutput().println(x.getRight());
        });
        return 0;
    }
}
