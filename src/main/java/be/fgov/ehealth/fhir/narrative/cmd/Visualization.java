package be.fgov.ehealth.fhir.narrative.cmd;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r4.model.Bundle;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.PrintStream;
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

    @Parameters(description = "Action to perform on file. May be one of ${COMPLETION-CANDIDATES}", index = "0") private Action action;
    @Parameters(index = "1") private File bundleFile;

    private final PrintStream output;

    public Visualization(PrintStream output) {
        this.output = output;
    }

    @Override
    public Integer call() throws Exception {
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
