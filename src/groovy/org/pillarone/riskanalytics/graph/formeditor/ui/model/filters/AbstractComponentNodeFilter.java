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
public abstract class AbstractComponentNodeFilter implements IComponentNodeFilter {

    private AbstractGraphModel fGraphModel;
    private List<ComponentNode> fSelectedNodes;
    private List<Connection> fSelectedConnections;

    public abstract boolean isNodeSelected(ComponentNode node);

    public void setGraphModel(AbstractGraphModel model) {
        fGraphModel = model;
        fSelectedNodes = null;
        fSelectedConnections = null;
    }

    public List<ComponentNode> getFilteredComponentNodeList() {
        if (fSelectedNodes==null) {
            fSelectedNodes = new ArrayList<ComponentNode>();
            for (ComponentNode node : fGraphModel.getAllComponentNodes()) {
                if (isNodeSelected(node)) {
                    fSelectedNodes.add(node);
                }
            }
        }
        return fSelectedNodes;
    }

    public List<Connection> getFilteredConnectionList() {
        if (fSelectedConnections==null) {
            fSelectedConnections = new ArrayList<Connection>();
            for (Connection c : fGraphModel.getAllConnections()) {
                if (isConnectionSelected(c)) {
                    fSelectedConnections.add(c);
                }
            }
        }
        return fSelectedConnections;
    }

    public boolean isConnectionSelected(Connection c) {
        return getFilteredComponentNodeList().contains(c.getFrom().getComponentNode())
                        && getFilteredComponentNodeList().contains(c.getTo().getComponentNode());
    }
}
