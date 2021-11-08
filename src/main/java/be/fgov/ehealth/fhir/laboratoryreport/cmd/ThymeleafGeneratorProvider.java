package be.fgov.ehealth.fhir.laboratoryreport.cmd;

import ca.uhn.fhir.narrative.CustomThymeleafNarrativeGenerator;
import org.hl7.fhir.r4.model.Bundle;

import java.io.File;

public abstract class ThymeleafGeneratorProvider {
    private final CustomThymeleafNarrativeGenerator generator;

    public ThymeleafGeneratorProvider(String propertiesFile) {
        this.generator = new CustomThymeleafNarrativeGenerator(propertiesFile);
    }

    protected CustomThymeleafNarrativeGenerator getGenerator() {
        return generator;
    }
}
