package org.pillarone.riskanalytics.graph.formeditor.ui;

import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.persistence.GraphPersistenceService;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.TypeDefinitionBean;

/**
 *
 */
public interface IGraphModelHandler {
    public void addModel(AbstractGraphModel model, TypeDefinitionBean typeDef, boolean isEditable);
    public void removeModel(AbstractGraphModel model);
    public void renameModel(AbstractGraphModel model);
    public GraphPersistenceService getPersistenceService();
}
