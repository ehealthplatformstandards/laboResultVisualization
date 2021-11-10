package be.fgov.ehealth.fhir.narrative.utils;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r4.model.Bundle;

public final class FhirNarrativeUtils {
    private FhirNarrativeUtils() { }

    public static Bundle stripNarratives(FhirContext ctx, Bundle bundle) {
        final IParser parser = ctx.newJsonParser().setPrettyPrint(true).setSuppressNarratives(true);
        return parser.parseResource(Bundle.class, parser.encodeResourceToString(bundle));
    }
}
