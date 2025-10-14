package be.fgov.ehealth.fhir.narrative.cmd;

import be.fgov.ehealth.fhir.narrative.gen.ResourceHtmlGenerator;
import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.model.Resource;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class HtmlPreview implements ResourceProcessor {

    private final Visualization visualization;
    private final FhirContext ctx;
    private final ResourceHtmlGenerator htmlGenerator = new ResourceHtmlGenerator();

    public HtmlPreview(final Visualization visualization, final FhirContext ctx) {
        this.visualization = visualization;
        this.ctx = ctx;
    }

    @Override
    public Integer process(final Resource resource) {
        final byte[] html;
        try {
            html = htmlGenerator.generateHtmlRepresentation(ctx, resource, visualization.css != null ?
                    new String(IOUtils.toByteArray(new FileInputStream(visualization.css)), StandardCharsets.UTF_8)
                    : null);
        } catch (final IOException e) {
            throw new IllegalArgumentException("Invalid css");
        }

        visualization.getOutput().println(new String(html, StandardCharsets.UTF_8));

        if (visualization.display) {
            try {
                final File tmpFile = File.createTempFile("be.fgov.ehealth.fhir.report", ".html");
                try (final OutputStream os = new BufferedOutputStream(new FileOutputStream(tmpFile))) {
                    os.write(html);
                }
                Desktop.getDesktop().browse(tmpFile.toURI());
            } catch (final IOException e) {
                //Ignore
            }
        }
        return 0;
    }

}
