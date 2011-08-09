package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.base.application.tree.ITreeModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.ComponentTypeTreeModelFactory;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.FilteringTreeModel;

public class ComponentTypeTree extends AbstractComponentDefinitionTree {

    public ComponentTypeTree(GraphModelEditor parent) {
        super(parent);
    }

    public ITreeModel getTreeModel() {
        return new FilteringTreeModel(ComponentTypeTreeModelFactory.getTree());
    }
}
