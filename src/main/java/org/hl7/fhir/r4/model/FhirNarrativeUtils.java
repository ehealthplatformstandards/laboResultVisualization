package org.hl7.fhir.r4.model;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r4.model.Bundle;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public final class FhirNarrativeUtils {
    private FhirNarrativeUtils() { }

    public static Bundle stripNarratives(Bundle bundle) {
        visit(bundle);
        return bundle;
    }

    private static void visit(Base resource) {
        List<Property> children = new LinkedList<>();
        resource.listChildren(children);
        children.forEach(p -> {
            Base[] property = resource.getProperty(p.getName().hashCode(), p.getName(), false);
            if (property != null) {
                Arrays.asList(property).forEach(base -> {
                    if (base instanceof Narrative) {
                        Narrative narrative = ((Narrative) base);
                        if (narrative.status == null || (narrative.status.getValue() != Narrative.NarrativeStatus.ADDITIONAL && narrative.status.getValue() != Narrative.NarrativeStatus.EXTENSIONS)) {
                            resource.setProperty(p.getName(), new Narrative());
                        }
                    } else {
                        visit(base);
                    }
                });
            }
        });
    }
}
