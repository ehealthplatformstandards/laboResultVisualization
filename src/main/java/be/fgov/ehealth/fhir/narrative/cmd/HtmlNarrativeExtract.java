package be.fgov.ehealth.fhir.narrative.cmd;

import org.hl7.fhir.r4.model.FhirNarrativeUtils;
import org.hl7.fhir.r4.model.Resource;

public class HtmlNarrativeExtract implements ResourceProcessor {
    private final Visualization visualization;

    public HtmlNarrativeExtract(Visualization visualization) {
        this.visualization = visualization;
    }

    @Override
    public Integer process(Resource resource) {
        FhirNarrativeUtils.collectNarratives(resource).forEach(x -> {
            visualization.getOutput().println(x.getLeft());
            visualization.getOutput().println(x.getRight());
            System.out.println("left:" + x.getLeft());
            System.out.println("left:" + x.getRight());
        });
        return 0;
    }
}
