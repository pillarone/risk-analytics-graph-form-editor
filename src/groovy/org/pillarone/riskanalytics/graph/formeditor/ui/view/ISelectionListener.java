package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import org.pillarone.riskanalytics.graph.core.graph.model.ComponentNode;
import org.pillarone.riskanalytics.graph.core.graph.model.Connection;
import org.pillarone.riskanalytics.graph.core.graph.model.filters.IComponentNodeFilter;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.treetable.NodeNameFilter;

import java.util.List;

/**
 *
 */
public interface ISelectionListener {

    public void applyFilter(IComponentNodeFilter filter);

    public void applyFilter(NodeNameFilter filter);

    public void setSelectedComponents(List<ComponentNode> selection);

    public void nodeSelected(String path);

    public void setSelectedConnections(List<Connection> selection);

    public void clearSelection();
}
