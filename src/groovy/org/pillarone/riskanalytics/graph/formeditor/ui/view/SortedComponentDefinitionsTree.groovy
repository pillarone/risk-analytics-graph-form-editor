package org.pillarone.riskanalytics.graph.formeditor.ui.view

import org.pillarone.riskanalytics.graph.formeditor.ui.model.palette.ComponentTypeTreeModelFactory
import org.pillarone.riskanalytics.graph.core.palette.model.ComponentDefinition
import org.pillarone.riskanalytics.graph.formeditor.ui.model.palette.TypeTreeNode
import com.ulcjava.base.application.tree.DefaultTreeModel
import com.ulcjava.base.application.tree.TreePath

/**
* @author fouad.jaada@intuitive-collaboration.com
*/
class SortedComponentDefinitionsTree extends AbstractComponentDefinitionTree {

    public SortedComponentDefinitionsTree(GraphModelEditor parent) {
        super(parent)
    }

    @Override
    protected DefaultTreeModel createTreeModel() {
        return ComponentTypeTreeModelFactory.getSortedComponentDefinitionsTreeModel()
    }

    @Override
    protected void insertNodeForComponentDefinition(ComponentDefinition definition) {
        TypeTreeNode node = new TypeTreeNode(definition);
        node.setLeaf(true);
        final TypeTreeNode root = (TypeTreeNode) fTreeModel.getRoot();
        final int index = findInsertIndex(root, definition.getTypeClass().getSimpleName())
        root.insert(node, index);
        getActualTreeModel().nodesWereInserted(new TreePath(root), [index] as int[]);
    }

}