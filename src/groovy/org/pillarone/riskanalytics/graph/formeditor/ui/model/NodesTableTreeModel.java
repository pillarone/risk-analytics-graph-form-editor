package org.pillarone.riskanalytics.graph.formeditor.ui.model;

import com.ulcjava.applicationframework.application.ApplicationContext;
import com.ulcjava.applicationframework.application.ResourceMap;
import com.ulcjava.base.application.ULCLabel;
import com.ulcjava.base.application.tabletree.AbstractTableTreeModel;
import com.ulcjava.base.application.tabletree.ITableTreeModel;
import com.ulcjava.base.application.tree.TreePath;
import org.pillarone.riskanalytics.graph.core.graph.model.*;
import org.pillarone.riskanalytics.graph.core.graph.util.IntegerRange;
import org.pillarone.riskanalytics.graph.core.graphimport.ComposedComponentGraphImport;
import org.pillarone.riskanalytics.graph.formeditor.util.UIUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Model underlying the nodes table in the form editor. This version also allows to hierarchically browse (recursively) components.
 * It refers directly to an instance of the {@link org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel} from which
 * the list of all components is obtained.
 * <p/>
 * The table listens to changes in the graph model through a {@link org.pillarone.riskanalytics.graph.core.graph.model.IGraphModelChangeListener}.
 * <p/>
 * For {@link org.pillarone.riskanalytics.graph.core.graph.model.ComposedComponentGraphModel}'s the table consists of 3 columns (name, type).
 *
 * @author martin.melchior
 */
public class NodesTableTreeModel extends AbstractTableTreeModel implements ITableTreeModel {

    public static final int NAMEID = 0;
    public static final int TYPEID = 2;
    public static final int INFOID = 1;

    private ApplicationContext fContext;
    private AbstractGraphModel fGraphModel;
    private String[] fColumnNames;
    private HashMap<GraphElement, List<GraphElement>> fCache;

    /**
     * @param ctx   is used basically to access the presentation of the table headers.
     * @param model the graph model used as basis for the table model.
     */
    public NodesTableTreeModel(ApplicationContext ctx, AbstractGraphModel model) {
        super();
        fContext = ctx;
        fGraphModel = model;
        fColumnNames = getColumnNames();
        resetCache();
        //addGraphModelListeners();
    }

    public void addToCache(GraphElement node, GraphElement parent) {
        if (!fCache.get(parent).contains(node)) {
            fCache.get(parent).add(node);
            fCache.put(node, new ArrayList<GraphElement>());
        }
        for (GraphElement child : searchForChildren(node)) {
            addToCache(child, node);
        }
    }

    private List<GraphElement> searchForChildren(GraphElement parent) {
        if (parent instanceof ComposedComponentNode) {
            ComposedComponentNode cc = (ComposedComponentNode) parent;
            if (cc.getComponentGraph() == null) {
                ComposedComponentGraphImport importer = new ComposedComponentGraphImport();
                ComposedComponentGraphModel ccModel = (ComposedComponentGraphModel) importer.importGraph(cc.getType().getTypeClass(), "");
                cc.setComponentGraph(ccModel);
            }
            return searchForChildren(((ComposedComponentNode) parent).getComponentGraph());
        }
        List<GraphElement> children = new ArrayList<GraphElement>();
        if (parent instanceof ComponentNode) {
            ComponentNode node = (ComponentNode) parent;
            children.addAll(node.getInPorts());
            children.addAll(node.getOutPorts());
        } else if (parent instanceof AbstractGraphModel) {
            if (parent == fGraphModel) {
                children.addAll(fGraphModel.getFilteredComponentsList());
            } else {
                children.addAll(((AbstractGraphModel) parent).getAllComponentNodes());
            }
            if (parent instanceof ComposedComponentGraphModel) {
                ComposedComponentGraphModel node = (ComposedComponentGraphModel) parent;
                children.addAll(node.getOuterInPorts());
                children.addAll(node.getOuterOutPorts());
            }
        }
        return children;
    }

    public void removeFromCache(GraphElement node) {
        List<GraphElement> toRemove = new ArrayList<GraphElement>();
        markToRemove(node, toRemove);
        for (Object n : toRemove) {
            fCache.remove(n);
        }
        fCache.get(fGraphModel).remove(node);
    }

    private void markToRemove(GraphElement node, List<GraphElement> toRemove) {
        if (toRemove == null) {
            toRemove = new ArrayList<GraphElement>();
        }
        if (fCache.containsKey(node)) {
            if (!toRemove.contains(node)) {
                toRemove.add(node);
            }
            List<GraphElement> children = fCache.get(node);
            for (GraphElement child : children) {
                markToRemove(child, toRemove);
            }
        }
    }

    private String[] getColumnNames() {
        ResourceMap resourceMap = fContext.getResourceMap(getClass());
        String[] columnHeaders = new String[3];
        columnHeaders[NAMEID] = resourceMap.getString("name.columnHeader");
        columnHeaders[TYPEID] = resourceMap.getString("componentType.columnHeader");
        columnHeaders[INFOID] = resourceMap.getString("info.columnHeader");
        return columnHeaders;
    }

    /**
     * Three or four columns depending whether the underlying model is a {@link org.pillarone.riskanalytics.graph.core.graph.model.ModelGraphModel} or
     * a {@link org.pillarone.riskanalytics.graph.core.graph.model.ComposedComponentGraphModel}.
     */
    public int getColumnCount() {
        return 3;
    }

    public Object getValueAt(Object o, int column) {
        if (o instanceof AbstractGraphModel) { // root node
            switch (column) {
                case NAMEID:
                    return fGraphModel.getDisplayName();
                case TYPEID:
                    return fGraphModel.getDisplayName() + " (" + fGraphModel.getPackageName() + ")";
                case INFOID:
                    return EnumGraphElementInfo.M.getDisplayValue();
                default:
                    return null;
            }
        } else if (o instanceof ComponentNode) {
            ComponentNode node = (ComponentNode) o;
            switch (column) {
                case NAMEID:
                    return node.getDisplayName();
                case TYPEID:
                    return node.getType().getTypeClass().getSimpleName() + " (" + node.getType().getTypeClass().getPackage().getName() + ")";
                case INFOID:
                    return o instanceof ComposedComponentNode ? EnumGraphElementInfo.CC.getDisplayValue() : EnumGraphElementInfo.C.getDisplayValue();
                default:
                    return null;
            }
        } else if (o instanceof Port) {
            Port port = (Port) o;
            switch (column) {
                case NAMEID:
                    return port.getDisplayName();
                case TYPEID:
                    return port.getPacketType().getSimpleName() + " (" + port.getPacketType().getPackage().getName() + ")";
                case INFOID:
                    if (port instanceof InPort) {
                        String value = EnumGraphElementInfo.IN.getDisplayValue();
                        IntegerRange range = port.getConnectionCardinality();
                        if (range != null) {
                            int numOfConn = ((InPort) port).getConnectionCount();
                            if (numOfConn < range.getTo()) {
                                value = EnumGraphElementInfo.IN_PLUS.getDisplayValue();
                            }
                            if (numOfConn < range.getFrom()) {
                                value = EnumGraphElementInfo.IN_PLUS_EX.getDisplayValue();
                            }
                            if (numOfConn > range.getTo()) {
                                value = EnumGraphElementInfo.IN_MINUS.getDisplayValue();
                            }
                        }
                        return value;
                    } else {
                        return EnumGraphElementInfo.OUT.getDisplayValue();
                    }
                default:
                    return null;
            }
        }
        return null;
    }

    public Class getColumnClass(int columnIndex) {
        return String.class;
    }

    public Object getRoot() {
        return fGraphModel;
    }

    public Object getChild(Object parent, int i) {
        return fCache.get(parent).get(i);
    }

    public int getChildCount(Object parent) {
        return fCache.containsKey(parent) ? fCache.get(parent).size() : 0;
    }

    public boolean isLeaf(Object o) {
        return getChildCount(o) == 0;
    }

    public int getIndexOfChild(Object parent, Object child) {
        return fCache.get(parent).indexOf(child);
    }

    public String getColumnName(int column) {
        return fColumnNames[column];
    }

    public void resetCache() {
        fCache = new HashMap<GraphElement, List<GraphElement>>();
        fCache.put(fGraphModel, new ArrayList<GraphElement>());
        for (GraphElement node : searchForChildren(fGraphModel)) {
            addToCache(node, fGraphModel);
        }
        nodeStructureChanged(new TreePath(fGraphModel));
        //printTree();
    }

    public TreePath[] getTreePaths(ComponentNode[] nodes) {
        List<TreePath> paths = new ArrayList<TreePath>();
        List<GraphElement> availableNodes = fCache.get(fGraphModel);
        for (GraphElement node : nodes) {
            if (availableNodes.contains(node)) {
                TreePath path = new TreePath(new Object[]{fGraphModel, node});
                paths.add(path);
            }
        }
        return paths.toArray(new TreePath[paths.size()]);
    }

    private void printTree() {
        if (fCache.containsKey(fGraphModel)) {
            printNode(fGraphModel, "");
        }
    }

    private void printNode(Object node, String tab) {
        String name = null;
        if (node instanceof AbstractGraphModel) {
            name = ((AbstractGraphModel) node).getName();
        } else if (node instanceof ComponentNode) {
            name = ((ComponentNode) node).getName();
        } else if (node instanceof Port) {
            name = ((Port) node).getName();
        }
        System.out.println(tab + name);
        for (Object child : fCache.get(node)) {
            printNode(child, tab + "   ");
        }
    }
}
