package org.pillarone.riskanalytics.graph.formeditor.ui.model;

import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.model.ComponentNode;

import java.util.List;

/**
 * 
 */
public interface IFilterChangedListener {
    public void applyFilter(IComponentNodeFilter filter);
}
