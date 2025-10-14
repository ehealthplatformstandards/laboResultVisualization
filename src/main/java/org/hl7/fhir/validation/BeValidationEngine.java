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
import java.util.ArrayList;
import java.util.List;

public class BeValidationEngine extends ValidationEngine {

    private final PrintStream ps;

    public BeValidationEngine(final PrintStream ps, final ValidationEngine ve) throws FHIRException, IOException {
        super(ve);
        this.ps = ps;
    }

    public Resource validateContent(final List<byte[]> sources, final List<String> profiles, final List<ValidationRecord> record) throws FHIRException, IOException {
        int idx = 0;
        final boolean asBundle = sources.size() > 1;
        final Bundle results = new Bundle();
        results.setType(Bundle.BundleType.COLLECTION);

        for (final byte[] data : sources) {
            final List<ValidationMessage> messages = new ArrayList<ValidationMessage>();

            final String mimeType = new String(data, 0, 1);

            Manager.FhirFormat fhirFormat = Manager.FhirFormat.JSON;
            if (mimeType.equals("<")) {
                fhirFormat = Manager.FhirFormat.XML;
            }
            final InstanceValidator validator = getValidator(fhirFormat);
            validator.validate(null, messages, new ByteArrayInputStream(data), fhirFormat, asSdList(profiles));
            if (record != null) {
                record.add(new ValidationRecord("--", messages));
            }
            try {
                final OperationOutcome outcome = ValidatorUtils.messagesToOutcome(messages, getContext(), getFhirPathEngine());
                ToolingExtensions.addStringExtension(outcome, "http://hl7.org/fhir/StructureDefinition/operationoutcome-file", "data-" + idx);
                results.addEntry().setResource(outcome);
            } catch (final EOperationOutcome eOperationOutcome) {
                ps.println("Validation Infrastructure fail validating element " + idx + ": " + eOperationOutcome.getMessage());
                throw new FHIRException(eOperationOutcome);
            }
            idx++;
        }
        return asBundle ? results : results.getEntryFirstRep().getResource();
    }

    @Override
    public Resource validate(final List<String> sources, final List<String> profiles, List<ValidatorUtils.SourceFile> refs, final List<ValidationRecord> record, final IValidationEngineLoader loader, final boolean all, final int delay, final boolean first) throws FHIRException, IOException {

        if (!profiles.isEmpty()) {
            ps.println("  Profiles: " + profiles);
        }

        refs = new ArrayList<>();
        final boolean asBundle = ValidatorUtils.parseSources(sources, refs, this.getContext());
        final Bundle results = new Bundle();
        results.setType(Bundle.BundleType.COLLECTION);

        for (final ValidatorUtils.SourceFile ref : refs) {
            final TimeTracker.Session tts = this.getContext().clock().start("validation");
            this.getContext().clock().milestone();
            ps.print("  Validate " + ref);
            final Content cnt = this.getIgLoader().loadContent(ref.getRef(), "validate", false, false);

            try {
                final OperationOutcome outcome = this.validate(ref.getRef(), cnt.getFocus(), cnt.getCntType(), profiles, record);
                ToolingExtensions.addStringExtension(outcome, "http://hl7.org/fhir/StructureDefinition/operationoutcome-file", ref.getRef());
                ps.println(" " + this.getContext().clock().milestone());
                results.addEntry().setResource(outcome);
                tts.end();
            } catch (final Exception eOperationOutcome) {
                ps.println("Validation Infrastructure fail validating " + ref + ": " + eOperationOutcome.getMessage());
                tts.end();
                throw new FHIRException(eOperationOutcome);
            }
        }

        return asBundle ? results : results.getEntryFirstRep().getResource();
    }
}
