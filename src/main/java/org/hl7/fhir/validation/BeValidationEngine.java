package org.hl7.fhir.validation;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.r5.model.Resource;
import org.hl7.fhir.r5.utils.ToolingExtensions;
import org.hl7.fhir.utilities.TimeTracker;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BeValidationEngine extends ValidationEngine {

    private final PrintStream ps;

    public BeValidationEngine(PrintStream ps, String src, String vString, TimeTracker tt) throws FHIRException, IOException, URISyntaxException {
        super(src, vString, tt);
        this.ps = ps;
    }

    @Override
    public Resource validate(List<String> sources, List<String> profiles, List<ValidationRecord> record) throws FHIRException, IOException {
        if (profiles.size() > 0) {
            ps.println("  Profiles: " + profiles);
        }

        List<String> refs = new ArrayList<>();
        boolean asBundle = ValidatorUtils.parseSources(sources, refs, this.getContext());
        Bundle results = new Bundle();
        results.setType(Bundle.BundleType.COLLECTION);
        Iterator<String> var7 = refs.iterator();

        while(var7.hasNext()) {
            String ref = (String)var7.next();
            TimeTracker.Session tts = this.getContext().clock().start("validation");
            this.getContext().clock().milestone();
            ps.print("  Validate " + ref);
            Content cnt = this.getIgLoader().loadContent(ref, "validate", false);

            try {
                OperationOutcome outcome = this.validate(ref, cnt.focus, cnt.cntType, profiles, record);
                ToolingExtensions.addStringExtension(outcome, "http://hl7.org/fhir/StructureDefinition/operationoutcome-file", ref);
                ps.println(" " + this.getContext().clock().milestone());
                results.addEntry().setResource(outcome);
                tts.end();
            } catch (Exception var12) {
                ps.println("Validation Infrastructure fail validating " + ref + ": " + var12.getMessage());
                tts.end();
                throw new FHIRException(var12);
            }
        }

        return (Resource)(asBundle ? results : results.getEntryFirstRep().getResource());
    }
}
