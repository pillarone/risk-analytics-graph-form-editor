package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.applicationframework.application.ApplicationContext;
import com.ulcjava.base.application.ClientContext;
import com.ulcjava.base.application.ULCTableTree;
import com.ulcjava.base.application.event.ITreeSelectionListener;
import com.ulcjava.base.application.event.TreeSelectionEvent;
import com.ulcjava.base.application.tabletree.AbstractTableTreeModel;
import com.ulcjava.base.application.tabletree.ULCTableTreeColumn;
import com.ulcjava.base.application.tree.TreePath;
import com.ulcjava.base.application.tree.ULCTreeSelectionModel;
import com.ulcjava.base.application.util.Color;
import com.ulcjava.base.application.util.Dimension;
import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.model.ComponentNode;
import org.pillarone.riskanalytics.graph.core.graph.model.Connection;
import org.pillarone.riskanalytics.graph.core.graph.model.IGraphModelChangeListener;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.NodesTableTreeModel;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class NodesTable extends ULCTableTree {

    AbstractGraphModel fGraphModel;
    NodesTableTreeModel fTableModel;

    public NodesTable(ApplicationContext ctx, AbstractGraphModel model) {
        fTableModel = new NodesTableTreeModel(ctx, model);
        fGraphModel = model;
        this.setModel(fTableModel);
        this.createDefaultColumnsFromModel();
        this.setShowGrid(true);
        //this.setRowSorter(new TableRowSorter(nodesModel));
        int width = ClientContext.getScreenWidth();
        int height = ClientContext.getScreenHeight();
        int preferredWidth = width / 2;
        int preferredHeight = preferredWidth * height * 10 / (width * 11 * 2);
        this.setPreferredScrollableViewportSize(new Dimension(preferredWidth, preferredHeight));
        final IGraphModelChangeListener graphListener = new GraphListener();
        fGraphModel.addGraphModelChangeListener(graphListener);
        this.getSelectionModel().addTreeSelectionListener(
                new ITreeSelectionListener() {
                    public void valueChanged(TreeSelectionEvent treeSelectionEvent) {
                        List<ComponentNode> selectedNodes = getComponentNodes(getSelectedPaths());
                        fGraphModel.setSelectedNodes(selectedNodes, graphListener);
                    }
                }
        );
        this.getSelectionModel().setSelectionMode(ULCTreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        this.setSelectionBackground(Color.green);
        ULCTableTreeColumn col = this.getColumnModel().getColumn(NodesTableTreeModel.INFOID);
        col.setMaxWidth(50);
    }

    public AbstractTableTreeModel getModel() {
        return fTableModel;
    }

    private class GraphListener implements IGraphModelChangeListener {
        public void filtersApplied() {
            fTableModel.resetCache();
        }

        public void connectionAdded(Connection c) {
        }

        public void connectionRemoved(Connection c) {
        }

        public void nodeAdded(ComponentNode node) {
            fTableModel.addToCache(node, fGraphModel);
            fTableModel.nodeStructureChanged(new TreePath(fGraphModel));
        }

        public void nodeRemoved(ComponentNode node) {
            int index = fTableModel.getIndexOfChild(fGraphModel, node);
            if (index>=0) {
                fTableModel.removeFromCache(node);
                fTableModel.nodeStructureChanged(new TreePath(fGraphModel));
            } else {
                System.out.println("Node with name " + node.getName() + " not found in the graph model.");
            }
        }

        public void nodesSelected(List<ComponentNode> nodes) {
            clearSelection();
            addPathSelection(getTreePaths(nodes));
        }

        public void connectionsSelected(List<Connection> connections) {
            // nothing to do here
        }

        public void selectionCleared() {
            clearSelection();
        }

        public void nodePropertyChanged(ComponentNode node, String propertyName, Object oldValue, Object newValue) {
            fTableModel.nodeChanged(new TreePath(new Object[]{fGraphModel,node}));
        }
    }

    public TreePath[] getTreePaths(List<ComponentNode> nodes) {
        return fTableModel.getTreePaths(nodes.toArray(new ComponentNode[0]));
    }

    public ComponentNode getComponentNode(TreePath treePath) {
        if (treePath != null && treePath.getPathCount()==2 && treePath.getPath()[1] instanceof ComponentNode) {
            return (ComponentNode) treePath.getPath()[1];
        }
        return null;
    }

    public List<ComponentNode> getComponentNodes(TreePath[] treePaths) {
        List<ComponentNode> nodes = new ArrayList<ComponentNode>();
        if (treePaths != null) {
            for (TreePath path : treePaths) {
                nodes.add(getComponentNode(path));
            }
        }
        return nodes;
    }
}
