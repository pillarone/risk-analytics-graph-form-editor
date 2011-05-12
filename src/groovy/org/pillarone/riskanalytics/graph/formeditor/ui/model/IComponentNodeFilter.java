package org.pillarone.riskanalytics.graph.formeditor.ui.model;

import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.model.ComponentNode;
import org.pillarone.riskanalytics.graph.core.graph.model.Connection;

import java.util.List;

/**
 * 
 */
public interface IComponentNodeFilter {
    public void setGraphModel(AbstractGraphModel model);
    public List<ComponentNode> getFilteredComponentNodeList();
    public List<Connection> getFilteredConnectionList();
}
