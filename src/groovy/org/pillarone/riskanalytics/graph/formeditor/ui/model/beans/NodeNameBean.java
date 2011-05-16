package org.pillarone.riskanalytics.graph.formeditor.ui.model.beans;


public class NodeNameBean {
    private String name;

    public NodeNameBean() {
        super();
    }

    public NodeNameBean(NodeNameBean node) {
        this.name = node.name;
    }

    public boolean isEqual(NodeNameBean node) {
        return this.name.equals(node.getName());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void reset() {
        name = null;
    }
}
