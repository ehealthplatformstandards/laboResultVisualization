package org.hl7.fhir.validation;

import java.io.PrintStream;

import org.hl7.fhir.r5.context.TerminologyCache;
import org.hl7.fhir.r5.model.FhirPublication;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.r5.utils.ToolingExtensions;
import org.hl7.fhir.utilities.TimeTracker;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.hl7.fhir.validation.cli.model.CliContext;
import org.hl7.fhir.validation.cli.services.StandAloneValidatorFetcher;
import org.hl7.fhir.validation.cli.services.ValidationService;

public class BeValidationService extends ValidationService {
    private final PrintStream ps;

    public BeValidationService(PrintStream ps) {
        super();
        this.ps = ps;
    }

    @Override
    public BeValidationEngine initializeValidator(CliContext cliContext, String definitions, TimeTracker tt) throws Exception {
        tt.milestone();

        ps.print("  Load FHIR v" + cliContext.getSv() + " from " + definitions);
        BeValidationEngine validator = new BeValidationEngine(ps, definitions, cliContext.getSv(), tt);
        FhirPublication ver = FhirPublication.fromCode(cliContext.getSv());
        IgLoader igLoader = new IgLoader(validator.getPcm(), validator.getContext(), validator.getVersion(), validator.isDebug());
        ps.println(" - " + validator.getContext().countAllCaches() + " resources (" + tt.milestone() + ")");
        igLoader.loadIg(validator.getIgs(), validator.getBinaries(), "hl7.terminology", false);
        ps.print("  Terminology server " + cliContext.getTxServer());
        String txver = validator.setTerminologyServer(cliContext.getTxServer(), cliContext.getTxLog(), ver);
        ps.println(" - Version " + txver + " (" + tt.milestone() + ")");
        validator.setDebug(cliContext.isDoDebug());

        for(String src: cliContext.getIgs()) {
            igLoader.loadIg(validator.getIgs(), validator.getBinaries(), src, cliContext.isRecursive());
        }

        ps.print("  Get set... ");
        validator.setQuestionnaireMode(cliContext.getQuestionnaireMode());
        validator.setDoNative(cliContext.isDoNative());
        validator.setHintAboutNonMustSupport(cliContext.isHintAboutNonMustSupport());
        validator.setAnyExtensionsAllowed(cliContext.isAnyExtensionsAllowed());
        validator.setLanguage(cliContext.getLang());
        validator.setLocale(cliContext.getLocale());
        validator.setSnomedExtension(cliContext.getSnomedCTCode());
        validator.setAssumeValidRestReferences(cliContext.isAssumeValidRestReferences());
        validator.setNoExtensibleBindingMessages(cliContext.isNoExtensibleBindingMessages());
        validator.setNoInvariantChecks(cliContext.isNoInvariants());
        validator.setWantInvariantInMessage(cliContext.isWantInvariantsInMessages());
        validator.setSecurityChecks(cliContext.isSecurityChecks());
        validator.setCrumbTrails(cliContext.isCrumbTrails());
        validator.setShowTimes(cliContext.isShowTimes());
        validator.setAllowExampleUrls(cliContext.isAllowExampleUrls());
        StandAloneValidatorFetcher fetcher = new StandAloneValidatorFetcher(validator.getPcm(), validator.getContext(), validator);
        validator.setFetcher(fetcher);
        validator.getContext().setLocator(fetcher);
        validator.getBundleValidationRules().addAll(cliContext.getBundleValidationRules());
        TerminologyCache.setNoCaching(cliContext.isNoInternalCaching());
        validator.prepare();
        ps.println(" go (" + tt.milestone() + ")");

        return validator;
    }

    /**
     * Processes the content of an OperationOutcome and prints a summary.
     *
     * @param operationOutcome Outcome of a call to the validator service
     * @param hasMultiples     set to true if multiple resources were validated (as a Bundle)
     * @param crumbs           Include slice info text
     * @return amount of issues that have severity FATAL or ERROR
     */
    @Override
    public int displayOperationOutcome(OperationOutcome operationOutcome, boolean hasMultiples,
        boolean crumbs) {
        int error = 0;
        int warn = 0;
        int info = 0;
        String file = ToolingExtensions.readStringExtension(operationOutcome,
            ToolingExtensions.EXT_OO_FILE);

        for (OperationOutcome.OperationOutcomeIssueComponent issue : operationOutcome.getIssue()) {
            if (issue.getSeverity() == OperationOutcome.IssueSeverity.FATAL ||
                issue.getSeverity() == OperationOutcome.IssueSeverity.ERROR) {
                error++;
            } else if (issue.getSeverity() == OperationOutcome.IssueSeverity.WARNING) {
                warn++;
            } else {
                info++;
            }
        }

        if (hasMultiples) {
            ps.print("-- ");
            ps.print(file);
            ps.print(" --");
            ps.println(Utilities.padLeft("", '-', Integer.max(38, file.length() + 6)));
        }
        ps.println((error == 0 ? "Success" : "*FAILURE*") + ": " + error + " " +
                   "errors, " + warn + " warnings, " + info + " notes");
        for (OperationOutcome.OperationOutcomeIssueComponent issue : operationOutcome.getIssue()) {
            ps.println(getIssueSummary(issue));
            if (crumbs) {
                ValidationMessage vm = (ValidationMessage) issue.getUserData("source.msg");
                if (vm != null && vm.sliceText != null) {
                    for (String s : vm.sliceText) {
                        ps.println("    slice info: " + s);
                    }
                }
            }
        }
        if (hasMultiples) {
            ps.print("---");
            ps.print(Utilities.padLeft("", '-', file.length()));
            ps.print("---");
            ps.println(Utilities.padLeft("", '-', Integer.max(38, file.length() + 6)));
            ps.println();
        }
        return error;
    }

    private String getIssueSummary(OperationOutcome.OperationOutcomeIssueComponent issue) {
        String loc;
        int line;
        int col;
        if (issue.hasExpression()) {
            line = ToolingExtensions.readIntegerExtension(issue,
                "http://hl7.org/fhir/StructureDefinition/operationoutcome-issue-line", -1);
            col = ToolingExtensions.readIntegerExtension(issue,
                "http://hl7.org/fhir/StructureDefinition/operationoutcome-issue-col", -1);
            loc = (issue.getExpression().get(0)).asStringValue() +
                  (line >= 0 && col >= 0 ? " (line " +
                                           line + ", col" +
                                           col + ")" : "");
        } else if (issue.hasLocation()) {
            loc = (issue.getLocation().get(0)).asStringValue();
        } else {
            line = ToolingExtensions.readIntegerExtension(issue,
                "http://hl7.org/fhir/StructureDefinition/operationoutcome-issue-line", -1);
            col = ToolingExtensions.readIntegerExtension(issue,
                "http://hl7.org/fhir/StructureDefinition/operationoutcome-issue-col", -1);
            loc = line >= 0 && col >= 0 ? "line " + line + ", col" + col : "??";
        }

        return "  " + issue.getSeverity().getDisplay() + " @ " + loc + " : " +
               issue.getDetails().getText();
    }

}
