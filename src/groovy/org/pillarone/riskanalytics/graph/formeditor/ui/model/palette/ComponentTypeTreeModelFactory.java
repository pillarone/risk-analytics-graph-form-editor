package org.pillarone.riskanalytics.graph.formeditor.ui.model.palette;

import com.ulcjava.base.application.tree.DefaultTreeModel;
import org.pillarone.riskanalytics.graph.core.palette.model.ComponentDefinition;
import org.pillarone.riskanalytics.graph.core.palette.service.IPaletteServiceListener;
import org.pillarone.riskanalytics.graph.core.palette.service.PaletteService;

import java.util.*;

public class ComponentTypeTreeModelFactory {

    protected static final String PATHSEP = "\\.";

    public static DefaultTreeModel getCategoryTree() {
        PaletteService service = PaletteService.getInstance();
        List<ComponentDefinition> componentDefinitions = service.getAllComponentDefinitions();
        Map<String, List<ComponentDefinition>> categoryMap = new HashMap<String, List<ComponentDefinition>>();
        for (ComponentDefinition cd : componentDefinitions) {
            List<String> categories = service.getCategoriesFromDefinition(cd);
            for (String c : categories) {
                if (!categoryMap.containsKey(c)) {
                    categoryMap.put(c, new ArrayList<ComponentDefinition>());
                }
                categoryMap.get(c).add(cd);
            }
        }
        TypeTreeNode root = new TypeTreeNode("", "Categories");
        root.setLeaf(false);
        final List<Map.Entry<String, List<ComponentDefinition>>> entries = new ArrayList<Map.Entry<String, List<ComponentDefinition>>>(categoryMap.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<String, List<ComponentDefinition>>>() {
            public int compare(Map.Entry<String, List<ComponentDefinition>> a, Map.Entry<String, List<ComponentDefinition>> b) {
                return a.getKey().compareTo(b.getKey());
            }
        });
        for (Map.Entry<String, List<ComponentDefinition>> category : entries) {
            TypeTreeNode categoryNode = new TypeTreeNode("", category.getKey());
            categoryNode.setLeaf(false);
            root.add(categoryNode);
            final List<ComponentDefinition> definitions = category.getValue();
            Collections.sort(definitions, new ComponentDefinitionComparator());
            for (ComponentDefinition cd : definitions) {
                TypeTreeNode leaf = new TypeTreeNode(cd);
                leaf.setLeaf(true);
                categoryNode.add(leaf);
            }
        }
        return new DefaultTreeModel(root);
    }

    public static DefaultTreeModel getFlatTree() {
        List<ComponentDefinition> componentDefinitions = PaletteService.getInstance().getAllComponentDefinitions();
        TypeTreeNode root = new TypeTreeNode("", "All Components");
        root.setLeaf(false);
        for (ComponentDefinition cd : componentDefinitions) {
            TypeTreeNode leaf = new TypeTreeNode(cd);
            leaf.setLeaf(true);
            root.add(leaf);
        }
        return new DefaultTreeModel(root);
    }

    public static DefaultTreeModel getPackageTree() {
        List<ComponentDefinition> componentDefinitions = PaletteService.getInstance().getAllComponentDefinitions();
        TypeTreeNode root = new TypeTreeNode("", "Types");
        root.setLeaf(false);
        for (ComponentDefinition componentDef : componentDefinitions) {
            addNode(root, "", componentDef);
        }
        return new DefaultTreeModel(root);
    }

    public static DefaultTreeModel getSortedComponentDefinitionsTreeModel() {
        List<ComponentDefinition> componentDefinitions = new ArrayList<ComponentDefinition>(PaletteService.getInstance().getAllComponentDefinitions());
        Collections.sort(componentDefinitions, ComponentDefinition.getComparator());
        final TypeTreeNode root = new TypeTreeNode("", "root");
        root.setLeaf(false);
        for (ComponentDefinition componentDef : componentDefinitions) {
            addNode(root, componentDef);
        }
        return new DefaultTreeModel(root);
    }

    private static void addNode(TypeTreeNode parent, ComponentDefinition cd) {
        TypeTreeNode node = new TypeTreeNode(cd);
        node.setLeaf(true);
        parent.add(node);
    }

    private static void addNode(TypeTreeNode parent, String parentPath, ComponentDefinition cd) {
        String path = cd.getTypeClass().getName();
        String reducedPath = path.substring(parentPath.length() == 0 ? 0 : parentPath.length() + 1);
        String[] pathElements = reducedPath.split(PATHSEP);
        if (pathElements == null) {
            return;
        }
        if (pathElements.length == 1) {
            TypeTreeNode node = new TypeTreeNode(cd);
            node.setLeaf(true);
            parent.add(node);
        } else if (pathElements.length > 1) {
            String nodeName = pathElements[0];
            TypeTreeNode node = getNode(parent, parentPath, nodeName);
            if (node != null) {
                node.setLeaf(false);
                addNode(node, node.getFullName(), cd);
            }
        }
    }

    private static TypeTreeNode getNode(TypeTreeNode parent, String parentPath, String name) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            TypeTreeNode nodeCandidate = (TypeTreeNode) parent.getChildAt(i);
            if (nodeCandidate.getName().equals(name)) {
                return nodeCandidate;
            }
        }
        TypeTreeNode node = new TypeTreeNode(parentPath, name);
        parent.add(node);
        return node;
    }

    public static class ComponentDefinitionComparator implements Comparator<ComponentDefinition> {
        public int compare(ComponentDefinition o1, ComponentDefinition o2) {
            return o1.getTypeClass().getName().compareTo(o2.getTypeClass().getName());
        }
    }
}
