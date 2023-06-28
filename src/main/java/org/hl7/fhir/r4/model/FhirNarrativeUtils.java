package org.hl7.fhir.r4.model;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public final class FhirNarrativeUtils {
    private FhirNarrativeUtils() { }

    public static Resource stripNarratives(Resource resource) {
        visitAndStrip(resource);
        return resource;
    }

    public static List<Pair<String, String>> collectNarratives(Resource resource) {
        LinkedList<Pair<String, String>> acc = new LinkedList<>();
        visitAndCollect(resource, "", acc);
        return acc;
    }

    private static void visitAndStrip(Base resource) {
        List<Property> children = new LinkedList<>();
        resource.listChildren(children);
        children.forEach(p -> {
            Base[] property = resource.getProperty(p.getName().hashCode(), p.getName(), false);
            if (property != null) {
                Arrays.asList(property).forEach(base -> {
                    if (base instanceof Narrative) {
                        Narrative narrative = ((Narrative) base);
                        if (narrative.status == null || (narrative.status.getValue() != Narrative.NarrativeStatus.ADDITIONAL && narrative.status.getValue() != Narrative.NarrativeStatus.EXTENSIONS)) {
                            resource.setProperty(p.getName(), new Narrative());
                        }
                    } else {
                        visitAndStrip(base);
                    }
                });
            }
        });
    }

    private static void visitAndCollect(Base resource, String path, List<Pair<String, String>> acc) {
        List<Property> children = new LinkedList<>();
        resource.listChildren(children);
        children.forEach(p -> {
            Base[] property = resource.getProperty(p.getName().hashCode(), p.getName(), false);
            if (property != null) {
                List<Base> bases = Arrays.asList(property);
                bases.forEach(base -> {
                    if (base instanceof Narrative) {
                        Narrative narrative = ((Narrative) base);
                        acc.add(Pair.of(path, narrative.getDiv().toString()));
                    } else {
                        visitAndCollect(base, path + "/" + p.getName() + "[" + bases.indexOf(base) + "]:" + base.getClass().getSimpleName(), acc);
                    }
                });
            }
        });
    }


}
