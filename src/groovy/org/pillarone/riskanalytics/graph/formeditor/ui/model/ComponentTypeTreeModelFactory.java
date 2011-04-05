package org.pillarone.riskanalytics.graph.formeditor.ui.model;

import com.ulcjava.base.application.tree.*;
import org.pillarone.riskanalytics.graph.core.palette.model.ComponentDefinition;
import org.pillarone.riskanalytics.graph.core.palette.service.PaletteService;
import org.pillarone.riskanalytics.graph.formeditor.ui.view.ComponentTypeTreeCellRenderer;

import java.util.List;

public class ComponentTypeTreeModelFactory {

    protected static final String PATHSEP = "\\.";
    private ComponentTypeTreeCellRenderer fTreeCellRenderer;

    public static DefaultTreeModel getTree() {
        List<ComponentDefinition> componentDefinitions = PaletteService.getInstance().getAllComponentDefinitions();
        TypeTreeNode root = new TypeTreeNode("", "Types");
        for (ComponentDefinition componentDef : componentDefinitions) {
            addNode(root, componentDef);
        }
        return new DefaultTreeModel(root);

    }

    private static void addNode(TypeTreeNode parent, ComponentDefinition cd) {
        String parentPath = parent.getPackagePath();
        String path = cd.getTypeClass().getName();
        String reducedPath = path.substring(parentPath.length()==0 ? 0 : parentPath.length()+1);
        String[] pathElements = reducedPath.split(PATHSEP);
        if (pathElements == null) {
            return;
        }
        if (pathElements.length==1) {
            TypeTreeNode node = new TypeTreeNode(cd);
            node.setLeaf(true);
            parent.add(node);
        } else if (pathElements.length>1) {
            String nodeName = pathElements[0];
            TypeTreeNode node = getNode(parent, nodeName);
            if (node != null) {
                node.setLeaf(false);
                addNode(node, cd);
            }
        }
    }

    private static TypeTreeNode getNode(TypeTreeNode parent, String name) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            TypeTreeNode nodeCandidate = (TypeTreeNode) parent.getChildAt(i);
            if (nodeCandidate.getName().equals(name)) {
                return nodeCandidate;
            }
        }
        String parentPath = parent.getPackagePath();
        String nodePath = parentPath==null || parentPath.length()==0
                            ? name
                            : parentPath+"."+name;
        TypeTreeNode node = new TypeTreeNode(nodePath, name);
        parent.add(node);
        return node;
    }
}
