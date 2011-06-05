package org.pillarone.riskanalytics.graph.formeditor.ui.model;

import com.ulcjava.base.application.tree.DefaultTreeModel;
import org.pillarone.riskanalytics.graph.core.palette.model.ComponentDefinition;
import org.pillarone.riskanalytics.graph.core.palette.service.PaletteService;
import org.pillarone.riskanalytics.graph.formeditor.ui.view.ComponentTypeTreeCellRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static DefaultTreeModel getCategoryTree() {
        PaletteService service = PaletteService.getInstance();
        List<ComponentDefinition> componentDefinitions = service.getAllComponentDefinitions();
        Map<String,List<ComponentDefinition>> categoryMap = new HashMap<String,List<ComponentDefinition>>();
        for (ComponentDefinition cd : componentDefinitions) {
            List<String> categories = service.getCategoriesFromDefinition(cd);
            for (String c : categories) {
                if (!categoryMap.containsKey(c)) {
                    categoryMap.put(c, new ArrayList<ComponentDefinition>());
                }
                categoryMap.get(c).add(cd);
            }
        }
        TypeTreeNode root = new TypeTreeNode("","Categories");
        for (Map.Entry<String,List<ComponentDefinition>> category : categoryMap.entrySet()) {
            TypeTreeNode categoryNode = new TypeTreeNode("", category.getKey());
            root.add(categoryNode);
            for (ComponentDefinition cd : category.getValue()) {
                TypeTreeNode leaf = new TypeTreeNode(cd);
                categoryNode.add(leaf);
            }
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
