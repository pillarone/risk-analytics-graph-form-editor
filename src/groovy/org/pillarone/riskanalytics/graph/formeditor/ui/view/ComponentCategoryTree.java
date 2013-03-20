package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.base.application.tree.DefaultTreeModel;
import com.ulcjava.base.application.tree.TreePath;
import org.pillarone.riskanalytics.graph.core.palette.model.ComponentDefinition;
import org.pillarone.riskanalytics.graph.core.palette.service.PaletteService;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.palette.ComponentTypeTreeModelFactory;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.palette.TypeTreeNode;

import java.util.List;

public class ComponentCategoryTree extends AbstractComponentDefinitionTree {

    public ComponentCategoryTree(GraphModelEditor parent) {
        super(parent);
    }

    @Override
    protected DefaultTreeModel createTreeModel() {
        return ComponentTypeTreeModelFactory.getCategoryTree();
    }

    @Override
    protected void insertNodeForComponentDefinition(ComponentDefinition definition) {
        final List<String> categories = PaletteService.getInstance().getCategoriesFromDefinition(definition);
        TypeTreeNode root = (TypeTreeNode) fTreeModel.getRoot();
        for (String category : categories) {
            final TypeTreeNode categoryNode = findOrCreateNode(root, category);
            TypeTreeNode leaf = new TypeTreeNode(definition);
            leaf.setLeaf(true);
            final int insertIndex = findInsertIndex(categoryNode, definition.getTypeClass().getSimpleName());
            categoryNode.insert(leaf, insertIndex);
            getActualTreeModel().nodesWereInserted(new TreePath(DefaultTreeModel.getPathToRoot(categoryNode)), new int[]{insertIndex});
        }

    }

}