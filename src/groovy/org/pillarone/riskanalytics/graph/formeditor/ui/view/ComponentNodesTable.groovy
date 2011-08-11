package org.pillarone.riskanalytics.graph.formeditor.ui.view;


import com.ulcjava.applicationframework.application.ApplicationContext
import com.ulcjava.base.application.ClientContext
import com.ulcjava.base.application.ULCTableTree
import com.ulcjava.base.application.event.ITreeSelectionListener
import com.ulcjava.base.application.event.TreeSelectionEvent
import com.ulcjava.base.application.tabletree.AbstractTableTreeModel
import com.ulcjava.base.application.tabletree.DefaultTableTreeCellRenderer
import com.ulcjava.base.application.tabletree.ULCTableTreeColumn
import com.ulcjava.base.application.tree.TreePath
import com.ulcjava.base.application.tree.ULCTreeSelectionModel
import com.ulcjava.base.application.util.Color
import com.ulcjava.base.application.util.Dimension
import com.ulcjava.base.shared.UlcEventConstants
import org.pillarone.riskanalytics.graph.core.graph.model.filters.IComponentNodeFilter
import org.pillarone.riskanalytics.graph.core.graph.model.filters.NoneComponentNodeFilter
import org.pillarone.riskanalytics.graph.formeditor.ui.model.treetable.FilteringTableTreeModel
import org.pillarone.riskanalytics.graph.formeditor.ui.model.treetable.GraphElementNode
import org.pillarone.riskanalytics.graph.formeditor.ui.model.treetable.NodesTableTreeModel
import org.pillarone.riskanalytics.graph.core.graph.model.*

/**
 *
 */
public class ComponentNodesTable extends ULCTableTree {

    AbstractGraphModel fGraphModel;
    FilteringTableTreeModel fTableModel;

    public ComponentNodesTable(ApplicationContext ctx, AbstractGraphModel model) {
        NodesTableTreeModel originalModel = new NodesTableTreeModel(ctx, model)
        IComponentNodeFilter noFilter = new NoneComponentNodeFilter()
        fTableModel = new FilteringTableTreeModel(originalModel, noFilter)
        fGraphModel = model
        this.setModel(fTableModel)
        this.createDefaultColumnsFromModel()
        ClientContext.setModelUpdateMode(fTableModel, UlcEventConstants.SYNCHRONOUS_MODE)

        this.setShowGrid(true)
        int width = ClientContext.getScreenWidth()
        int height = ClientContext.getScreenHeight()
        int preferredWidth = width / 2
        int preferredHeight = preferredWidth * height * 10 / (width * 11 * 2)
        this.setPreferredScrollableViewportSize(new Dimension(preferredWidth, preferredHeight))

        final IGraphModelChangeListener graphListener = new GraphListener()
        fGraphModel.addGraphModelChangeListener(graphListener)
        this.getSelectionModel().addTreeSelectionListener(
                new ITreeSelectionListener() {
                    public void valueChanged(TreeSelectionEvent treeSelectionEvent) {
                        List<ComponentNode> selectedNodes = getComponentNodes(getSelectedPaths())
                        fGraphModel.setSelectedNodes(selectedNodes, graphListener)
                    }
                }
        )

        this.getSelectionModel().setSelectionMode(ULCTreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION)
        this.setSelectionBackground(Color.green)
        ULCTableTreeColumn col = this.getColumnModel().getColumn(NodesTableTreeModel.INFOID)
        DefaultTableTreeCellRenderer renderer = new InfoTableTreeCellRenderer()
        col.setCellRenderer(renderer)
        col.setMaxWidth(50)
    }

    public AbstractTableTreeModel getModel() {
        return fTableModel
    }

    private class GraphListener implements IGraphModelChangeListener {
        public void filtersApplied() {
            if (fGraphModel.nodeFilters && fGraphModel.nodeFilters.size()>0) {
                IComponentNodeFilter filter = fGraphModel.nodeFilters[0]
                fTableModel.setFilter(filter)
                fTableModel.applyFilter();
            }
        }

        public void connectionAdded(Connection c) {
        }

        public void connectionRemoved(Connection c) {
        }

        public void nodeAdded(ComponentNode node) {
            fTableModel.addChild(fTableModel.getRoot(), node);
        }

        public void nodeRemoved(ComponentNode node) {
            GraphElementNode treeNode = fTableModel.findNode(node);
            fTableModel.removeChild(fTableModel.getRoot(), treeNode);
        }

        public void outerPortAdded(Port p) {
            fTableModel.addChild(fTableModel.getRoot(), p);
        }

        public void outerPortRemoved(Port p) {
            GraphElementNode treeNode = fTableModel.findNode(p);
            fTableModel.removeChild(fTableModel.getRoot(), treeNode);
        }

        public void nodesSelected(List<ComponentNode> nodes) {
            clearSelection();
            ComponentNode[] nodes0 = nodes as ComponentNode[]
            addPathSelection(fTableModel.getTreePaths(nodes0));
        }

        public void connectionsSelected(List<Connection> connections) {
            // nothing to do here
        }

        public void selectionCleared() {
            clearSelection();
        }

        public void nodePropertyChanged(ComponentNode node, String propertyName, Object oldValue, Object newValue) {
            fTableModel.nodeChanged(fTableModel.getTreePath(node));
        }
    }

    public TreePath[] getTreePaths(List<ComponentNode> nodes) {
        return fTableModel.getTreePaths(nodes.toArray(new ComponentNode[0]));
    }

    public ComponentNode getComponentNode(TreePath treePath) {
        if (treePath != null && treePath.getPathCount() == 2 && treePath.getPath()[1] instanceof ComponentNode) {
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
