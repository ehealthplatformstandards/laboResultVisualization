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
import java.util.Objects;

public class DiagnosticReportHtmlGenerator {
    private INarrativeGenerator generator;

    public DiagnosticReportHtmlGenerator() {
        this.generator = new CustomThymeleafNarrativeGenerator("classpath:/narratives/diagnosticreport/narrative.properties");
    }

    public DiagnosticReportHtmlGenerator(INarrativeGenerator generator) {
        this.generator = generator;
    }

    public byte[] generateHtmlRepresentation(FhirContext fhirContext, Resource resource, String css) {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        System.out.println("gen dans rep: " + fhirContext.getNarrativeGenerator().toString());
        try(OutputStreamWriter sw = new OutputStreamWriter(bos, StandardCharsets.UTF_8)) {
            sw.append("<html><head><style>");
            sw.append(css != null ? css :
                    new String(IOUtils.toByteArray(Objects.requireNonNull(this.getClass().getResourceAsStream("/narratives/diagnosticreport/narrative.css"))), StandardCharsets.UTF_8)
            );
            sw.append("</style></head><body>\r\n");
            sw.append(generateDivRepresentation(fhirContext, resource, css));
            sw.append("</body></html>\r\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bos.toByteArray();
    }

    public String generateDivRepresentation(FhirContext fhirContext, Resource resource, String css) {
        final IParser parser = fhirContext.newJsonParser().setPrettyPrint(true).setSuppressNarratives(false);

        switch (resource.getResourceType().toString()) {
            case "Bundle":
                generator = new CustomThymeleafNarrativeGenerator("classpath:/narratives/diagnosticreport/narrative.properties");
                fhirContext.setNarrativeGenerator(this.generator);
                final Bundle bundleParse = parser.parseResource(Bundle.class, parser.encodeResourceToString(FhirNarrativeUtils.stripNarratives(resource)));

                return ((DiagnosticReport) bundleParse.getEntry().stream()
                        .filter(e -> e.getResource().fhirType().equals("DiagnosticReport")).findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Bundle must contain a DiagnosticReport"))
                        .getResource()).getText().getDiv().toString();

            case "Immunization":
                generator = new CustomThymeleafNarrativeGenerator("classpath:/narratives/immunization/narrative.properties");
                fhirContext.setNarrativeGenerator(this.generator);
                final Immunization immunizationParse = parser.parseResource(Immunization.class, parser.encodeResourceToString(FhirNarrativeUtils.stripNarratives(resource)));

                return immunizationParse.getText().getDiv().toString();

            case "AllergyIntolerance":
                generator = new CustomThymeleafNarrativeGenerator("classpath:/narratives/allergy/narrative.properties");
                fhirContext.setNarrativeGenerator(this.generator);
                final AllergyIntolerance allergyParse = parser.parseResource(AllergyIntolerance.class, parser.encodeResourceToString(FhirNarrativeUtils.stripNarratives(resource)));

                return allergyParse.getText().getDiv().toString();

            default:
                //
                return "";
        }


    }

    public String generateDivRepresentation2(FhirContext fhirContext, Resource resource, String css) {
        //final IParser parser = fhirContext.newJsonParser().setPrettyPrint(true).setSuppressNarratives(false);
        final IParser parser = fhirContext.newXmlParser().setPrettyPrint(true).setSuppressNarratives(false);
        //fhirContext.setNarrativeGenerator(this.generator);


        Bundle bundle = (Bundle) resource;
        System.out.println(bundle.getEntry().size());

        for (Bundle.BundleEntryComponent bundleEntryComponent : bundle.getEntry()){
            System.out.println(bundleEntryComponent.getResource().getResourceType() + " " + bundleEntryComponent.getFullUrl());

            if (bundleEntryComponent.getResource().getResourceType().toString() == "DiagnosticReport") {
                DiagnosticReport diagnosticReport = (DiagnosticReport) bundleEntryComponent.getResource();
                System.out.println("String: " + diagnosticReport.getText().getDiv().toString());
                System.out.println("concl: " + diagnosticReport.getId());
            }
        }

        //There must be a better way to do this
        //final Resource reparsed = parser.parseResource(Bundle.class, parser.encodeResourceToString(FhirNarrativeUtils.stripNarratives(bundle)));

        /*return ((DiagnosticReport) reparsed.getEntry().stream()
                    .filter(e -> e.getResource().fhirType().equals("DiagnosticReport")).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Bundle must contain a DiagnosticReport"))
                    .getResource()).getText().getDiv().toString();*/

        //generator = new CustomThymeleafNarrativeGenerator("classpath:/narratives/diagnosticreport/narrative.properties");
        fhirContext.setNarrativeGenerator(this.generator);
        //System.out.println("gen dans div2: " + fhirContext.getNarrativeGenerator());
        final Bundle bundleParse = parser.parseResource(Bundle.class, parser.encodeResourceToString(FhirNarrativeUtils.stripNarratives(resource)));

        //System.out.println(FhirNarrativeUtils.stripNarratives(resource).getEntry().size());
        System.out.println(bundleParse.getEntry().size());
        for (Bundle.BundleEntryComponent bundleEntryComponent : bundleParse.getEntry()){
            System.out.println(bundleEntryComponent.getResource().getResourceType() + " " + bundleEntryComponent.getFullUrl());

            if (bundleEntryComponent.getResource().getResourceType().toString() == "DiagnosticReport") {
                DiagnosticReport diagnosticReport = (DiagnosticReport) bundleEntryComponent.getResource();
                System.out.println("String: " + diagnosticReport.getText().getDiv().toString());
                System.out.println("concl: " + diagnosticReport.getId());
            }
        }

        /*try {
            bundleParse.getEntry().stream();
            ((DiagnosticReport) bundleParse.getEntry().stream()
                    .filter(e -> e.getResource().fhirType().equals("DiagnosticReport")).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Bundle must contain a DiagnosticReport"))
                    .getResource()).getText().getDiv().toString();
        } catch (IllegalArgumentException e) {
            System.out.println(e);
        }*/

        System.out.println("return: " + ((DiagnosticReport) bundleParse.getEntry().stream()
                .filter(e -> e.getResource().fhirType().equals("DiagnosticReport")).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Bundle must contain a DiagnosticReport"))
                .getResource()).getText().getDiv().toString());

        return ((DiagnosticReport) bundleParse.getEntry().stream()
                .filter(e -> e.getResource().fhirType().equals("DiagnosticReport")).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Bundle must contain a DiagnosticReport"))
                .getResource()).getText().getDiv().toString();
    }

}
