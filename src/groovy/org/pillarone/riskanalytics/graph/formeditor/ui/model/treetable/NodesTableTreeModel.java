package org.pillarone.riskanalytics.graph.formeditor.ui.model.treetable;

import com.ulcjava.applicationframework.application.ApplicationContext;
import com.ulcjava.applicationframework.application.ResourceMap;
import com.ulcjava.base.application.tabletree.AbstractTableTreeModel;
import com.ulcjava.base.application.tabletree.ITableTreeModel;
import com.ulcjava.base.application.tabletree.ITableTreeNode;
import com.ulcjava.base.application.tree.TreePath;
import org.pillarone.riskanalytics.graph.core.graph.model.*;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.treetable.GraphElementNode;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.treetable.SimpleTableTreeNode;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.treetable.GraphElementTreeBuilder;

import java.util.ArrayList;
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
    GraphElementTreeBuilder fTreeBuilder;

    /**
     * @param ctx   is used basically to access the presentation of the table headers.
     * @param model the graph model used as basis for the table model.
     */
    public NodesTableTreeModel(ApplicationContext ctx, AbstractGraphModel model) {
        super();
        fContext = ctx;
        fGraphModel = model;
        fColumnNames = getColumnNames();
        fTreeBuilder = new GraphElementTreeBuilder(model);
        //addGraphModelListeners();
    }

    public AbstractGraphModel getGraphModel() {
        return fGraphModel;
    }

    private String[] getColumnNames() {
        if (fContext != null) {
            ResourceMap resourceMap = fContext.getResourceMap(getClass());
            String[] columnHeaders = new String[3];
            columnHeaders[NAMEID] = resourceMap.getString("name.columnHeader");
            columnHeaders[TYPEID] = resourceMap.getString("componentType.columnHeader");
            columnHeaders[INFOID] = resourceMap.getString("info.columnHeader");
            return columnHeaders;
        } else {
            String[] columnHeaders = new String[3];
            columnHeaders[NAMEID] = "name";
            columnHeaders[TYPEID] = "type";
            columnHeaders[INFOID] = "info";
            return columnHeaders;
        }
    }

    /**
     * Three or four columns depending whether the underlying model is a {@link org.pillarone.riskanalytics.graph.core.graph.model.ModelGraphModel} or
     * a {@link org.pillarone.riskanalytics.graph.core.graph.model.ComposedComponentGraphModel}.
     */
    public int getColumnCount() {
        return 3;
    }

    public Object getValueAt(Object o, int column) {
        if (o instanceof GraphElementNode) {
            return ((GraphElementNode)o).getValueAt(column);
        }
        return null;
    }

    public Class getColumnClass(int columnIndex) {
        return String.class;
    }

    public GraphElementNode getRoot() {
        return fTreeBuilder.getRoot();
    }

    public GraphElementNode getChild(Object parent, int i) {
        if (parent instanceof SimpleTableTreeNode) {
            return (GraphElementNode)((GraphElementNode)parent).getChildAt(i);
        }
        return null;
    }

    public int getChildCount(Object parent) {
        if (parent instanceof SimpleTableTreeNode) {
            return ((SimpleTableTreeNode)parent).getChildCount();
        }
        return 0;
    }

    public boolean isLeaf(Object node) {
        return getChildCount(node) == 0;
    }

    public int getIndexOfChild(Object parent, Object child) {
        if (parent instanceof SimpleTableTreeNode && child instanceof ITableTreeNode) {
            return ((SimpleTableTreeNode)parent).getIndex((ITableTreeNode) child);
        }
        return -1;
    }

    public GraphElementNode addChild(GraphElementNode parent, GraphElement element) {
        GraphElementNode child = fTreeBuilder.buildNode(element);
        if (child != null) {
            int numOfChildren = parent.getChildCount();
            parent.add(child);
            nodesWereInserted(getTreePath(fTreeBuilder.getRoot()), new int[]{numOfChildren});
        }
        return child;
    }

    public void removeChild(GraphElementNode parent, GraphElementNode child) {
        parent.remove(child);
        int childIndex = parent.getIndex(child);
        nodesWereRemoved(getTreePath(parent), new int[]{childIndex}, new Object[]{child});
    }

    public GraphElementNode findNode(GraphElement element) {
        return fTreeBuilder.findNode(element);
    }

    public String getColumnName(int column) {
        return fColumnNames[column];
    }

    /**public void resetCache() {
        fTreeBuilder = new GraphElementTreeBuilder(fGraphModel);
        nodeStructureChanged(new TreePath(fGraphModel));
    }*/

    public TreePath getTreePath(GraphElementNode node) {
        List<Object> pathElements = new ArrayList<Object>();
        pathElements.add(node);
        ITableTreeNode currentNode = node.getParent();
        while (currentNode != null && currentNode instanceof GraphElementNode) {
            pathElements.add(0, currentNode);
            currentNode = currentNode.getParent();
        }
        return new TreePath(pathElements.toArray());
    }

    public TreePath[] getTreePaths(ComponentNode[] nodes) {
        List<TreePath> paths = new ArrayList<TreePath>();
        for (ComponentNode graphElement : nodes) {
            GraphElementNode node = findNode(graphElement);
            if (node != null) {
                TreePath path = getTreePath(node);
                paths.add(path);
            }
        }
        return paths.toArray(new TreePath[paths.size()]);
    }

    private void printTree() {
        printNode(fTreeBuilder.getRoot(), "");
    }

    private void printNode(ITableTreeNode node, String tab) {
        if (node instanceof SimpleTableTreeNode) {
            String name = ((SimpleTableTreeNode) node).getName();
            System.out.println(tab + name);
            for (ITableTreeNode child : ((SimpleTableTreeNode) node).getChildren()) {
                printNode(child, tab + "   ");
            }
        }
    }
}
