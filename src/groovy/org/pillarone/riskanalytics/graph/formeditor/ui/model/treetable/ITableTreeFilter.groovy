package org.pillarone.riskanalytics.graph.formeditor.ui.model.treetable

interface ITableTreeFilter {
    boolean acceptNode(SimpleTableTreeNode node)
}

class NodeNameFilter implements ITableTreeFilter {

    String nodeName

    public NodeNameFilter(nodeName) {
        this.nodeName = nodeName;
    }

    public boolean acceptNode(SimpleTableTreeNode node) {
        return node.displayName.contains(nodeName)
    }
}

