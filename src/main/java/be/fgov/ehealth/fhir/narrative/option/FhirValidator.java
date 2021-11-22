package be.fgov.ehealth.fhir.narrative.option;

import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.r5.model.Resource;
import org.hl7.fhir.utilities.TimeTracker;
import org.hl7.fhir.utilities.VersionUtilities;
import org.hl7.fhir.validation.BeValidationEngine;
import org.hl7.fhir.validation.BeValidationService;
import org.hl7.fhir.validation.ValidationRecord;
import org.hl7.fhir.validation.cli.model.CliContext;
import org.hl7.fhir.validation.cli.services.HTMLOutputGenerator;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FhirValidator {

    private final BeValidationService validationService;
    private final CliContext cliContext;

    public FhirValidator(final PrintStream ps, final List<String> igs) throws Exception {
        this.validationService = new BeValidationService(ps);
        cliContext = new CliContext();
        cliContext.setSv(VersionUtilities.getCurrentPackageVersion("4.0"));
        cliContext.setIgs(igs);
    }

    public Pair<Boolean, String> validate(final String source) throws Exception {
        // initialize validator engine
        final TimeTracker tt = new TimeTracker();
        final String definitions = VersionUtilities.packageForVersion(cliContext.getSv()) + "#" + VersionUtilities.getCurrentVersion(cliContext.getSv());
        final BeValidationEngine validator = validationService.initializeValidator(cliContext, definitions, tt);

        // add source
        cliContext.setSources(Arrays.asList(source));

        // validate
        final List<ValidationRecord> records = new ArrayList<>();
        final Resource resource = validator.validate(cliContext.getSources(), cliContext.getProfiles(), records);
        final int errors = validationService.displayOperationOutcome((OperationOutcome) resource, false, cliContext.isCrumbTrails());

        // return hasErrors and html
        return Pair.of(errors != 0, toHml(records));
    }

    private String toHml(final List<ValidationRecord> records) {
        return (new HTMLOutputGenerator(records)).generate(System.currentTimeMillis());
    }
}
