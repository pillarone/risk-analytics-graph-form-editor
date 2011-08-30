package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.base.application.tree.DefaultTreeModel;
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
            final TypeTreeNode categoryNode = findCategoryNode(root, category);
            TypeTreeNode leaf = new TypeTreeNode(definition);
            leaf.setLeaf(true);
            categoryNode.insert(leaf, findInsertIndex(categoryNode, definition.getTypeClass().getName()));
        }

    }

    protected TypeTreeNode findCategoryNode(TypeTreeNode root, String name) {
        for (int i = 0; i < root.getChildCount(); i++) {
            TypeTreeNode child = (TypeTreeNode) root.getChildAt(i);
            if (child.getFullName().equals(name)) {
                return child;
            }
        }

        TypeTreeNode categoryNode = new TypeTreeNode("", name);
        root.insert(categoryNode, findInsertIndex(root, name));
        return categoryNode;
    }
}
