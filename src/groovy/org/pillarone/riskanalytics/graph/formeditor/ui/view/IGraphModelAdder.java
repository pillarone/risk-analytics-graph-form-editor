package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.TypeDefinitionBean;

/**
 *
 */
public interface IGraphModelAdder {
    public void addModelToView(AbstractGraphModel model, TypeDefinitionBean typeDef, boolean isEditable);
}
