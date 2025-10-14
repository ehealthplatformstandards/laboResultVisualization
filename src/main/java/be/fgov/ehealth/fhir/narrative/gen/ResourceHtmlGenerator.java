package be.fgov.ehealth.fhir.narrative.gen;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative.CustomThymeleafNarrativeGenerator;
import ca.uhn.fhir.narrative.INarrativeGenerator;
import ca.uhn.fhir.parser.IParser;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.model.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

public class ResourceHtmlGenerator {

    private INarrativeGenerator generator;

    public ResourceHtmlGenerator() {
        //this.generator = new CustomThymeleafNarrativeGenerator("classpath:/narratives/diagnosticreport/narrative.properties");
    }

    public ResourceHtmlGenerator(final INarrativeGenerator generator) {
        this.generator = generator;
    }

    public static StringBuilder getDiagnosticNarrative(final Bundle bundleParse, final StringBuilder narrative) {
        return narrative.append(((DiagnosticReport) bundleParse.getEntry().stream()
                .filter(e -> e.getResource().fhirType().equals("DiagnosticReport")).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Bundle must contain a DiagnosticReport"))
                .getResource()).getText().getDiv().toString()).append("<br/>");
    }

    //TODO change for other messages
    public byte[] generateHtmlRepresentation(final FhirContext fhirContext, final Resource resource, final String css) {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (final OutputStreamWriter sw = new OutputStreamWriter(bos, StandardCharsets.UTF_8)) {
            sw.append("<html><head><style>");
            sw.append(css != null ? css :
                    new String(IOUtils.toByteArray(Objects.requireNonNull(this.getClass().getResourceAsStream("/narratives/diagnosticreport/narrative.css"))), StandardCharsets.UTF_8)
            );
            sw.append("</style></head><body>\r\n");
            sw.append(generateDivRepresentation(fhirContext, resource, css));
            sw.append("</body></html>\r\n");
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return bos.toByteArray();
    }

    public String generateDivRepresentation(final FhirContext fhirContext, final Resource resource, final String css) {
        final IParser parser;

        switch (resource.getResourceType().toString()) {
            case "Bundle":
                Bundle bundle = (Bundle) resource;

                final List<CanonicalType> profiles = bundle.getMeta().getProfile();
                final String mainProfile = profiles.isEmpty() ? "" : profiles.get(0).getValue();

                StringBuilder narrative = new StringBuilder();
                generator = new CustomThymeleafNarrativeGenerator("classpath:/narratives/diagnosticreport/narrative.properties");
                fhirContext.setNarrativeGenerator(this.generator);
                parser = fhirContext.newJsonParser().setPrettyPrint(true).setSuppressNarratives(false);

                Bundle bundleParse;
                if ("Bundle".equals(bundle.getEntryFirstRep().getResource().getResourceType().toString())) {
                    do {
                        for (final Bundle.BundleEntryComponent bundleEntryComponent : bundle.getEntry()) {
                            bundleParse = parser.parseResource(Bundle.class, parser.encodeResourceToString(FhirNarrativeUtils.stripNarratives(bundleEntryComponent.getResource())));
                            narrative = getDiagnosticNarrative(bundleParse, narrative);
                        }
                        bundle = (Bundle) bundle.getEntryFirstRep().getResource();
                    }
                    while ("Bundle".equals(bundle.getEntryFirstRep().getResource().getResourceType().toString()));
                    return narrative.toString();
                } else {
                    try {

                        switch (mainProfile) {
                            case "https://www.ehealth.fgov.be/standards/fhir/pss/StructureDefinition/PSSResponseBundle":
                            case "https://www.ehealth.fgov.be/standards/fhir/pss/StructureDefinition/PSSRequestBundle":
                                //No visualization at moment
                                return "";
                            default:
                                bundleParse = parser.parseResource(Bundle.class, parser.encodeResourceToString(FhirNarrativeUtils.stripNarratives(resource)));
                                return getDiagnosticNarrative(bundleParse, narrative).toString();
                        }
                    } catch (final Exception e) {
                        System.out.println("Error while parsing Bundle: " + e);
                        return "";
                    }
                }

            case "Immunization":
                generator = new CustomThymeleafNarrativeGenerator("classpath:/narratives/immunization/narrative.properties");
                fhirContext.setNarrativeGenerator(this.generator);
                parser = fhirContext.newJsonParser().setPrettyPrint(true).setSuppressNarratives(false);
                final Immunization immunizationParse = parser.parseResource(Immunization.class, parser.encodeResourceToString(FhirNarrativeUtils.stripNarratives(resource)));

                return immunizationParse.getText().getDiv().toString();

            case "AllergyIntolerance":
                generator = new CustomThymeleafNarrativeGenerator("classpath:/narratives/allergy/narrative.properties");
                fhirContext.setNarrativeGenerator(this.generator);
                parser = fhirContext.newJsonParser().setPrettyPrint(true).setSuppressNarratives(false);
                final AllergyIntolerance allergyParse = parser.parseResource(AllergyIntolerance.class, parser.encodeResourceToString(FhirNarrativeUtils.stripNarratives(resource)));

                return allergyParse.getText().getDiv().toString();

            case "ServiceRequest":
                generator = new CustomThymeleafNarrativeGenerator("classpath:/narratives/servicerequest/narrative.properties");
                fhirContext.setNarrativeGenerator(this.generator);
                parser = fhirContext.newJsonParser().setPrettyPrint(true).setSuppressNarratives(false);
                final ServiceRequest serviceRequestParse = parser.parseResource(ServiceRequest.class, parser.encodeResourceToString(FhirNarrativeUtils.stripNarratives(resource)));
                return serviceRequestParse.getText().getDiv().toString();

            case "Task":
                generator = new CustomThymeleafNarrativeGenerator("classpath:/narratives/task/narrative.properties");
                fhirContext.setNarrativeGenerator(this.generator);
                parser = fhirContext.newJsonParser().setPrettyPrint(true).setSuppressNarratives(false);
                final Task taskParse = parser.parseResource(Task.class, parser.encodeResourceToString(FhirNarrativeUtils.stripNarratives(resource)));
                return taskParse.getText().getDiv().toString();

            default:
                //
                return "";
        }


    }

}
