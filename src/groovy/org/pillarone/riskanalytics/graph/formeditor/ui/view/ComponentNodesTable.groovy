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
import com.ulcjava.base.application.ULCPopupMenu
import com.ulcjava.base.application.ULCMenuItem
import com.ulcjava.applicationframework.application.ApplicationActionMap
import com.ulcjava.base.application.IAction
import com.ulcjava.base.application.util.KeyStroke
import com.ulcjava.base.application.event.KeyEvent
import com.ulcjava.base.application.ULCComponent
import com.ulcjava.applicationframework.application.Action
import com.ulcjava.base.application.ULCAlert
import com.ulcjava.base.application.UlcUtilities
import com.ulcjava.base.application.event.IWindowListener
import com.ulcjava.base.application.event.WindowEvent
import com.ulcjava.base.application.event.IActionListener
import com.ulcjava.base.application.event.ActionEvent
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.NodeBean
import com.ulcjava.base.application.dnd.TransferHandler
import com.ulcjava.base.application.dnd.Transferable
import com.ulcjava.base.application.dnd.DataFlavor
import com.ulcjava.base.application.dnd.DnDTreeData
import org.pillarone.riskanalytics.graph.formeditor.ui.model.palette.TypeTreeNode

/**
 *
 */
public class ComponentNodesTable extends ULCTableTree implements ISelectionListener {

    AbstractGraphModel fGraphModel;
    FilteringTableTreeModel fTableModel;
    ApplicationContext fApplicationContext;
    List<ISelectionListener> fSelectionListeners;
    boolean fExternalSelection;

    public ComponentNodesTable(ApplicationContext ctx, AbstractGraphModel model) {
        fApplicationContext = ctx;

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

        fSelectionListeners = new ArrayList<ISelectionListener>();
        this.getSelectionModel().setSelectionMode(ULCTreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION)
        this.setSelectionBackground(Color.yellow)

        ULCTableTreeColumn col = this.getColumnModel().getColumn(NodesTableTreeModel.INFOID)
        DefaultTableTreeCellRenderer renderer = new InfoTableTreeCellRenderer()
        col.setCellRenderer(renderer)
        col.setMaxWidth(50)

        addListeners()

        createContextMenu()

        TransferHandler transferHandler = new TypeTransferHandler();
        this.setTransferHandler(transferHandler);
    }

    public AbstractTableTreeModel getModel() {
        return fTableModel
    }

    private void addListeners() {
        addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent event) {
                modifyNodeAction();
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

        ULCMenuItem addItem = new ULCMenuItem("add node")
        addItem.addActionListener(actionMap.get("newNodeAction"))
        nodesMenu.add(addItem)

        ULCMenuItem editItem = new ULCMenuItem("edit node")
        IAction editNodeAction = actionMap.get("modifyNodeAction");
        editItem.addActionListener(editNodeAction);
        this.registerKeyboardAction(editNodeAction, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true), ULCComponent.WHEN_FOCUSED);
        nodesMenu.add(editItem)

        ULCMenuItem connectItem = new ULCMenuItem("connect")
        connectItem.addActionListener(actionMap.get("connectSelectedAction"))
        nodesMenu.add(connectItem)

        if (fGraphModel instanceof ComposedComponentGraphModel) {
            ULCMenuItem replicateItem = new ULCMenuItem("replicate")
            replicateItem.addActionListener(actionMap.get("replicateSelectedPortAction"))
            nodesMenu.add(replicateItem)
        }

        nodesMenu.addSeparator()

        ULCMenuItem deleteItem = new ULCMenuItem("remove")
        IAction removeNodeAction = actionMap.get("removeAction")
        deleteItem.addActionListener(removeNodeAction)
        this.registerKeyboardAction(removeNodeAction, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, true), ULCComponent.WHEN_FOCUSED)
        nodesMenu.add(deleteItem)

        nodesMenu.addSeparator()

        ULCMenuItem expandItem = new ULCMenuItem("expand")
        expandItem.addActionListener(actionMap.get("expandAction"))
        nodesMenu.add(expandItem)
        ULCMenuItem expandAllItem = new ULCMenuItem("expand all")
        expandAllItem.addActionListener(actionMap.get("expandAllAction"))
        nodesMenu.add(expandAllItem)
        ULCMenuItem collapseItem = new ULCMenuItem("collapse")
        collapseItem.addActionListener(actionMap.get("collapseAction"))
        nodesMenu.add(collapseItem)
        ULCMenuItem collapseAllItem = new ULCMenuItem("collapse all")
        collapseAllItem.addActionListener(actionMap.get("collapseAllAction"));
        nodesMenu.add(collapseAllItem)

        nodesMenu.addSeparator()

        ULCMenuItem showConnectedItem = new ULCMenuItem("show connected")
        showConnectedItem.addActionListener(actionMap.get("showConnectedAction"))
        nodesMenu.add(showConnectedItem)

        ULCMenuItem clearSelectionsItem = new ULCMenuItem("clear all selections")
        clearSelectionsItem.addActionListener(actionMap.get("clearSelectionAction"))
        nodesMenu.add(clearSelectionsItem)

        this.setComponentPopupMenu(nodesMenu)
    }

    @Action
    public void modifyNodeAction() {
        ComponentNode selectedNode = getSelectedNode();
        if (selectedNode != null) {
            NodeEditDialog dialog = new NodeEditDialog(UlcUtilities.getWindowAncestor(this), fGraphModel)
            dialog.setModal(true)
            dialog.setVisible(true)
            NodeBean bean = dialog.getBeanForm().getModel().getBean();
            bean.setName(selectedNode.getName());
            bean.setComponentType(selectedNode.getType().getTypeClass().getName());
            bean.setComment(selectedNode.getComment());
            if (fGraphModel instanceof ModelGraphModel) {
                bean.setStarter(((ModelGraphModel) fGraphModel).getStartComponents().contains(selectedNode));
            }
            dialog.getBeanForm().getModel().setEditedNode(selectedNode);
            dialog.setEditedNode(selectedNode);
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
                    ULCAlert alert = new ULCAlert("No connections possible.",
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
                        ULCAlert alert = new ULCAlert("Ports not connected.",
                                "Ports cannot be connected or already connected.", "ok")
                        alert.show()
                    }
                } else {
                    ULCAlert alert = new ULCAlert("No connection added.",
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
            List<Port> ports = getSelectedPorts()
            if (ports != null && ports.size() == 1) {
                Port p = ports.get(0);
                Class packetType = p.getPacketType()
                ComposedComponentGraphModel ccGraphModel = (ComposedComponentGraphModel) fGraphModel
                if (ccGraphModel.isReplicated(p)) {
                    ULCAlert alert = new ULCAlert("Port already replicated.",
                            "Port is already replicated.", "ok")
                    alert.show()
                } else if (p instanceof InPort) {
                    InPort inPort = (InPort) p
                    InPort outerInPort = ccGraphModel.createOuterInPort(packetType, p.getName())
                    ccGraphModel.createConnection(outerInPort, inPort)
                } else {
                    OutPort outPort = (OutPort) p
                    OutPort outerOutPort = ccGraphModel.createOuterOutPort(packetType, p.getName())
                    ccGraphModel.createConnection(outPort, outerOutPort)
                }
            }
        }
    }

    @Action
    public void removeAction() {
        final List<ComponentNode> nodes = getSelectedNodes()
        for (ComponentNode node : nodes) {
            fGraphModel.removeComponentNode(node)
        }

        if (fGraphModel instanceof ComposedComponentGraphModel) {
            final List<Port> ports = getSelectedOuterPorts()
            if (ports != null && ports.size() > 0) {
                ComposedComponentGraphModel ccGraphModel = (ComposedComponentGraphModel) fGraphModel
                for (Port p : ports) {
                    ccGraphModel.removeOuterPort(p)
                }
            }
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
        List<ComponentNode> selectedNodes = getSelectedNodes()
        List<Connection> connections = fGraphModel.getEmergingConnections(selectedNodes)
        List<ComponentNode> connectedNodes = fGraphModel.getConnectedNodes(selectedNodes)
        setSelectedConnections(connections)
        setSelectedComponents(connectedNodes)
        fSelectionListeners*.each { it ->
            it.setSelectedConnections(connections)
            it.setSelectedComponents(connectedNodes)
        }
    }

    @Action
    public void clearSelectionAction() {
        clearSelection();
        for (ISelectionListener listener : fSelectionListeners) {
            listener.clearSelection();
        }
    }

    /////////////////////////////////////////
    // Implementation of ISelectionListener
    /////////////////////////////////////////

    public void applyFilter(IComponentNodeFilter filter) {
        fTableModel.setFilter(filter)
    }

    public void setSelectedComponents(List<ComponentNode> selection) {
        TreePath[] selectedPaths = fTableModel.getTreePaths(selection.toArray(new ComponentNode[0]))
        if (selectedPaths != null) {
            fExternalSelection = true
            if (selectedPaths.length==0) {
                this.clearSelection()
            } else {
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
        if (selected.length > 0) {
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
            for (TreePath path : selected) {
                if (path.getPath().length == 2) {
                    GraphElementNode node = (GraphElementNode)path.getPath()[1];
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
            for (TreePath path : selected) {
                GraphElementNode node = (GraphElementNode)path.getLastPathComponent();
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
            for (Port p : ports) {
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
            for (TreePath path : treePaths) {
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
                if (componentType != null ) {
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
