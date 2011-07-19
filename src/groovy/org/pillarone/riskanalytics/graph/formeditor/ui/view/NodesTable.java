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
import com.ulcjava.base.shared.UlcEventConstants;
import org.pillarone.riskanalytics.graph.core.graph.model.*;
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
        ClientContext.setModelUpdateMode(fTableModel, UlcEventConstants.SYNCHRONOUS_MODE);
        this.setShowGrid(true);
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
            //fTableModel.nodesWereInserted(new TreePath(fGraphModel), new int[]{fGraphModel.getAllComponentNodes().size() - 1}); TODO: Does not work some reason ???
            fTableModel.nodeStructureChanged(new TreePath(fGraphModel));
        }

        public void nodeRemoved(ComponentNode node) {
            try {
                fTableModel.removeFromCache(node);
                // fTableModel.nodesWereRemoved(new TreePath(fGraphModel), new int[]{index}, new Object[]{node}); TODO: DOes not work for some reason
                fTableModel.nodeStructureChanged(new TreePath(fGraphModel));
            } catch (Exception ex) {
                System.out.println("Exception while removing entry in the tree  table: " + node.getName() + " - exception: " + ex.getMessage());
            }
        }

        public void outerPortAdded(Port p) {
            fTableModel.addToCache(p, fGraphModel);
            fTableModel.nodeStructureChanged(new TreePath(fGraphModel));
        }

        public void outerPortRemoved(Port p) {
            int index = fTableModel.getIndexOfChild(fGraphModel, p);
            if (index>=0) {
                fTableModel.removeFromCache(p);
                fTableModel.nodeStructureChanged(new TreePath(fGraphModel));
            } else {
                System.out.println("Outer port with name " + p.getName() + " not found in the graph model.");
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
                ComponentNode node = getComponentNode(path);
                if (node != null) {
                    nodes.add(node);
                }
            }
        }
        return nodes;
    }
}
