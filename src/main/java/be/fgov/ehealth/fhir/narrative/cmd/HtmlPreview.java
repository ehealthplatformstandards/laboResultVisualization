package be.fgov.ehealth.fhir.narrative.cmd;

import be.fgov.ehealth.fhir.narrative.gen.DiagnosticReportHtmlGenerator;
import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.model.Bundle;

import java.awt.Desktop;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class HtmlPreview implements BundleProcessor {
    private final Visualization visualization;
    private final FhirContext ctx;
    private final DiagnosticReportHtmlGenerator htmlGenerator = new DiagnosticReportHtmlGenerator();

    public HtmlPreview(Visualization visualization, FhirContext ctx) {
        this.visualization = visualization;
        this.ctx = ctx;
    }

    @Override
    public Integer process(Bundle bundle) {
        FhirContext fhirContext = this.ctx;

        byte[] html;
        try {
            html = htmlGenerator.generateHtmlRepresentation(fhirContext, bundle, visualization.css != null ?
                    new String(IOUtils.toByteArray(new FileInputStream(visualization.css)), StandardCharsets.UTF_8)
                    : null);
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid css");
        }

        visualization.getOutput().println(new String(html, StandardCharsets.UTF_8));

        if (visualization.display) {
            try {
                File tmpFile = File.createTempFile("be.fgov.ehealth.fhir.laboratoryreport", ".html");
                try (OutputStream os = new BufferedOutputStream(new FileOutputStream(tmpFile))) {
                    os.write(html);
                }
                Desktop.getDesktop().browse(tmpFile.toURI());
            } catch (IOException e) {
                //Ignore
            }
        }
        return 0;
    }
}
