package be.fgov.ehealth.fhir.narrative.cmd;

import be.fgov.ehealth.fhir.narrative.option.FhirValidator;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.Resource;
import picocli.CommandLine.*;
import picocli.CommandLine.Model.CommandSpec;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.Callable;

import static java.nio.file.Files.readAllBytes;
import static org.hl7.fhir.r4.model.FhirNarrativeUtils.stripNarratives;

@Command(name = "fhirpreview", mixinStandardHelpOptions = true)
public class Visualization implements Callable<Integer> {
    @Option(names = { "-s", "--style" }, description = "Style")
    protected File css;

    @Option(names = { "-d", "--display" }, description = "Display generated document in visualizer")
    protected Boolean display = false;

    //TODO rendre mandatory l'IG
    @Option(names = { "-v", "--validate" }, arity = "1..1",
            description = {
            "Validate the resource.",
            "If absent, validation is not performed.",
            "Optionally specify a list of URLs, seperated by a comma, to implementation guides to be used for validation. (fallback: ${FALLBACK-VALUE})." },
            split = ",")
    protected String[] implementationGuideUrls;

    @Parameters(description = "Action to perform on file. May be one of ${COMPLETION-CANDIDATES}", index = "0") private Action action;
    @Parameters(index = "1") private File file;

    @Parameters(index = "2") private String profile;

    @Spec private CommandSpec spec;

    private final PrintStream output;

    public Visualization(PrintStream output) {
        this.output = output;
    }

    @Override
    public Integer call() throws Exception {
        final ParseResult pr = spec.commandLine().getParseResult();
        final boolean shouldValidate = pr.hasMatchedOption('v');

        if (shouldValidate) {
            final Pair<Integer, String> validated = new FhirValidator(output, Arrays.asList(implementationGuideUrls)).validate(file.getAbsolutePath());

            final boolean errors = validated.getLeft() != null && validated.getLeft() > 0;
            if (errors) {
                if (display) {
                    final String html = validated.getRight();
                    final File tmpFile = File.createTempFile("be.fgov.ehealth.fhir.validation", ".html");
                    try (OutputStream os = new BufferedOutputStream(new FileOutputStream(tmpFile))) {
                        os.write(html.getBytes());
                    }
                    Desktop.getDesktop().browse(tmpFile.toURI());
                }
                return 0;
            }
        }

        final FhirContext ctx = FhirContext.forR4();
        IParser parser = null;

        if (FilenameUtils.getExtension(file.getAbsolutePath()).equals("json")) {
            parser  = ctx.newJsonParser();
        } else if (FilenameUtils.getExtension(file.getAbsolutePath()).equals("xml") ) {
            parser = ctx.newXmlParser();
        }


        switch (profile){
            case "immunization":
                final Immunization immunization = (Immunization) stripNarratives(parser.parseResource(Immunization.class, new String(readAllBytes(file.toPath()), StandardCharsets.UTF_8)));
                return action.execute(this, ctx, immunization);
            case "lab":
                final Bundle lab = (Bundle) stripNarratives(parser.parseResource(Bundle.class, new String(readAllBytes(file.toPath()), StandardCharsets.UTF_8)));
                return action.execute(this, ctx, lab);
            case "allergy":
                final AllergyIntolerance allergy = (AllergyIntolerance) stripNarratives(parser.parseResource(AllergyIntolerance.class, new String(readAllBytes(file.toPath()), StandardCharsets.UTF_8)));
                return action.execute(this, ctx, allergy);
            default:
                return 0;
        }
    }

    public PrintStream getOutput() {
        return output;
    }

    public enum Action {
        html((viz, ctx, bundle) -> new HtmlPreview(viz, ctx).process(bundle)),
        embed((viz, ctx, bundle) -> new HtmlNarrativeEmbed(viz, ctx).process(bundle)),
        extract((viz, ctx, bundle) -> new HtmlNarrativeExtract(viz).process(bundle));

        private final ActionLambda executor;

        Action(ActionLambda executor) {
            this.executor = executor;
        }

        public Integer execute(Visualization visualization, FhirContext ctx, Resource resource) throws Exception {
            return executor.execute(visualization, ctx, resource);
        }

        public interface ActionLambda {
            Integer execute(Visualization visualization, FhirContext ctx,  Resource resource) throws Exception;
        }
    }
}
