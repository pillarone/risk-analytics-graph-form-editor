package org.pillarone.riskanalytics.graph.formeditor.ui.model.beans;


public class NameBean {
    private String name;

    public NameBean() {
        super();
    }

    public NameBean(NameBean node) {
        this.name = node.name;
    }

    public boolean isEqual(NameBean node) {
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
