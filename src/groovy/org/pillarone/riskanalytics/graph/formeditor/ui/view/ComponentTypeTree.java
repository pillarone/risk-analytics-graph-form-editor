package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.base.application.tree.ITreeModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.ComponentTypeTreeModelFactory;

public class ComponentTypeTree extends AbstractComponentDefinitionTree {

    public ComponentTypeTree(GraphModelEditor parent) {
        super(parent);
    }

    public ITreeModel getTreeModel() {
        return ComponentTypeTreeModelFactory.getTree();
    }
}
