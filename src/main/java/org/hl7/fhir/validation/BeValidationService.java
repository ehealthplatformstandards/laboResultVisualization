package org.hl7.fhir.validation;

import org.hl7.fhir.r5.context.TerminologyCache;
import org.hl7.fhir.r5.model.FhirPublication;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.r5.model.StringType;
import org.hl7.fhir.r5.utils.ToolingExtensions;
import org.hl7.fhir.utilities.TimeTracker;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.hl7.fhir.validation.cli.model.CliContext;
import org.hl7.fhir.validation.cli.services.StandAloneValidatorFetcher;
import org.hl7.fhir.validation.cli.services.ValidationService;

import java.io.PrintStream;
import java.util.Iterator;

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
        Iterator var9 = cliContext.getIgs().iterator();

        while(var9.hasNext()) {
            String src = (String)var9.next();
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

    @Override
    public int displayOperationOutcome(OperationOutcome oo, boolean hasMultiples, boolean crumbs) {
        int error = 0;
        int warn = 0;
        int info = 0;
        String file = ToolingExtensions.readStringExtension(oo, "http://hl7.org/fhir/StructureDefinition/operationoutcome-file");
        Iterator var8 = oo.getIssue().iterator();

        while(true) {
            OperationOutcome.OperationOutcomeIssueComponent issue;
            while(var8.hasNext()) {
                issue = (OperationOutcome.OperationOutcomeIssueComponent)var8.next();
                if (issue.getSeverity() != OperationOutcome.IssueSeverity.FATAL && issue.getSeverity() != OperationOutcome.IssueSeverity.ERROR) {
                    if (issue.getSeverity() == OperationOutcome.IssueSeverity.WARNING) {
                        ++warn;
                    } else {
                        ++info;
                    }
                } else {
                    ++error;
                }
            }

            if (hasMultiples) {
                ps.print("-- ");
                ps.print(file);
                ps.print(" --");
                ps.println(Utilities.padLeft("", '-', Integer.max(38, file.length() + 6)));
            }

            ps.println((error == 0 ? "Success" : "*FAILURE*") + ": " + Integer.toString(error) + " errors, " + Integer.toString(warn) + " warnings, " + Integer.toString(info) + " notes");
            var8 = oo.getIssue().iterator();

            while(true) {
                ValidationMessage vm;
                do {
                    do {
                        do {
                            if (!var8.hasNext()) {
                                if (hasMultiples) {
                                    ps.print("---");
                                    ps.print(Utilities.padLeft("", '-', file.length()));
                                    ps.print("---");
                                    ps.println(Utilities.padLeft("", '-', Integer.max(38, file.length() + 6)));
                                    ps.println();
                                }

                                return error;
                            }

                            issue = (OperationOutcome.OperationOutcomeIssueComponent)var8.next();
                            ps.println(this.getIssueSummary(issue));
                        } while(!crumbs);

                        vm = (ValidationMessage)issue.getUserData("source.msg");
                    } while(vm == null);
                } while(vm.sliceText == null);

                String[] var11 = vm.sliceText;
                int var12 = var11.length;

                for(int var13 = 0; var13 < var12; ++var13) {
                    String s = var11[var13];
                    ps.println("    slice info: " + s);
                }
            }
        }
    }

    private String getIssueSummary(OperationOutcome.OperationOutcomeIssueComponent issue) {
        String loc;
        int line;
        int col;
        if (issue.hasExpression()) {
            line = ToolingExtensions.readIntegerExtension(issue, "http://hl7.org/fhir/StructureDefinition/operationoutcome-issue-line", -1);
            col = ToolingExtensions.readIntegerExtension(issue, "http://hl7.org/fhir/StructureDefinition/operationoutcome-issue-col", -1);
            loc = ((StringType)issue.getExpression().get(0)).asStringValue() + (line >= 0 && col >= 0 ? " (line " + Integer.toString(line) + ", col" + Integer.toString(col) + ")" : "");
        } else if (issue.hasLocation()) {
            loc = ((StringType)issue.getLocation().get(0)).asStringValue();
        } else {
            line = ToolingExtensions.readIntegerExtension(issue, "http://hl7.org/fhir/StructureDefinition/operationoutcome-issue-line", -1);
            col = ToolingExtensions.readIntegerExtension(issue, "http://hl7.org/fhir/StructureDefinition/operationoutcome-issue-col", -1);
            loc = line >= 0 && col >= 0 ? "line " + Integer.toString(line) + ", col" + Integer.toString(col) : "??";
        }

        return "  " + issue.getSeverity().getDisplay() + " @ " + loc + " : " + issue.getDetails().getText();
    }
}
