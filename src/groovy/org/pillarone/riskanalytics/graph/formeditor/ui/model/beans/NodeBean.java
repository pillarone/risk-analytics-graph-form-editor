package org.pillarone.riskanalytics.graph.formeditor.ui.model.beans;


public class NodeBean {
    private String name;
    private String componentType;
    private String comment;
    private boolean starter;

    public NodeBean() {
        super();
    }

    public NodeBean(NodeBean node) {
        this.name = node.name;
        this.componentType = node.componentType;
        this.comment = node.comment;
        this.starter = node.starter;
    }

    public boolean isEqual(NodeBean node) {
        return this.name.equals(node.getName()) && this.getComponentType().equals(node.getComponentType());
    }

    public boolean isStarter() {
        return starter;
    }

    public void setStarter(boolean starter) {
        this.starter = starter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String type) {
        this.componentType = type;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void reset() {
        name = null;
        componentType = null;
        comment = null;
        starter = false;
    }
}
