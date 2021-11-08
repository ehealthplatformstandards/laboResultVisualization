package be.fgov.ehealth.fhir.laboratoryreport.cmd;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative.CustomThymeleafNarrativeGenerator;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r4.model.Bundle;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;

@Command(name = "Visualization", mixinStandardHelpOptions = true)
public class Visualization implements Callable<Integer> {
    @Option(names = { "-s", "--style" }, description = "Style")
    protected File css;

    @Option(names = { "-d", "--display" }, description = "Display generated document in visualizer")
    protected Boolean display = false;

    @Parameters(index = "0") private Action action;
    @Parameters(index = "1") private File bundleFile;

    private final PrintStream output;

    public Visualization(PrintStream output) {
        this.output = output;
    }

    @Override
    public Integer call() throws Exception {
        final FhirContext ctx = FhirContext.forR4();
        final IParser parser = ctx.newJsonParser().setPrettyPrint(true).setSuppressNarratives(true);

        final Bundle bundle = parser.parseResource(Bundle.class, parser.encodeResourceToString(parser.parseResource(Bundle.class, new String(readAllBytes(bundleFile.toPath()), StandardCharsets.UTF_8))));

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
