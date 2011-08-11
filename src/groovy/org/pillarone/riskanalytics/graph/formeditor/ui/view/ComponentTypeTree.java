package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.base.application.tree.ITreeModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.palette.ComponentTypeTreeModelFactory;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.palette.FilteringTreeModel;

public class ComponentTypeTree extends AbstractComponentDefinitionTree {

    public ComponentTypeTree(GraphModelEditor parent) {
        super(parent);
    }

    public ITreeModel getTreeModel() {
        return new FilteringTreeModel(ComponentTypeTreeModelFactory.getPackageTree());
    }
}
