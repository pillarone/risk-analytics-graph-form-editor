package org.pillarone.riskanalytics.graph.formeditor.util;

import org.pillarone.riskanalytics.graph.core.palette.model.ComponentDefinition;
import org.pillarone.riskanalytics.graph.core.palette.service.PaletteService;

import java.util.ArrayList;
import java.util.List;

public class PaletteUtilities {

    private static List<String> EXCLUDEFILTERS = new ArrayList<String>();
    private static List<String> MATCHFILTERS = new ArrayList<String>();

    static {
        /*	registerComponentDefinitionFilter("org.pillarone.riskanalytics", true);
          registerComponentDefinitionFilter("generators", true);
          registerComponentDefinitionFilter("reinsurance.contracts", true);
          registerComponentDefinitionFilter("underwriting", true);
          registerComponentDefinitionFilter("examples", false);
          registerComponentDefinitionFilter("Example", false);*/
    }

    public static void registerComponentDefinitionFilter(String str, boolean match) {
        if (match) {
            MATCHFILTERS.add(str);
        } else {
            EXCLUDEFILTERS.add(str);
        }
    }

    public static void removeComponentDefinitionFilter(String str, boolean match) {
        if (match) {
            if (MATCHFILTERS.contains(str)) {
                MATCHFILTERS.remove(str);
            }
        } else {
            if (EXCLUDEFILTERS.contains(str)) {
                EXCLUDEFILTERS.remove(str);
            }
        }
    }

    public static String printComponentFilters() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Constraints on the type names to be included:\n");
        buffer.append("One of the following substrings should be contained:\n");
        for (String f : MATCHFILTERS) {
            buffer.append(f + "\n");
        }
        buffer.append("None of the following substrings should be contained:\n");
        for (String f : MATCHFILTERS) {
            buffer.append(f + "\n");
        }
        return buffer.toString();
    }

    public static List<String> getAvailableComponentNames(boolean useFilters) {
        List<ComponentDefinition> availableTypes = PaletteService.getInstance().getAllComponentDefinitions();
        List<String> selectedTypes = new ArrayList<String>();
        for (ComponentDefinition d : availableTypes) {
            String name = d.toString();
            boolean match = true;
            if (useFilters) {
                for (String s : EXCLUDEFILTERS) {
                    if (name.indexOf(s) > 0) {
                        match = false;
                    }
                }
                if (match && MATCHFILTERS.size() > 0) {
                    boolean match2 = false;
                    for (String s : MATCHFILTERS) {
                        if (name.indexOf(s) > 0) {
                            match2 = true;
                        }
                    }
                    match = match && match2;
                }
            }
            if (match) {
                selectedTypes.add(d.toString());
            }
        }
        return selectedTypes;
    }
}
