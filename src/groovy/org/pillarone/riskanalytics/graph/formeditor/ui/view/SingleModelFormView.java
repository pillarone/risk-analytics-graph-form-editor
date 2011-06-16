package org.pillarone.riskanalytics.graph.formeditor.ui.view;


import com.ulcjava.applicationframework.application.AbstractBean;
import com.ulcjava.applicationframework.application.Action;
import com.ulcjava.applicationframework.application.ApplicationActionMap;
import com.ulcjava.applicationframework.application.ApplicationContext;
import com.ulcjava.base.application.*;
import com.ulcjava.base.application.event.*;
import com.ulcjava.base.application.tree.TreePath;
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
    //private ULCTable fNodesTable;
    private ULCTableTree fNodesTable;
    private ULCTable fConnectionsTable;

    private NodeEditDialog fNodeEditDialog;
    private ConnectNodesDialog fConnectNodesDialog;
    private ConnectionEditDialog fConnectionEditForm;
    private ReplicationEditDialog fReplicationEditForm;

    private boolean fNodesSelected;
    private boolean fTwoNodesSelected;
    private boolean fConnectionSelected;


    public SingleModelFormView(ApplicationContext ctx, AbstractGraphModel model) {
        super();
        fMainView = new ULCBoxPane(true, 1);
        fApplicationContext = ctx;
        injectGraphModel(model);
        setVisible(true);
    }

    public void createView() {
        fNodesTable = new NodesTable(fApplicationContext, fGraphModel);
        fNodesSelected = false;
        fTwoNodesSelected = false;
        fNodesTable.setVisible(true);
        ULCScrollPane nodesScrollPane = new ULCScrollPane(fNodesTable);
        ULCBoxPane nodesPane = new ULCBoxPane(true);
        nodesPane.setBorder(BorderFactory.createTitledBorder("Components"));
        nodesPane.add(ULCBoxPane.BOX_EXPAND_EXPAND, nodesScrollPane);

        fConnectionsTable = new ConnectionsTable(fApplicationContext, fGraphModel);
        fConnectionSelected = false;
        ULCScrollPane connScrollPane = new ULCScrollPane(fConnectionsTable);
        ULCBoxPane connPane = new ULCBoxPane(true);
        connPane.setBorder(BorderFactory.createTitledBorder("Connections"));
        connPane.add(ULCBoxPane.BOX_EXPAND_EXPAND, connScrollPane);

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
        createView();
        addListeners();
        addNodesContextMenu();
        addConnectionsContextMenu();
    }

    @SuppressWarnings("serial")
    private void addListeners() {
        fNodesTable.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent event) {
                modifyNodeAction();
            }
        });
        
        /*fNodesTable.getSelectionModel().addListSelectionListener(new IListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                int[] selectedRows = fNodesTable.getSelectedRows();
                setNodesSelected(selectedRows.length > 0);
                setTwoNodesSelected(selectedRows.length == 2);
            }
        });*/

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

    private void addNodesContextMenu() {
        ULCPopupMenu menu = new ULCPopupMenu();
        ApplicationActionMap actionMap = getActionMap();

        ULCMenuItem addItem = new ULCMenuItem("add");
        addItem.addActionListener(actionMap.get("newNodeAction"));
        menu.add(addItem);

        ULCMenuItem connectItem = new ULCMenuItem("connect");
        connectItem.addActionListener(actionMap.get("connectSelectedNodesAction"));
        menu.add(connectItem);

        menu.addSeparator();

        ULCMenuItem deleteItem = new ULCMenuItem("remove");
        deleteItem.addActionListener(actionMap.get("removeNodeAction"));
        menu.add(deleteItem);

        menu.addSeparator();

        ULCMenuItem showConnectedItem = new ULCMenuItem("show connected");
        showConnectedItem.addActionListener(actionMap.get("showConnectedElementsAction"));
        menu.add(showConnectedItem);

        ULCMenuItem clearSelectionsItem = new ULCMenuItem("clear selections");
        clearSelectionsItem.addActionListener(actionMap.get("clearSelectionsAction"));
        menu.add(clearSelectionsItem);

        /*menu.addSeparator();

        ULCMenuItem addParameterSetItem = new ULCMenuItem("create parameter set");
        clearSelectionsItem.addActionListener(actionMap.get("createParametersAction"));
        menu.add(clearSelectionsItem);*/

        fNodesTable.setComponentPopupMenu(menu);
    }

    private void addConnectionsContextMenu() {
        ULCPopupMenu menu = new ULCPopupMenu();
        ApplicationActionMap actionMap = getActionMap();

        ULCMenuItem addItem = new ULCMenuItem("add");
        addItem.addActionListener(actionMap.get("newConnectionAction"));
        menu.add(addItem);

        if (fGraphModel instanceof ComposedComponentGraphModel) {
            ULCMenuItem replicateItem = new ULCMenuItem("replicate");
            replicateItem.addActionListener(actionMap.get("newReplicationAction"));
            menu.add(replicateItem);
        }

        menu.addSeparator();

        ULCMenuItem deleteItem = new ULCMenuItem("remove");
        deleteItem.addActionListener(actionMap.get("removeConnectionAction"));
        menu.add(deleteItem);

        menu.addSeparator();

        ULCMenuItem showAttachedNodesItem = new ULCMenuItem("show connected");
        showAttachedNodesItem.addActionListener(actionMap.get("showAttachedNodesAction"));
        menu.add(showAttachedNodesItem);

        ULCMenuItem clearSelectionsItem = new ULCMenuItem("clear selections");
        clearSelectionsItem.addActionListener(actionMap.get("clearSelectionsAction"));
        menu.add(clearSelectionsItem);

        fConnectionsTable.setComponentPopupMenu(menu);
    }


    private void showNodeEditDialog(boolean show) {
        if (fNodeEditDialog == null) {
            fNodeEditDialog = new NodeEditDialog(UlcUtilities.getWindowAncestor(fNodesTable), fGraphModel);
        }
        fNodeEditDialog.setModal(true);
        fNodeEditDialog.setVisible(show);
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
        if (fConnectionEditForm == null) {
            fConnectionEditForm = new ConnectionEditDialog(UlcUtilities.getWindowAncestor(fConnectionsTable), fGraphModel);
        }
        fConnectionEditForm.setVisible(true);
    }

    private void showReplicationDialog() {
        if (fGraphModel instanceof ComposedComponentGraphModel) {
            if (fReplicationEditForm == null) {
                fReplicationEditForm = new ReplicationEditDialog(UlcUtilities.getWindowAncestor(fConnectionsTable), (ComposedComponentGraphModel) fGraphModel);
            }
            fReplicationEditForm.setVisible(true);
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
        /*int[] selectedRows = fNodesTable.getSelectedRows();
        if (selectedRows.length > 0) {
            int index = fNodesTable.convertRowIndexToModel(selectedRows[0]);
            return fGraphModel.getAllComponentNodes().get(index);
        } else {
            return null;
        }*/
        TreePath[] selected = fNodesTable.getSelectedPaths();
        if (selected.length > 0) {
            TreePath selected0 = selected[0];
            if (selected0.getPath().length==2 && selected0.getPath()[1] instanceof ComponentNode) {
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
        if (selected.length > 0 ) {
            List<ComponentNode> nodes = new ArrayList<ComponentNode>();
            for (TreePath path : selected) {
                if (path.getPath().length==2 && path.getPath()[1] instanceof ComponentNode) {
                    nodes.add((ComponentNode) path.getPath()[1]);
                }
            }
            return nodes;
        }
        return null;
    }

    protected List<Port> getSelectedPorts() {
        TreePath[] selected = fNodesTable.getSelectedPaths();
        if (selected.length > 0 ) {
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
        if (fNodeEditDialog == null || !fNodeEditDialog.isVisible()) {
            showNodeEditDialog(true);
        }
        fNodeEditDialog.getBeanForm().getModel().getBean().reset();
        fNodeEditDialog.getBeanForm().getModel().setEditedNode(null);
        fNodeEditDialog.setEditedNode(null);
    }

    public void newNodeAction(String componentType) {
        if (fNodeEditDialog == null || !fNodeEditDialog.isVisible()) {
            showNodeEditDialog(true);
        }
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
            if (fNodeEditDialog == null || !fNodeEditDialog.isVisible()) {
                showNodeEditDialog(true);
            }
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
            if (nodes != null && nodes.size()==2) {
                if (fConnectNodesDialog == null || !fConnectNodesDialog.isVisible()) {
                    showConnectNodesDialog(nodes.get(0), nodes.get(1));
                }
            } else {
                List<Port> ports = getSelectedPorts();
                if (ports != null && ports.size()==2) {
                    Port p1 = ports.get(0);
                    Port p2 = ports.get(1);
                    if (p1.allowedToConnectTo(p2)) {
                        Port from;
                        if (p1.getClass()!=p2.getClass()) { // non-replicating
                            from = p1 instanceof InPort ? p2 : p1;
                        } else { // replicating
                            if (p1 instanceof InPort) {
                                from = p1.isComposedComponentOuterPort() ? p1 : p2;
                            } else {
                                from = p1.isComposedComponentOuterPort() ? p2 : p1;
                            }
                        }
                        Port to = from==p1 ? p2 : p1;
                        fGraphModel.createConnection(from, to);
                    }
                }
            }
        }
    }

    @SuppressWarnings("serial")
    @Action(enabledProperty = "nodesSelected")
    public void removeNodeAction() {
        final List<ComponentNode> nodes = getSelectedNodes();
        boolean hasNoConnections = true;
        for (ComponentNode node : nodes) {
            hasNoConnections = hasNoConnections && !fGraphModel.isConnected(node);
        }
        if (!hasNoConnections) {
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
    }

    private void removeNodes(List<ComponentNode> nodes) {
        for (ComponentNode node : nodes) {
            fGraphModel.removeComponentNode(node);
        }
    }

    @Action
    public void newConnectionAction() {
        if (fConnectionEditForm == null || !fConnectionEditForm.isVisible()) {
            showNewConnectionDialog();
        }
        fConnectionEditForm.getBeanForm().getModel().getBean().reset();
    }

    @Action(enabledProperty = "isModel")
    public void newReplicationAction() {
        if (fReplicationEditForm == null || !fReplicationEditForm.isVisible()) {
            showReplicationDialog();
        }
        fReplicationEditForm.getBeanForm().getModel().getBean().reset();
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
                if (fConnectionEditForm == null || !fConnectionEditForm.isVisible()) {
                    showNewConnectionDialog();
                }
                ConnectionBean bean = fConnectionEditForm.getBeanForm().getModel().getBean();
                bean.setFrom(GraphModelUtilities.getPortName(selectedConnection.getFrom()));
                bean.setFrom(GraphModelUtilities.getPortName(selectedConnection.getTo()));
            } else {
                if (fReplicationEditForm == null || !fReplicationEditForm.isVisible()) {
                    showReplicationDialog();
                }
                ReplicationBean bean = fReplicationEditForm.getBeanForm().getModel().getBean();
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
