package org.pillarone.riskanalytics.graph.formeditor.ui.model.palette

import com.ulcjava.base.application.tree.ITreeNode

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */
class NameFilter implements ITreeFilter {

    private String regex

    public NameFilter(String name) {
        regex = getRegex(name ? name.toLowerCase() : "")
    }

    boolean acceptNode(ITreeNode node) {
        return accept(node)
    }

    protected boolean accept(ITreeNode node) {
        return false
    }

    protected boolean accept(TypeTreeNode typeTreeNode) {
        return typeTreeNode.name.toLowerCase().matches(regex)
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
