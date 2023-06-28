package be.fgov.ehealth.fhir.narrative.cmd;

import org.hl7.fhir.r4.model.Resource;

public interface ResourceProcessor {
    Integer process(Resource resource) throws Exception;
}
