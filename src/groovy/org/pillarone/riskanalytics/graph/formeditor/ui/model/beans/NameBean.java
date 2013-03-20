package org.pillarone.riskanalytics.graph.formeditor.ui.model.beans;


public class NameBean {
    private String name;
    private String portType;

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

    public String getPortType() {
        return portType;
    }

    public void setPortType(String portType) {
        this.portType = portType;
    }

    public void reset() {
        name = null;
        portType = null;
    }
}
