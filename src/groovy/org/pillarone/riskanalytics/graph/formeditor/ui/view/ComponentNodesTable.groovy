package org.pillarone.riskanalytics.graph.formeditor.ui.view;


import com.ulcjava.applicationframework.application.Action
import com.ulcjava.applicationframework.application.ApplicationActionMap
import com.ulcjava.applicationframework.application.ApplicationContext
import com.ulcjava.base.application.dnd.DataFlavor
import com.ulcjava.base.application.dnd.DnDTreeData
import com.ulcjava.base.application.dnd.TransferHandler
import com.ulcjava.base.application.dnd.Transferable
import com.ulcjava.base.application.tabletree.AbstractTableTreeModel
import com.ulcjava.base.application.tabletree.DefaultTableTreeCellRenderer
import com.ulcjava.base.application.tabletree.ULCTableTreeColumn
import com.ulcjava.base.application.tree.TreePath
import com.ulcjava.base.application.tree.ULCTreeSelectionModel
import com.ulcjava.base.application.util.Dimension
import com.ulcjava.base.shared.UlcEventConstants
import org.pillarone.riskanalytics.graph.core.graph.model.filters.IComponentNodeFilter
import org.pillarone.riskanalytics.graph.core.graph.model.filters.NoneComponentNodeFilter
import org.pillarone.riskanalytics.graph.core.graph.util.UIUtils
import org.pillarone.riskanalytics.graph.formeditor.ui.IGraphModelHandler
import org.pillarone.riskanalytics.graph.formeditor.ui.IModelRenameListener
import org.pillarone.riskanalytics.graph.formeditor.ui.ISelectionListener
import org.pillarone.riskanalytics.graph.formeditor.ui.IWatchList
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.NameBean
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.NodeBean
import org.pillarone.riskanalytics.graph.formeditor.ui.model.palette.TypeTreeNode
import org.pillarone.riskanalytics.graph.formeditor.ui.model.treetable.FilteringTableTreeModel
import org.pillarone.riskanalytics.graph.formeditor.ui.model.treetable.GraphElementNode
import org.pillarone.riskanalytics.graph.formeditor.ui.model.treetable.NodeNameFilter
import org.pillarone.riskanalytics.graph.formeditor.ui.model.treetable.NodesTableTreeModel
import org.pillarone.riskanalytics.graph.formeditor.ui.view.dialogs.ConnectNodesDialog
import org.pillarone.riskanalytics.graph.formeditor.ui.view.dialogs.NodeEditDialog
import org.pillarone.riskanalytics.graph.formeditor.ui.view.dialogs.PortNameDialog
import org.pillarone.riskanalytics.graph.formeditor.ui.view.helpers.InfoTableTreeCellRenderer
import org.pillarone.riskanalytics.graph.formeditor.util.GraphModelUtilities
import com.ulcjava.base.application.*
import com.ulcjava.base.application.event.*
import org.pillarone.riskanalytics.graph.core.graph.model.*

/**
 *
 */
public class ComponentNodesTable extends ULCTableTree implements ISelectionListener, IModelRenameListener {

    AbstractGraphModel fGraphModel;
    FilteringTableTreeModel fTableModel;
    TreePath fRootPath;
    ApplicationContext fApplicationContext;
    List<ISelectionListener> fSelectionListeners;
    boolean fExternalSelection;
    IWatchList fWatchList;

    private IGraphModelHandler graphModelHandler; // some weak reference to the parent pane so that parts of a graph can be copied to a new tab.
    private boolean readOnly;

    public ComponentNodesTable(ApplicationContext ctx, AbstractGraphModel model, boolean readOnly) {
        this.readOnly = readOnly;
        fApplicationContext = ctx;

        NodesTableTreeModel originalModel = new NodesTableTreeModel(ctx, model)
        IComponentNodeFilter noFilter = new NoneComponentNodeFilter()
        fTableModel = new FilteringTableTreeModel(originalModel, noFilter)
        fGraphModel = model
        this.setModel(fTableModel)
        this.createDefaultColumnsFromModel()
        ClientContext.setModelUpdateMode(fTableModel, UlcEventConstants.SYNCHRONOUS_MODE)

        fRootPath = new TreePath((GraphElementNode) fTableModel.getRoot())
        this.setShowGrid(true)
        int width = ClientContext.getScreenWidth()
        int height = ClientContext.getScreenHeight()
        int preferredWidth = width / 2
        int preferredHeight = preferredWidth * height * 10 / (width * 11 * 2)
        this.setPreferredScrollableViewportSize(new Dimension(preferredWidth, preferredHeight))

        fSelectionListeners = new ArrayList<ISelectionListener>();
        this.getSelectionModel().setSelectionMode(ULCTreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION)

        setRenderer()

        addListeners()

        createContextMenu()

        if (!readOnly) {
            TransferHandler transferHandler = new TypeTransferHandler()
            this.setTransferHandler(transferHandler)
        };
    }

    public setReadOnly() {
        this.readOnly = true;
        this.setTransferHandler(null);
        this.setComponentPopupMenu(null);
        createContextMenu();
    }

    void setGraphModelHandler(IGraphModelHandler graphModelHandler) {
        this.graphModelHandler = graphModelHandler
    }

    private void setRenderer() {
        DefaultTableTreeCellRenderer defaultTableTreeCellRenderer = new DefaultTableTreeCellRenderer()
        InfoTableTreeCellRenderer infoTableTreeCellRenderer = new InfoTableTreeCellRenderer()
        for (int i = 0; i < getColumnModel().getColumnCount(); i++) {
            ULCTableTreeColumn column = getColumnModel().getColumn(i);
            if (i != NodesTableTreeModel.INFOID) {
                column.setCellRenderer(defaultTableTreeCellRenderer);
            } else {
                column.setCellRenderer(infoTableTreeCellRenderer)
                column.setMaxWidth(50)
            }
        }

    }

    public AbstractTableTreeModel getModel() {
        return fTableModel
    }

    private void addListeners() {
        addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent event) {
                modifyNodeAction()
            }
        })

        getSelectionModel().addTreeSelectionListener(new ITreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent treeSelectionEvent) {
                if (!fExternalSelection) {
                    List<ComponentNode> selectedNodes = getSelectedNodes()
                    fSelectionListeners*.each {it -> it.setSelectedComponents(selectedNodes)}
                } else {
                    fExternalSelection = false;
                }
            }
        })

        getModel().addTableTreeModelListener(new ITableTreeModelListener() {
            void tableTreeStructureChanged(TableTreeModelEvent tableTreeModelEvent) {
            }
            void tableTreeNodeStructureChanged(TableTreeModelEvent tableTreeModelEvent) {
            }
            void tableTreeNodesInserted(TableTreeModelEvent tableTreeModelEvent) {
                expandPath(fRootPath)
            }
            void tableTreeNodesRemoved(TableTreeModelEvent tableTreeModelEvent) {
            }
            void tableTreeNodesChanged(TableTreeModelEvent tableTreeModelEvent) {
            }
        })
    }

    public void addSelectionListener(ISelectionListener selectionListener) {
        fSelectionListeners.add(selectionListener);
    }

    public void removeSelectionListener(ISelectionListener selectionListener) {
        if (fSelectionListeners.contains(selectionListener)) {
            fSelectionListeners.remove(selectionListener);
        }
    }

    private void createContextMenu() {
        ULCPopupMenu nodesMenu = new ULCPopupMenu();
        ApplicationActionMap actionMap = fApplicationContext.getActionMap(this)

        if (!readOnly) {
            ULCMenuItem renameModelItem = new ULCMenuItem(("rename model"))
            ULCMenuItem editItem = new ULCMenuItem("edit node")
            ULCMenuItem connectItem = new ULCMenuItem("connect")
            ULCMenuItem replicateItem = new ULCMenuItem("replicate")
            ULCMenuItem deleteItem = new ULCMenuItem("remove")

            nodesMenu.add(renameModelItem)
            nodesMenu.addSeparator()
            nodesMenu.add(editItem)
            nodesMenu.add(connectItem)
            if (fGraphModel instanceof ComposedComponentGraphModel) {
                nodesMenu.add(replicateItem)
            }
            nodesMenu.addSeparator()
            nodesMenu.add(deleteItem)
            nodesMenu.addSeparator()

            renameModelItem.addActionListener(new IActionListener() {
                void actionPerformed(ActionEvent actionEvent) {
                    graphModelHandler.renameModel(fGraphModel)
                }
            })
            IAction editNodeAction = actionMap.get("modifyNodeAction")
            editItem.addActionListener(editNodeAction)
            this.registerKeyboardAction(editNodeAction, com.ulcjava.base.application.util.KeyStroke.getKeyStroke(com.ulcjava.base.application.event.KeyEvent.VK_ENTER, 0, true), com.ulcjava.base.application.ULCComponent.WHEN_FOCUSED)
            connectItem.addActionListener(actionMap.get("connectSelectedAction"))
            if (fGraphModel instanceof ComposedComponentGraphModel) {
                replicateItem.addActionListener(actionMap.get("replicateSelectedPortAction"))
            }
            IAction removeNodeAction = actionMap.get("removeAction")
            deleteItem.addActionListener(removeNodeAction)
            this.registerKeyboardAction(removeNodeAction, com.ulcjava.base.application.util.KeyStroke.getKeyStroke(com.ulcjava.base.application.event.KeyEvent.VK_DELETE, 0, true), com.ulcjava.base.application.ULCComponent.WHEN_FOCUSED)
        }

        ULCMenuItem addToWatchesItem = new ULCMenuItem("add to watches");
        ULCMenuItem expandItem = new ULCMenuItem("expand")
        ULCMenuItem expandAllItem = new ULCMenuItem("expand all")
        ULCMenuItem collapseItem = new ULCMenuItem("collapse")
        ULCMenuItem collapseAllItem = new ULCMenuItem("collapse all")
        ULCMenuItem showConnectedItem = new ULCMenuItem("show connected")
        ULCMenuItem clearSelectionsItem = new ULCMenuItem("clear all selections")

        nodesMenu.add(addToWatchesItem);
        nodesMenu.addSeparator()
        nodesMenu.add(expandItem)
        nodesMenu.add(expandAllItem)
        nodesMenu.add(collapseItem)
        nodesMenu.add(collapseAllItem)
        nodesMenu.addSeparator()
        nodesMenu.add(showConnectedItem)
        nodesMenu.add(clearSelectionsItem)

        addToWatchesItem.addActionListener(actionMap.get("addSelectedToWatches"));
        expandItem.addActionListener(actionMap.get("expandAction"))
        expandAllItem.addActionListener(actionMap.get("expandAllAction"))
        collapseItem.addActionListener(actionMap.get("collapseAction"))
        collapseAllItem.addActionListener(actionMap.get("collapseAllAction"));
        showConnectedItem.addActionListener(actionMap.get("showConnectedAction"))
        clearSelectionsItem.addActionListener(actionMap.get("clearSelectionAction"))

        this.setComponentPopupMenu(nodesMenu)
    }

    public void setWatchList(IWatchList watchList) {
        fWatchList = watchList;
    }

    @Action
    public void modifyNodeAction() {
        if (readOnly) return

        ComponentNode selectedNode = getSelectedNode();
        if (selectedNode != null) {
            NodeEditDialog dialog = new NodeEditDialog(UlcUtilities.getWindowAncestor(this), fGraphModel)
            dialog.setModal(true)
            dialog.setVisible(true)
            NodeBean bean = dialog.getBeanForm().getModel().getBean();
            bean.setName(UIUtils.formatDisplayName(selectedNode.getName()));
            bean.setComponentType(selectedNode.getType().getTypeClass().getName());
            bean.setComment(selectedNode.getComment());
            if (fGraphModel instanceof ModelGraphModel) {
                bean.setStarter(((ModelGraphModel) fGraphModel).getStartComponents().contains(selectedNode));
            }
            dialog.getBeanForm().getModel().setEditedNode(selectedNode);
            dialog.setEditedNode(selectedNode);
            dialog.setWatchList(fWatchList) // todo eliminate this by rather using the IGraphModelChangeListener
        }
    }

    @Action
    public void newNodeAction() {
        NodeEditDialog dialog = new NodeEditDialog(UlcUtilities.getWindowAncestor(this), fGraphModel)
        dialog.setModal(true)
        dialog.setVisible(true)
    }

    public void newNodeAction(String componentType) {
        NodeEditDialog dialog = new NodeEditDialog(UlcUtilities.getWindowAncestor(this), fGraphModel)
        dialog.setModal(true);
        dialog.setVisible(true)
        NodeBean bean = dialog.getBeanForm().getModel().getBean();
        bean.setComponentType(componentType);
    }

    /**
     * Action to create and add a connection between two ports to the graph model.
     * It is necessary that either
     * <ul>
     *     <it>exactly two ports in the tree table are selected, or</it>
     *     <it>exactly two nodes are selected.</it>
     * </ul>
     * In the latter case, a dialog is opened in which the user needs to specify the port.
     */
    @Action
    public void connectSelectedAction() {
        int[] selectedRows = this.getSelectedRows()

        if (selectedRows != null && selectedRows.length == 2) {
            List<ComponentNode> nodes = getSelectedNodes()
            if (nodes != null && nodes.size() == 2) {
                ComponentNode node1 = nodes.get(0)
                ComponentNode node2 = nodes.get(1)
                if (node1 != null && node1.hasPorts() &&
                        node2 != null && node2.hasPorts()) {
                    ConnectNodesDialog dialog = new ConnectNodesDialog(UlcUtilities.getWindowAncestor(this), fGraphModel);
                    dialog.setNodes(nodes.get(0), nodes.get(1))
                    dialog.setVisible(true)
                } else {
                    ULCAlert alert = new ULCAlert(UlcUtilities.getWindowAncestor(this), "No connections possible.",
                            "The components selected cannot be connected.", "ok")
                    alert.show()
                }
            } else {
                List<Port> ports = getSelectedPorts()
                if (ports != null && ports.size() == 2) {
                    Port p1 = ports.get(0)
                    Port p2 = ports.get(1)
                    if (!fGraphModel.isConnected(p1, p2) && p1.allowedToConnectTo(p2)) {
                        Port from
                        if (p1.getClass() != p2.getClass()) { // non-replicating
                            from = p1 instanceof InPort ? p2 : p1
                        } else { // replicating
                            if (p1 instanceof InPort) {
                                from = p1.isComposedComponentOuterPort() ? p1 : p2
                            } else {
                                from = p1.isComposedComponentOuterPort() ? p2 : p1
                            }
                        }
                        Port to = from == p1 ? p2 : p1
                        fGraphModel.createConnection(from, to)
                    } else {
                        ULCAlert alert = new ULCAlert(UlcUtilities.getWindowAncestor(this), "Ports not connected.",
                                "Ports cannot be connected or already connected.", "ok")
                        alert.show()
                    }
                } else {
                    ULCAlert alert = new ULCAlert(UlcUtilities.getWindowAncestor(this), "No connection added.",
                            "Selected exactly two components or two ports.", "ok")
                    alert.show()
                }
            }
        }
    }

    @Action
    public void replicateSelectedPortAction() {
        int[] selectedRows = this.getSelectedRows()
        if (selectedRows != null && selectedRows.length > 0) {
            ComposedComponentGraphModel ccGraphModel = (ComposedComponentGraphModel) fGraphModel
            List<Port> selectedPorts = getSelectedPorts()
            for (Port graphPort : selectedPorts) {
                if (ccGraphModel.isReplicated(graphPort)) {
                    final ULCAlert alert = new ULCAlert(UlcUtilities.getWindowAncestor(this), // parent window
                            "Port is already replicated.", // window title
                            "Do you want to introduce another replicating port?", "Yes", "No");
                    alert.addWindowListener(new IWindowListener() {
                        public void windowClosing(WindowEvent event) {
                            if (alert.getValue().equals("Yes")) {
                                showPortNameDialog(graphPort);
                            }
                        }
                    });
                    alert.show();
                } else {
                    showPortNameDialog(graphPort);
                }
            }
        }
    }

    private void showPortNameDialog(Port graphPort) {
        PortNameDialog dialog = new PortNameDialog(UlcUtilities.getWindowAncestor(this), (ComposedComponentGraphModel) fGraphModel, graphPort);
        dialog.setModal(true);
        NameBean bean = dialog.getBeanForm().getModel().getBean();
        bean.setName(UIUtils.formatDisplayName(graphPort.getName()));
        dialog.setVisible(true);
    }

    @Action
    public void removeAction() {
        Closure remove = {List<ComponentNode> nodes ->
            //remove node watches
            if (fWatchList != null) {
                for (ComponentNode node: nodes) {
                    for (OutPort p: node.getOutPorts()) {
                        String pathOfWatchToRemove = GraphModelUtilities.getPath(p, fGraphModel);
                        fWatchList.removeWatch(pathOfWatchToRemove);
                    }
                }
            }
            //remove component node
            for (ComponentNode node: nodes) {
                fGraphModel.removeComponentNode(node)
            }

            if (fGraphModel instanceof ComposedComponentGraphModel) {
                final List<Port> ports = getSelectedOuterPorts()
                if (ports != null && ports.size() > 0) {
                    ComposedComponentGraphModel ccGraphModel = (ComposedComponentGraphModel) fGraphModel
                    for (Port p: ports) {
                        ccGraphModel.removeOuterPort(p)
                    }
                }
            }
        }

        final List<ComponentNode> selectedNodes = getSelectedNodes()
        boolean componentConnected = false
        for (ComponentNode node: selectedNodes) {
            if (fGraphModel.hasConnections(node)) {
                componentConnected = true
                break
            }
        }

        if (componentConnected) {
            final ULCAlert alert = new ULCAlert(UlcUtilities.getWindowAncestor(this), "Delete Component and its connections", "At least one selected component has been connected, the component and its connections will be deleted", "Delete", "Cancel")
            alert.addWindowListener([windowClosing: {WindowEvent windowEvent ->
                if (alert.getValue().equals(alert.firstButtonLabel)) {
                    remove.call(selectedNodes)
                }
            }] as IWindowListener)
            alert.show()
        } else {
            remove.call(selectedNodes)
        }
    }

    @Action
    public void expandAction() {
        TreePath[] selectedPaths = getSelectedPaths()
        expandPaths(selectedPaths, true)
    }

    @Action
    public void expandAllAction() {
        expandAll()
    }

    @Action
    public void collapseAction() {
        TreePath[] selectedPaths = getSelectedPaths()
        collapsePaths(selectedPaths, true)
    }

    @Action
    public void collapseAllAction() {
        collapseAll()
    }

    @Action
    public void showConnectedAction() {
        try {
        List<ComponentNode> selectedNodes = getSelectedNodes()
        List<Connection> connections = fGraphModel.getEmergingConnections(selectedNodes)
        List<ComponentNode> connectedNodes = fGraphModel.getConnectedNodes(selectedNodes)
        setSelectedConnections(connections)
        setSelectedComponents(connectedNodes)
            fSelectionListeners*.each { it ->
                it.setSelectedConnections(connections)
                it.setSelectedComponents(connectedNodes)
            }
        } catch (Exception ex) {

        }
    }

    @Action
    public void clearSelectionAction() {
        try {
            clearSelection();
            for (ISelectionListener listener: fSelectionListeners) {
                listener.clearSelection();
            }
        } catch (Exception ex) {

        }
    }

    @Action
    public void addSelectedToWatches() {
        List<Port> selectedPorts = this.getSelectedPorts();
        for (Port graphPort: selectedPorts) {
            if (graphPort instanceof InPort) {
                ULCAlert alert = new ULCAlert("In-Port selected.",
                        "Only out-ports can be watched.", "ok");
                alert.show();
                return;
            }
            String path = GraphModelUtilities.getPath(graphPort, fGraphModel);
            fWatchList.addWatch(path);
        }
    }

    /////////////////////////////////////////
    // Implementation of IModelRenameListener
    /////////////////////////////////////////
    void modelRenamed(AbstractGraphModel modelWithNewName, String oldName, String oldPackageName) {
        if (fGraphModel.equals(modelWithNewName)) {
            ((GraphElementNode)fTableModel.getRoot()).setName(modelWithNewName.getName())
            
        }
    }


    /////////////////////////////////////////
    // Implementation of ISelectionListener
    /////////////////////////////////////////

    public void applyFilter(IComponentNodeFilter filter) {
        fTableModel.setFilter(filter)
    }

    public void applyFilter(NodeNameFilter filter) {
    }

    public void setSelectedComponents(List<ComponentNode> selection) {
        TreePath[] selectedPaths = fTableModel.getTreePaths(selection.toArray(new ComponentNode[0]))
        if (selectedPaths != null) {
            fExternalSelection = true
            if (selectedPaths.length == 0) {
                this.clearSelection()
            } else {
                //getSelectionModel().set
                setPathSelection(selectedPaths)
            }
        }
    }

    public void setSelectedConnections(List<Connection> selection) {
        // Nothing to do here
    }

    public void clearSelection() {
        super.clearSelection();
    }

    /////////////////////////////////////////
    // Custom methods
    /////////////////////////////////////////

    private ComponentNode getSelectedNode() {
        TreePath[] selected = getSelectedPaths();
        if (selected && selected.length > 0) {
            TreePath selected0 = selected[0];
            if (selected0.getPath().length == 2) {
                GraphElementNode node = (GraphElementNode) selected0.getPath()[1];
                if (node.getElement() instanceof ComponentNode) {
                    return (ComponentNode) node.getElement();
                }
            }
        }
        return null;
    }

    /**
     * @return the selected nodes or <code>null</code> if no row is selected
     */
    private List<ComponentNode> getSelectedNodes() {
        TreePath[] selected = getSelectedPaths();
        List<ComponentNode> nodes = new ArrayList<ComponentNode>();
        if (selected && selected.length > 0) {
            for (TreePath path: selected) {
                if (path.getPath().length == 2) {
                    GraphElementNode node = (GraphElementNode) path.getPath()[1];
                    if (node.getElement() instanceof ComponentNode) {
                        nodes.add((ComponentNode) node.getElement());
                    }
                }
            }
        }
        return nodes;
    }

    private List<Port> getSelectedPorts() {
        TreePath[] selected = getSelectedPaths();
        List<Port> ports = new ArrayList<Port>();
        if (selected && selected.length > 0) {
            for (TreePath path: selected) {
                GraphElementNode node = (GraphElementNode) path.getLastPathComponent();
                if (node.getElement() instanceof Port) {
                    ports.add((Port) node.getElement());
                }
            }
        }
        return ports;
    }

    private List<Port> getSelectedOuterPorts() {
        List<Port> ports = getSelectedPorts();
        List<Port> outerPorts = new ArrayList<Port>();
        if (ports != null && ports.size() > 0) {
            for (Port p: ports) {
                if (p.isComposedComponentOuterPort()) {
                    outerPorts.add(p);
                }
            }
        }
        return outerPorts;
    }

    private ComponentNode getComponentNode(TreePath treePath) {
        if (treePath != null && treePath.getPathCount() == 2 && treePath.getPath()[1] instanceof ComponentNode) {
            return (ComponentNode) treePath.getPath()[1];
        }
        return null;
    }

    private List<ComponentNode> getComponentNodes(TreePath[] treePaths) {
        List<ComponentNode> nodes = new ArrayList<ComponentNode>();
        if (treePaths != null) {
            for (TreePath path: treePaths) {
                ComponentNode node = getComponentNode(path);
                if (node != null) {
                    nodes.add(node);
                }
            }
        }
        return nodes;
    }

    private class TypeTransferHandler extends TransferHandler {

        @Override
        public boolean importData(ULCComponent targetComponent, Transferable transferable) {
            Object dragData0 = transferable.getTransferData(DataFlavor.DRAG_FLAVOR);
            DnDTreeData dragData = (DnDTreeData) dragData0;
            TreePath[] paths = dragData.getTreePaths();
            Object selected = paths[0].getLastPathComponent();
            if (selected instanceof TypeTreeNode) {
                String componentType = ((TypeTreeNode) selected).getFullName();
                if (componentType != null) {
                    newNodeAction(componentType);
                    return true;
                }
            }
            return false;
        }

        /**
         * Do nothing on the export side
         */
        @Override
        public void exportDone(ULCComponent src, Transferable t, int action) {
        }
    }
}
