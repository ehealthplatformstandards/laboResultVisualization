package be.fgov.ehealth.fhir.narrative.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.r5.model.Resource;
import org.hl7.fhir.utilities.TimeTracker;
import org.hl7.fhir.utilities.VersionUtilities;
import org.hl7.fhir.validation.ValidationEngine;
import org.hl7.fhir.validation.ValidationRecord;
import org.hl7.fhir.validation.cli.model.CliContext;
import org.hl7.fhir.validation.cli.services.HTMLOutputGenerator;
import org.hl7.fhir.validation.cli.services.ValidationService;

import java.util.ArrayList;
import java.util.List;

public class FhirValidator {

    private final ValidationService validationService = new ValidationService();
    private final CliContext cliContext;

    public FhirValidator(String source, String ig) {
        cliContext = new CliContext();
        cliContext.setSv(VersionUtilities.getCurrentPackageVersion("4.0"));
        if (StringUtils.isNotEmpty(ig)) {
            cliContext.addIg(ig);
        }
        cliContext.addSource(source);
    }

    public Pair<Boolean, String> validate() throws Exception {
        // initialize validator engine
        final List<ValidationRecord> records = new ArrayList<>();
        final TimeTracker tt = new TimeTracker();
        String definitions = VersionUtilities.packageForVersion(cliContext.getSv()) + "#" + VersionUtilities.getCurrentVersion(cliContext.getSv());
        ValidationEngine validator = validationService.initializeValidator(cliContext, definitions, tt);

        // validate
        final Resource resource = validator.validate(cliContext.getSources(), cliContext.getProfiles(), records);
        final int errors = validationService.displayOperationOutcome((OperationOutcome) resource, false, cliContext.isCrumbTrails());

        // return hasError and html
        return new ImmutablePair<>(errors != 0, toHml(records));
    }

    private String toHml(final List<ValidationRecord> records) {
        return (new HTMLOutputGenerator(records)).generate(System.currentTimeMillis());
    }
}
