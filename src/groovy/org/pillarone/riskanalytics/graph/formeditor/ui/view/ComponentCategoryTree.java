package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.base.application.table.ITableModel;
import com.ulcjava.base.application.tree.DefaultTreeModel;
import com.ulcjava.base.application.tree.ITreeModel;
import org.pillarone.riskanalytics.graph.core.palette.model.ComponentDefinition;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.palette.ComponentTypeTreeModelFactory;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.palette.FilteringTreeModel;

public class ComponentCategoryTree extends AbstractComponentDefinitionTree{

    public ComponentCategoryTree(GraphModelEditor parent) {
        super(parent);
    }

    @Override
    protected DefaultTreeModel createTreeModel() {
        return ComponentTypeTreeModelFactory.getCategoryTree();
    }

    @Override
    protected void insertNodeForComponentDefinition(ComponentDefinition definition) {
        //TODO
    }
}
