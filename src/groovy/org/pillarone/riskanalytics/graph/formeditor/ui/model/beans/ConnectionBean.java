package org.pillarone.riskanalytics.graph.formeditor.ui.model.beans;


public class ConnectionBean {
    private String from;
    private String to;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void reset() {
        from = null;
        to = null;
    }
}
