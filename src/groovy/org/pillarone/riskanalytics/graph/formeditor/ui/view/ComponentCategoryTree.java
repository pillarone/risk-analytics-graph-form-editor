package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.base.application.ULCAlert;
import com.ulcjava.base.application.ULCBoxPane;
import com.ulcjava.base.application.ULCScrollPane;
import com.ulcjava.base.application.ULCTree;
import com.ulcjava.base.application.event.ActionEvent;
import com.ulcjava.base.application.event.IActionListener;
import com.ulcjava.base.application.tree.ITreeModel;
import com.ulcjava.base.application.tree.TreePath;
import com.ulcjava.base.application.tree.ULCTreeSelectionModel;
import com.ulcjava.base.application.util.Dimension;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.ComponentTypeTreeModelFactory;
import org.pillarone.riskanalytics.graph.formeditor.util.ComponentTypeTreeUtilities;

public class ComponentCategoryTree extends AbstractComponentDefinitionTree {

    public ComponentCategoryTree(GraphModelEditor parent) {
        super(parent);
    }

    public ITreeModel getTreeModel() {
        return ComponentTypeTreeModelFactory.getCategoryTree();
    }
}
