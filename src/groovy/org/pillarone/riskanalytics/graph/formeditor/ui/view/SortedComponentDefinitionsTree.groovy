package org.pillarone.riskanalytics.graph.formeditor.ui.view

import com.ulcjava.base.application.tree.ITreeModel
import org.pillarone.riskanalytics.graph.formeditor.ui.model.palette.ComponentTypeTreeModelFactory
import org.pillarone.riskanalytics.graph.formeditor.ui.model.palette.FilteringTreeModel

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */
class SortedComponentDefinitionsTree extends AbstractComponentDefinitionTree {

    public SortedComponentDefinitionsTree(GraphModelEditor parent) {
        super(parent)
    }

    @Override
    protected ITreeModel getTreeModel() {
        new FilteringTreeModel(ComponentTypeTreeModelFactory.getSortedComponentDefinitionsTreeModel());
    }


}
