package org.pillarone.riskanalytics.graph.formeditor.ui.view

import org.pillarone.riskanalytics.graph.formeditor.ui.model.palette.TypeTreeNode
import com.canoo.ulc.graph.shared.ShapeTemplate

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */
class NameFilter implements INameFilter {

    private String regex

    public NameFilter(String name) {
        regex = getRegex(name ? name.toLowerCase() : "")
    }

    public boolean accept(TypeTreeNode typeTreeNode) {
        return typeTreeNode.name.toLowerCase().matches(regex)
    }

    public boolean accept(ShapeTemplate shapeTemplate) {
        return shapeTemplate.getDisplayName().toLowerCase().matches(regex)
    }

    private String getRegex(String expr) {
        if (!expr.startsWith('.*')) {
            expr = '.*' + expr
        }
        if (!expr.endsWith('.*')) {
            expr = expr + '.*'
        }
        return expr
    }

}