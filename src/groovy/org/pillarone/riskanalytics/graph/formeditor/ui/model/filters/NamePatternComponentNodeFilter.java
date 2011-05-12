package org.pillarone.riskanalytics.graph.formeditor.ui.model.filters;

import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.model.ComponentNode;
import org.pillarone.riskanalytics.graph.core.graph.model.Connection;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.IComponentNodeFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class NamePatternComponentNodeFilter extends AbstractComponentNodeFilter {

    private String fExpression;

    public NamePatternComponentNodeFilter(String expr) {
        if (!expr.startsWith(".*")){
            expr = ".*"+expr;
        }
        if (!expr.endsWith(".*")){
            expr = expr+".*";
        }
        fExpression = expr;
    }

    public boolean isNodeSelected(ComponentNode node) {
        String name = node.getName();
        return name.matches(fExpression);
    }
}
