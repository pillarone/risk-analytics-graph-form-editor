package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.base.application.tree.ITreeModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.palette.ComponentTypeTreeModelFactory;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.palette.FilteringTreeModel;

public class ComponentCategoryTree extends AbstractComponentDefinitionTree{

    public ComponentCategoryTree(GraphModelEditor parent) {
        super(parent);
    }

    public ITreeModel getTreeModel() {
        return new FilteringTreeModel(ComponentTypeTreeModelFactory.getCategoryTree());
    }
}
