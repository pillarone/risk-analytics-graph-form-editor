package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.base.application.tree.DefaultTreeModel;
import com.ulcjava.base.application.tree.TreePath;
import org.pillarone.riskanalytics.graph.core.palette.model.ComponentDefinition;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.palette.ComponentTypeTreeModelFactory;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.palette.TypeTreeNode;

import java.util.Arrays;
import java.util.List;

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
        final List<String> typeNames = Arrays.asList(definition.getTypeClass().getName().split("\\."));
        TypeTreeNode root = (TypeTreeNode) fTreeModel.getRoot();
        insertNode(root, definition, typeNames);
    }

    private void insertNode(TypeTreeNode current, ComponentDefinition definition, List<String> typeNames) {
        if (typeNames.size() > 1) {
            String currentNode = typeNames.get(0);
            insertNode(findOrCreateNode(current, currentNode), definition, typeNames.subList(1, typeNames.size()));
            return;
        }

        final int insertIndex = findInsertIndex(current, definition.getSimpleName());
        final TypeTreeNode typeTreeNode = new TypeTreeNode(definition);
        typeTreeNode.setLeaf(true);
        current.insert(typeTreeNode, insertIndex);
        getActualTreeModel().nodesWereInserted(new TreePath(DefaultTreeModel.getPathToRoot(current)), new int[]{insertIndex});

    }


}