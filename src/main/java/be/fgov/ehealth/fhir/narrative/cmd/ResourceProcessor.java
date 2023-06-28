package be.fgov.ehealth.fhir.narrative.cmd;

import org.hl7.fhir.r4.model.Bundle;

public interface BundleProcessor {
    Integer process(Bundle bundle) throws Exception;
}
