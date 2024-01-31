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
import java.util.List;

import static java.util.Collections.singletonList;

public class FhirValidator {

    private final BeValidationService validationService;
    private final CliContext cliContext;
    private final BeValidationEngine validator;
    private final TimeTracker tt = new TimeTracker();

    public FhirValidator(final PrintStream ps, final List<String> implementationGuideUrls) throws Exception {
        this.validationService = new BeValidationService(ps);
        cliContext = new CliContext();
        cliContext.setSv(VersionUtilities.getCurrentPackageVersion("4.0"));
        cliContext.setIgs(implementationGuideUrls);
        cliContext.setDisplayWarnings(true);
        cliContext.setAllowExampleUrls(true);

        final String definitions = VersionUtilities.packageForVersion(cliContext.getSv()) + "#" + VersionUtilities.getCurrentVersion(cliContext.getSv());
        validator = validationService.initializeValidator(cliContext, definitions, tt);
    }

    public Pair<Integer, String> validate(final String source) throws Exception {
        // add source
        cliContext.setSources(singletonList(source));

        // validate
        final List<ValidationRecord> records = new ArrayList<>();
        final Resource resource = validator.validate(cliContext.getSources(), cliContext.getProfiles(), null, records,null,false,0,false);
        final int errors = validationService.displayOperationOutcome((OperationOutcome) resource, false, cliContext.isCrumbTrails());

        // return hasErrors and html
        return Pair.of(errors, toHml(records));
    }

    public Pair<Integer, String> validate(final byte[] data) throws Exception {
        // validate
        final List<ValidationRecord> records = new ArrayList<>();
        final Resource resource = validator.validateContent(singletonList(data), cliContext.getProfiles(), records);
        final int errors = validationService.displayOperationOutcome((OperationOutcome) resource, false, cliContext.isCrumbTrails());

        // return hasErrors and html
        return Pair.of(errors, toHml(records));
    }

    private String toHml(final List<ValidationRecord> records) {
        return (new HTMLOutputGenerator(records)).generate(System.currentTimeMillis());
    }
}
