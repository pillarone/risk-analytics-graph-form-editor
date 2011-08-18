package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import org.pillarone.riskanalytics.graph.core.graph.model.ComponentNode;
import org.pillarone.riskanalytics.graph.core.graph.model.Connection;
import org.pillarone.riskanalytics.graph.core.graph.model.filters.IComponentNodeFilter;

import java.util.List;

/**
 *
 */
public interface ISelectionListener {

    public void applyFilter(IComponentNodeFilter filter);

    public void setSelectedComponents(List<ComponentNode> selection);

    public void setSelectedConnections(List<Connection> selection);

    public void clearSelection();
}
