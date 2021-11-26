package org.hl7.fhir.validation;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r5.elementmodel.Manager;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.r5.model.Resource;
import org.hl7.fhir.r5.utils.EOperationOutcome;
import org.hl7.fhir.r5.utils.ToolingExtensions;
import org.hl7.fhir.utilities.TimeTracker;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.hl7.fhir.validation.instance.InstanceValidator;

import java.io.ByteArrayInputStream;
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

    public Resource validateContent(List<byte[]> sources, List<String> profiles, List<ValidationRecord> record) throws FHIRException, IOException {
        int idx = 0;
        boolean asBundle = sources.size() > 1;
        Bundle results = new Bundle();
        results.setType(Bundle.BundleType.COLLECTION);

        for (byte[] data: sources) {
            List<ValidationMessage> messages = new ArrayList<ValidationMessage>();
            InstanceValidator validator = getValidator();
            validator.validate(null, messages, new ByteArrayInputStream(data), Manager.FhirFormat.JSON, asSdList(profiles));
            if (record != null) {
                record.add(new ValidationRecord("--", messages));
            }
            try {
                OperationOutcome outcome =  ValidatorUtils.messagesToOutcome(messages, getContext(), getFhirPathEngine());
                ToolingExtensions.addStringExtension(outcome, "http://hl7.org/fhir/StructureDefinition/operationoutcome-file", "data-"+idx);
                results.addEntry().setResource(outcome);
            } catch (EOperationOutcome eOperationOutcome) {
                ps.println("Validation Infrastructure fail validating element " + idx + ": " + eOperationOutcome.getMessage());
                throw new FHIRException(eOperationOutcome);
            }
            idx++;
        }
        return asBundle ? results : results.getEntryFirstRep().getResource();
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

        for(String ref : refs) {
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
            } catch (Exception eOperationOutcome) {
                ps.println("Validation Infrastructure fail validating " + ref + ": " + eOperationOutcome.getMessage());
                tts.end();
                throw new FHIRException(eOperationOutcome);
            }
        }

        return (Resource)(asBundle ? results : results.getEntryFirstRep().getResource());
    }
}
