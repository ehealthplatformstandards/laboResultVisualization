package be.fgov.ehealth.fhir.narrative;

import be.fgov.ehealth.fhir.narrative.cmd.Visualization;
import picocli.CommandLine;

import java.io.PrintStream;

public class App
{
    public static void main(String[] args ) {
        PrintStream out = System.out;
        System.exit(commandLine(args, out));
    }

    public static int commandLine(String[] args, PrintStream out) {
        return new CommandLine(new Visualization(out)).execute(args);
    }
}
