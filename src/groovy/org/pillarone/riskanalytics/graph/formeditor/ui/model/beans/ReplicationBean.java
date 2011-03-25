package org.pillarone.riskanalytics.graph.formeditor.ui.model.beans;


public class ReplicationBean {
    private String outer;
    private String inner;

    public String getOuter() {
        return outer;
    }

    public void setOuter(String outer) {
        this.outer = outer;
    }

    public String getInner() {
        return inner;
    }

    public void setInner(String inner) {
        this.inner = inner;
    }

    public void reset() {
        outer = null;
        inner = null;
    }
}
