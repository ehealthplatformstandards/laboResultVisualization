package be.fgov.ehealth.fhir.narrative.cmd;

import be.fgov.ehealth.fhir.narrative.utils.FhirValidator;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.Bundle;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import static be.fgov.ehealth.fhir.narrative.utils.FhirNarrativeUtils.stripNarratives;
import static java.nio.file.Files.readAllBytes;

@Command(name = "fhirpreview", mixinStandardHelpOptions = true)
public class Visualization implements Callable<Integer> {
    @Option(names = { "-s", "--style" }, description = "Style")
    protected File css;

    @Option(names = { "-d", "--display" }, description = "Display generated document in visualizer")
    protected Boolean display = false;

    @Option(names = { "-v", "--validate" }, arity = "0..1", fallbackValue = "https://build.fhir.org/ig/hl7-be/hl7-be-fhir-laboratory-report",
            description = {
            "Validate the resource.",
            "If absent, validation is not performed.",
            "Optionally specify a URL to an implementation guide to be used for validation. (fallback: ${FALLBACK-VALUE})." })
    protected String validate;

    @Parameters(description = "Action to perform on file. May be one of ${COMPLETION-CANDIDATES}", index = "0") private Action action;
    @Parameters(index = "1") private File bundleFile;

    private final PrintStream output;

    public Visualization(PrintStream output) {
        this.output = output;
    }

    @Override
    public Integer call() throws Exception {
        if (!StringUtils.isEmpty(validate)) {
            final Pair<Boolean, String> validated = new FhirValidator(output, bundleFile.getAbsolutePath(), validate).validate();
            final Boolean errors = validated.getLeft();
            if (errors) {
                if (display) {
                    String html = validated.getRight();
                    final File tmpFile = File.createTempFile("be.fgov.ehealth.fhir.laboratoryreport.validation", ".html");
                    try (OutputStream os = new BufferedOutputStream(new FileOutputStream(tmpFile))) {
                        os.write(html.getBytes());
                    }
                    Desktop.getDesktop().browse(tmpFile.toURI());
                }
                return 0;
            }
        }

        final FhirContext ctx = FhirContext.forR4();
        final IParser parser = ctx.newJsonParser();
        final Bundle bundle = stripNarratives(ctx, parser.parseResource(Bundle.class, new String(readAllBytes(bundleFile.toPath()), StandardCharsets.UTF_8)));

        return action.execute(this, ctx, bundle);
    }

    public PrintStream getOutput() {
        return output;
    }

    public enum Action {
        html((viz, ctx, bundle) -> new HtmlPreview(viz, ctx).process(bundle)),
        embed((viz, ctx, bundle) -> new HtmlNarrativeEmbed(viz, ctx).process(bundle));

        private final ActionLambda executor;

        Action(ActionLambda executor) {
            this.executor = executor;
        }

        public Integer execute(Visualization visualization, FhirContext ctx, Bundle bundle) throws Exception {
            return executor.execute(visualization, ctx, bundle);
        }

        public interface ActionLambda {
            Integer execute(Visualization visualization, FhirContext ctx,  Bundle bundle) throws Exception;
        }
    }
}
