package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.base.application.tree.DefaultMutableTreeNode;
import com.ulcjava.base.application.tree.DefaultTreeModel;
import com.ulcjava.base.application.tree.ITreeModel;
import com.ulcjava.base.application.tree.ITreeNode;
import org.pillarone.riskanalytics.graph.core.palette.model.ComponentDefinition;
import org.pillarone.riskanalytics.graph.core.palette.service.IPaletteServiceListener;
import org.pillarone.riskanalytics.graph.core.palette.service.PaletteService;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.palette.ComponentTypeTreeModelFactory;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.palette.FilteringTreeModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.palette.TypeTreeNode;

public class ComponentTypeTree extends AbstractComponentDefinitionTree {

    public ComponentTypeTree(GraphModelEditor parent) {
        super(parent);
    }

    @Override
    protected DefaultTreeModel createTreeModel() {
        return ComponentTypeTreeModelFactory.getPackageTree();
    }

    @Override
    protected void insertNodeForComponentDefinition(ComponentDefinition definition) {
        //TODO
    }
}
