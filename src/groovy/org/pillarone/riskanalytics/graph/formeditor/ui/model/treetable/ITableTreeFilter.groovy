package org.pillarone.riskanalytics.graph.formeditor.ui.model.treetable

import com.ulcjava.base.application.tabletree.ITableTreeNode

interface ITableTreeFilter {
    boolean acceptNode(SimpleTableTreeNode node)
}

class NodeNameFilter implements ITableTreeFilter {

    String nodeName

    public NodeNameFilter(nodeName) {
        this.nodeName = nodeName;
    }

    public boolean acceptNode(SimpleTableTreeNode node) {
        if (!nodeName) return true
        return node.displayName.contains(nodeName)
    }

    public boolean acceptNode(ITableTreeNode node) {
        if (!nodeName) return true
        Object obj = node.getValueAt(0)
        return obj && obj.toString().contains(nodeName)
    }
}

