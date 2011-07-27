package org.pillarone.riskanalytics.graph.formeditor.ui.view;


import com.ulcjava.applicationframework.application.AbstractBean;
import com.ulcjava.applicationframework.application.Action;
import com.ulcjava.applicationframework.application.ApplicationActionMap;
import com.ulcjava.applicationframework.application.ApplicationContext;
import com.ulcjava.base.application.*;
import com.ulcjava.base.application.event.*;
import com.ulcjava.base.application.tree.TreePath;
import com.ulcjava.base.application.util.KeyStroke;
import org.pillarone.riskanalytics.graph.core.graph.model.*;
import org.pillarone.riskanalytics.graph.formeditor.ui.handlers.TypeTransferHandler;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.ConnectionBean;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.NodeBean;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.ReplicationBean;
import org.pillarone.riskanalytics.graph.formeditor.util.GraphModelUtilities;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

public class SingleModelFormView extends AbstractBean implements GraphModelEditable, GraphModelViewable {
    private ApplicationContext fApplicationContext;

    private AbstractGraphModel fGraphModel;
    private boolean fIsModel;

    private ULCBoxPane fMainView;
    private ULCScrollPane fNodesPane;
    private ULCTableTree fNodesTable;

    private ULCScrollPane fConnectionsPane;
    private ULCTable fConnectionsTable;

    private NodeEditDialog fNodeEditDialog;
    private ConnectNodesDialog fConnectNodesDialog;
    private ConnectionEditDialog fConnectionEditDialog;
    private ReplicationEditDialog fReplicationEditDialog;

    private boolean fNodesSelected;
    private boolean fTwoNodesSelected;
    private boolean fConnectionSelected;


    public SingleModelFormView(ApplicationContext ctx) {
        super();
        fMainView = new ULCBoxPane(true, 1);
        fApplicationContext = ctx;
        createView();
    }

    public void createView() {
        fNodesPane = new ULCScrollPane();
        ULCBoxPane nodesPane = new ULCBoxPane(true);
        nodesPane.setBorder(BorderFactory.createTitledBorder("Components"));
        nodesPane.add(ULCBoxPane.BOX_EXPAND_EXPAND, fNodesPane);

        fConnectionsPane = new ULCScrollPane();
        ULCBoxPane connPane = new ULCBoxPane(true);
        connPane.setBorder(BorderFactory.createTitledBorder("Connections"));
        connPane.add(ULCBoxPane.BOX_EXPAND_EXPAND, fConnectionsPane);

        ULCSplitPane splitPane = new ULCSplitPane(ULCSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(nodesPane);
        splitPane.setRightComponent(connPane);
        splitPane.setDividerLocation(0.5);
        splitPane.setDividerSize(10);
        //splitPane.setDividerLocationAnimationEnabled(true);
        fMainView.add(ULCBoxPane.BOX_EXPAND_EXPAND, splitPane);
    }

    public void setTransferHandler(TypeTransferHandler transferHandler) {
        transferHandler.setModelEditView(this);
        fNodesTable.setTransferHandler(transferHandler);
    }

    public ULCBoxPane getView() {
        return fMainView;
    }

    public void setVisible(boolean visible) {
        if (fMainView != null) {
            fMainView.setVisible(visible);
        }
    }

    public void injectGraphModel(AbstractGraphModel model) {
        fGraphModel = model;
        fIsModel = model instanceof ModelGraphModel;
        fNodesSelected = false;
        fTwoNodesSelected = false;

        fNodesTable = new NodesTable(fApplicationContext, fGraphModel);
        fNodesTable.setVisible(true);
        fNodesPane.setViewPortView(fNodesTable);

        fConnectionsTable = new ConnectionsTable(fApplicationContext, fGraphModel);
        fConnectionSelected = false;
        fConnectionsPane.setViewPortView(fConnectionsTable);

        addListeners();
        createNodesContextMenu();
        createConnectionsContextMenu();
    }

    @SuppressWarnings("serial")
    private void addListeners() {
        fNodesTable.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent event) {
                modifyNodeAction();
            }
        });
        fNodesTable.getSelectionModel().addTreeSelectionListener(new ITreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent treeSelectionEvent) {
                int[] selectedRows = fNodesTable.getSelectedRows();
                setNodesSelected(selectedRows != null && selectedRows.length > 0);
                setTwoNodesSelected(selectedRows != null && selectedRows.length == 2);

            }
        });
        fConnectionsTable.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent event) {
                modifyConnectionAction();
            }
        });
        fConnectionsTable.getSelectionModel().addListSelectionListener(new IListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                setConnectionSelected(fConnectionsTable.getSelectedRows().length > 0);
            }
        });
    }

    private void createNodesContextMenu() {
        ULCPopupMenu nodesMenu = new ULCPopupMenu();
        ApplicationActionMap actionMap = getActionMap();

        ULCMenuItem addItem = new ULCMenuItem("add");
        addItem.addActionListener(actionMap.get("newNodeAction"));
        nodesMenu.add(addItem);

        ULCMenuItem connectItem = new ULCMenuItem("connect");
        connectItem.addActionListener(actionMap.get("connectSelectedNodesAction"));
        nodesMenu.add(connectItem);

        if (fGraphModel instanceof ComposedComponentGraphModel) {
            ULCMenuItem replicateItem = new ULCMenuItem("replicate");
            replicateItem.addActionListener(actionMap.get("replicateSelectedPortAction"));
            nodesMenu.add(replicateItem);
        }

        nodesMenu.addSeparator();

        ULCMenuItem deleteItem = new ULCMenuItem("remove");
        IAction removeNodeAction = actionMap.get("removeNodeAction");
        deleteItem.addActionListener(removeNodeAction);
        fNodesTable.registerKeyboardAction(removeNodeAction, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, true), ULCComponent.WHEN_FOCUSED);
        nodesMenu.add(deleteItem);

        nodesMenu.addSeparator();

        ULCMenuItem expandItem = new ULCMenuItem("expand");
        expandItem.addActionListener(actionMap.get("expandAction"));
        nodesMenu.add(expandItem);
        ULCMenuItem expandAllItem = new ULCMenuItem("expand all");
        expandAllItem.addActionListener(actionMap.get("expandAllAction"));
        nodesMenu.add(expandAllItem);
        ULCMenuItem collapseItem = new ULCMenuItem("collapse");
        collapseItem.addActionListener(actionMap.get("collapseAction"));
        nodesMenu.add(collapseItem);
        ULCMenuItem collapseAllItem = new ULCMenuItem("collapse all");
        collapseAllItem.addActionListener(actionMap.get("collapseAllAction"));
        nodesMenu.add(collapseAllItem);

        nodesMenu.addSeparator();

        ULCMenuItem showConnectedItem = new ULCMenuItem("show connected");
        showConnectedItem.addActionListener(actionMap.get("showConnectedElementsAction"));
        nodesMenu.add(showConnectedItem);

        ULCMenuItem clearSelectionsItem = new ULCMenuItem("clear selections");
        clearSelectionsItem.addActionListener(actionMap.get("clearSelectionsAction"));
        nodesMenu.add(clearSelectionsItem);

        /*menu.addSeparator();

        ULCMenuItem addParameterSetItem = new ULCMenuItem("create parameter set");
        clearSelectionsItem.addActionListener(actionMap.get("createParametersAction"));
        menu.add(clearSelectionsItem);*/

        fNodesTable.setComponentPopupMenu(nodesMenu);
    }

    private void createConnectionsContextMenu() {
        ULCPopupMenu connectionsMenu = new ULCPopupMenu();
        ApplicationActionMap actionMap = getActionMap();

        ULCMenuItem addItem = new ULCMenuItem("add");
        addItem.addActionListener(actionMap.get("newConnectionAction"));
        connectionsMenu.add(addItem);

        if (fGraphModel instanceof ComposedComponentGraphModel) {
            ULCMenuItem replicateItem = new ULCMenuItem("replicate");
            replicateItem.addActionListener(actionMap.get("newReplicationAction"));
            connectionsMenu.add(replicateItem);
        }

        connectionsMenu.addSeparator();

        ULCMenuItem deleteItem = new ULCMenuItem("remove");
        IAction removeConnectionAction = actionMap.get("removeConnectionAction");
        deleteItem.addActionListener(removeConnectionAction);
        fConnectionsTable.registerKeyboardAction(removeConnectionAction, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, true), ULCComponent.WHEN_FOCUSED);
        connectionsMenu.add(deleteItem);

        connectionsMenu.addSeparator();

        ULCMenuItem showAttachedNodesItem = new ULCMenuItem("show connected");
        showAttachedNodesItem.addActionListener(actionMap.get("showAttachedNodesAction"));
        connectionsMenu.add(showAttachedNodesItem);

        ULCMenuItem clearSelectionsItem = new ULCMenuItem("clear selections");
        clearSelectionsItem.addActionListener(actionMap.get("clearSelectionsAction"));
        connectionsMenu.add(clearSelectionsItem);

        fConnectionsTable.setComponentPopupMenu(connectionsMenu);
    }


    private void showNodeEditDialog() {
        if (fNodeEditDialog == null) {
            fNodeEditDialog = new NodeEditDialog(UlcUtilities.getWindowAncestor(fNodesTable), fGraphModel);
            fNodeEditDialog.setModal(true);
        }
        fNodeEditDialog.setVisible(true);
    }

    private void showConnectNodesDialog(ComponentNode node1, ComponentNode node2) {
        if (fConnectNodesDialog == null) {
            fConnectNodesDialog = new ConnectNodesDialog(UlcUtilities.getWindowAncestor(fNodesTable), fGraphModel);
        }
        if (node1 != null && node1.hasPorts() &&
                node2 != null && node2.hasPorts()) {
            fConnectNodesDialog.setNodes(node1, node2);
            fConnectNodesDialog.setVisible(true);
        } else {
            ULCAlert alert = new ULCAlert("Connection not created.",
                    "At least one of the selected nodes does not contain ports. Connection cannot be created.", "ok");
            alert.show();
        }
    }

    private void showNewConnectionDialog() {
        if (fConnectionEditDialog == null) {
            fConnectionEditDialog = new ConnectionEditDialog(UlcUtilities.getWindowAncestor(fConnectionsTable), fGraphModel);
        }
        fConnectionEditDialog.setVisible(true);
    }

    private void showReplicationDialog() {
        if (fGraphModel instanceof ComposedComponentGraphModel) {
            if (fReplicationEditDialog == null) {
                fReplicationEditDialog = new ReplicationEditDialog(UlcUtilities.getWindowAncestor(fConnectionsTable), (ComposedComponentGraphModel) fGraphModel);
            }
            fReplicationEditDialog.setVisible(true);
        } else {
            ULCAlert alert = new ULCAlert("Port replication not possible.",
                    "Port replication not possible for models.", "ok");
            alert.show();
        }
    }

    public boolean isNodesSelected() {
        return fNodesSelected;
    }

    public void setNodesSelected(boolean selectionAvailable) {
        if (fNodesSelected != selectionAvailable) {
            fNodesSelected = selectionAvailable;
            firePropertyChange(new PropertyChangeEvent(this, "nodesSelected", !fNodesSelected, fNodesSelected));
        }
    }

    public boolean isTwoNodesSelected() {
        return fTwoNodesSelected;
    }

    public void setTwoNodesSelected(boolean twoNodesSelected) {
        if (fTwoNodesSelected != twoNodesSelected) {
            fTwoNodesSelected = twoNodesSelected;
            firePropertyChange(new PropertyChangeEvent(this, "twoNodesSelected", !fTwoNodesSelected, fTwoNodesSelected));
        }
    }

    public boolean isConnectionSelected() {
        return fConnectionSelected;
    }

    public void setConnectionSelected(boolean selectionAvailable) {
        if (fConnectionSelected != selectionAvailable) {
            fConnectionSelected = selectionAvailable;
            firePropertyChange(new PropertyChangeEvent(this, "connectionSelected", !fConnectionSelected, fConnectionSelected));
        }
    }

    /**
     * @return the selected node or <code>null</code> if no row is selected
     */
    protected ComponentNode getSelectedNode() {
        TreePath[] selected = fNodesTable.getSelectedPaths();
        if (selected.length > 0) {
            TreePath selected0 = selected[0];
            if (selected0.getPath().length == 2 && selected0.getPath()[1] instanceof ComponentNode) {
                return (ComponentNode) selected0.getPath()[1];
            }
        }
        return null;
    }

    /**
     * @return the selected nodes or <code>null</code> if no row is selected
     */
    protected List<ComponentNode> getSelectedNodes() {
        TreePath[] selected = fNodesTable.getSelectedPaths();
        if (selected.length > 0) {
            List<ComponentNode> nodes = new ArrayList<ComponentNode>();
            for (TreePath path : selected) {
                if (path.getPath().length == 2 && path.getPath()[1] instanceof ComponentNode) {
                    nodes.add((ComponentNode) path.getPath()[1]);
                }
            }
            return nodes;
        }
        return null;
    }

    protected List<Port> getSelectedPorts() {
        TreePath[] selected = fNodesTable.getSelectedPaths();
        if (selected.length > 0) {
            List<Port> ports = new ArrayList<Port>();
            for (TreePath path : selected) {
                if (path.getLastPathComponent() instanceof Port) {
                    ports.add((Port) path.getLastPathComponent());
                }
            }
            return ports;
        }
        return null;
    }

    protected List<Port> getSelectedOuterPorts() {
        List<Port> ports = getSelectedPorts();
        if (ports != null && ports.size() > 0) {
            List<Port> outerPorts = new ArrayList<Port>();
            for (Port p : ports) {
                if (p.isComposedComponentOuterPort()) {
                    outerPorts.add(p);
                }
            }
            return outerPorts;
        }
        return null;
    }

    /**
     * @return the selected bean or <code>null</code> if no row is selected
     */
    protected Connection getSelectedConnection() {
        int[] selectedRows = fConnectionsTable.getSelectedRows();
        if (selectedRows.length > 0) {
            int index = fConnectionsTable.convertRowIndexToModel(selectedRows[0]);
            return fGraphModel.getAllConnections().get(index);
        } else {
            return null;
        }
    }

    @Action
    public void newNodeAction() {
        showNodeEditDialog();
        fNodeEditDialog.getBeanForm().getModel().getBean().reset();
        fNodeEditDialog.getBeanForm().getModel().setEditedNode(null);
        fNodeEditDialog.setEditedNode(null);
    }

    public void newNodeAction(String componentType) {
        showNodeEditDialog();
        NodeBean bean = fNodeEditDialog.getBeanForm().getModel().getBean();
        bean.reset();
        bean.setComponentType(componentType);
        fNodeEditDialog.getBeanForm().getModel().setEditedNode(null);
        fNodeEditDialog.setEditedNode(null);
    }

    @Action(enabledProperty = "nodesSelected")
    public void modifyNodeAction() {
        ComponentNode selectedNode = getSelectedNode();
        if (selectedNode != null) {
            showNodeEditDialog();
            NodeBean bean = fNodeEditDialog.getBeanForm().getModel().getBean();
            bean.setName(selectedNode.getName());
            bean.setComponentType(selectedNode.getType().getTypeClass().getName());
            bean.setComment(selectedNode.getComment());
            if (fIsModel) {
                bean.setStarter(((ModelGraphModel) fGraphModel).getStartComponents().contains(selectedNode));
            }
            fNodeEditDialog.getBeanForm().getModel().setEditedNode(selectedNode);
            fNodeEditDialog.setEditedNode(selectedNode);
        }
    }

    @SuppressWarnings("serial")
    @Action(enabledProperty = "twoNodesSelected")
    public void connectSelectedNodesAction() {
        if (isTwoNodesSelected()) {
            List<ComponentNode> nodes = getSelectedNodes();
            if (nodes != null && nodes.size() == 2) {
                if (fConnectNodesDialog == null || !fConnectNodesDialog.isVisible()) {
                    showConnectNodesDialog(nodes.get(0), nodes.get(1));
                }
            } else {
                List<Port> ports = getSelectedPorts();
                if (ports != null && ports.size() == 2) {
                    Port p1 = ports.get(0);
                    Port p2 = ports.get(1);
                    if (!fGraphModel.isConnected(p1, p2) && p1.allowedToConnectTo(p2)) {
                        Port from;
                        if (p1.getClass() != p2.getClass()) { // non-replicating
                            from = p1 instanceof InPort ? p2 : p1;
                        } else { // replicating
                            if (p1 instanceof InPort) {
                                from = p1.isComposedComponentOuterPort() ? p1 : p2;
                            } else {
                                from = p1.isComposedComponentOuterPort() ? p2 : p1;
                            }
                        }
                        Port to = from == p1 ? p2 : p1;
                        fGraphModel.createConnection(from, to);
                    } else {
                        ULCAlert alert = new ULCAlert("Ports not connected.",
                                "Ports cannot be connected or already connected.", "ok");
                        alert.show();
                    }
                }
            }
        }
    }

    @Action(enabledProperty = "nodesSelected")
    public void replicateSelectedPortAction() {
        if (isNodesSelected()) {
            List<Port> ports = getSelectedPorts();
            if (ports != null && ports.size() == 1) {
                Port p = ports.get(0);
                Class packetType = p.getPacketType();
                ComposedComponentGraphModel ccGraphModel = (ComposedComponentGraphModel) fGraphModel;
                if (ccGraphModel.isReplicated(p)) {
                    ULCAlert alert = new ULCAlert("Port already replicated.",
                            "Port is already replicated.", "ok");
                    alert.show();
                } else {
                    if (p instanceof InPort) {
                        InPort inPort = (InPort) p;
                        InPort outerInPort = ccGraphModel.createOuterInPort(packetType, p.getName());
                        ccGraphModel.addOuterPort(outerInPort);
                        ccGraphModel.createConnection(outerInPort, inPort);
                    } else {
                        OutPort outPort = (OutPort) p;
                        OutPort outerOutPort = ccGraphModel.createOuterOutPort(packetType, p.getName());
                        ccGraphModel.addOuterPort(outerOutPort);
                        ccGraphModel.createConnection(outPort, outerOutPort);
                    }
                }
            }
        }
    }

    @SuppressWarnings("serial")
    @Action(enabledProperty = "nodesSelected")
    public void removeNodeAction() {
        final List<ComponentNode> nodes = getSelectedNodes();
        if (fGraphModel.hasConnections(nodes)) {
            final ULCAlert alert = new ULCAlert(
                    "Remove Component Node",
                    "The removal of this component node will also remove associated connections. Proceed?",
                    "Yes",
                    "No");
            alert.addWindowListener(new IWindowListener() {
                public void windowClosing(WindowEvent event) {
                    if (alert.getValue().equals("Yes")) {
                        removeNodes(nodes);
                    }
                }
            });
            alert.show();
        } else {
            removeNodes(nodes);
        }

        if (fGraphModel instanceof ComposedComponentGraphModel) {
            final List<Port> ports = getSelectedOuterPorts();
            if (ports != null && ports.size() > 0) {
                ComposedComponentGraphModel ccGraphModel = (ComposedComponentGraphModel) fGraphModel;
                boolean hasNoReplication = true;
                for (Port p : ports) {
                    ccGraphModel.removeOuterPort(p);
                }
            }
        }
    }

    private void removeNodes(List<ComponentNode> nodes) {
        for (ComponentNode node : nodes) {
            fGraphModel.removeComponentNode(node);
        }
    }

    @Action
    public void newConnectionAction() {
        if (fConnectionEditDialog == null || !fConnectionEditDialog.isVisible()) {
            showNewConnectionDialog();
        }
        fConnectionEditDialog.getBeanForm().getModel().getBean().reset();
    }

    @Action(enabledProperty = "isModel")
    public void newReplicationAction() {
        if (fReplicationEditDialog == null || !fReplicationEditDialog.isVisible()) {
            showReplicationDialog();
        }
        fReplicationEditDialog.getBeanForm().getModel().getBean().reset();
    }

    @SuppressWarnings("serial")
    @Action(enabledProperty = "nodesSelected")
    public void expandAction() {
        TreePath[] selectedPaths = fNodesTable.getSelectedPaths();
        fNodesTable.expandPaths(selectedPaths, true);
    }

    @SuppressWarnings("serial")
    @Action(enabledProperty = "nodesSelected")
    public void expandAllAction() {
        fNodesTable.expandAll();
    }

    @SuppressWarnings("serial")
    @Action(enabledProperty = "nodesSelected")
    public void collapseAction() {
        TreePath[] selectedPaths = fNodesTable.getSelectedPaths();
        fNodesTable.collapsePaths(selectedPaths, true);
    }

    @SuppressWarnings("serial")
    @Action(enabledProperty = "nodesSelected")
    public void collapseAllAction() {
        fNodesTable.collapseAll();
    }

    @Action
    public void showAttachedNodesAction() {
        List<Connection> connections = fGraphModel.getSelectedConnections();
        List<ComponentNode> connectedNodes = fGraphModel.getAttachedNodes(connections);
        fGraphModel.setSelectedNodes(connectedNodes, this);
    }

    @Action(enabledProperty = "nodesSelected")
    public void showConnectedElementsAction() {
        List<ComponentNode> selectedNodes = getSelectedNodes();
        List<Connection> connections = fGraphModel.getEmergingConnections(selectedNodes);
        fGraphModel.setSelectedConnections(connections, this);
        List<ComponentNode> connectedNodes = fGraphModel.getConnectedNodes(selectedNodes);
        fGraphModel.setSelectedNodes(connectedNodes, this);
    }

    @Action
    public void clearSelectionsAction() {
        fGraphModel.clearSelections();
    }

    @Action(enabledProperty = "connectionSelected")
    public void modifyConnectionAction() {
        Connection selectedConnection = getSelectedConnection();
        if (selectedConnection != null) {
            if (!selectedConnection.isReplicatingConnection()) {
                if (fConnectionEditDialog == null || !fConnectionEditDialog.isVisible()) {
                    showNewConnectionDialog();
                }
                ConnectionBean bean = fConnectionEditDialog.getBeanForm().getModel().getBean();
                bean.setFrom(GraphModelUtilities.getPortName(selectedConnection.getFrom()));
                bean.setFrom(GraphModelUtilities.getPortName(selectedConnection.getTo()));
            } else {
                if (fReplicationEditDialog == null || !fReplicationEditDialog.isVisible()) {
                    showReplicationDialog();
                }
                ReplicationBean bean = fReplicationEditDialog.getBeanForm().getModel().getBean();
                String fromPortName = GraphModelUtilities.getPortName(selectedConnection.getFrom());
                String toPortName = GraphModelUtilities.getPortName(selectedConnection.getTo());
                if (fromPortName.split(".").length == 1) {
                    bean.setInner(toPortName);
                    bean.setOuter(fromPortName);
                } else {
                    bean.setInner(fromPortName);
                    bean.setOuter(toPortName);
                }
            }
        }
    }

    @Action(enabledProperty = "connectionSelected")
    public void removeConnectionAction() {
        Connection connection = getSelectedConnection();
        if (connection != null) {
            fGraphModel.removeConnection(connection);
        }
    }

    private ApplicationActionMap getActionMap() {
        return fApplicationContext.getActionMap(this);
    }
}
