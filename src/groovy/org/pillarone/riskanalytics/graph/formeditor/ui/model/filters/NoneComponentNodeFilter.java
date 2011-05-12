package org.pillarone.riskanalytics.graph.formeditor.ui.model.filters;

import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.model.ComponentNode;
import org.pillarone.riskanalytics.graph.core.graph.model.Connection;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.IComponentNodeFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class NoneComponentNodeFilter implements IComponentNodeFilter {

    private AbstractGraphModel fGraphModel;

    public NoneComponentNodeFilter() {
    }

    public void setGraphModel(AbstractGraphModel model) {
        fGraphModel = model;
    }

    public List<ComponentNode> getFilteredComponentNodeList() {
        return fGraphModel.getAllComponentNodes();
    }

    public List<Connection> getFilteredConnectionList() {
        return fGraphModel.getAllConnections();
    }
}
