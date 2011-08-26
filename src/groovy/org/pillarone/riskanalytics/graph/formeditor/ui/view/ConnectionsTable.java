package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.applicationframework.application.Action;
import com.ulcjava.applicationframework.application.ApplicationActionMap;
import com.ulcjava.applicationframework.application.ApplicationContext;
import com.ulcjava.base.application.*;
import com.ulcjava.base.application.event.*;
import com.ulcjava.base.application.table.TableRowSorter;
import com.ulcjava.base.application.util.Color;
import com.ulcjava.base.application.util.Dimension;
import com.ulcjava.base.application.util.KeyStroke;
import org.pillarone.riskanalytics.graph.core.graph.model.*;
import org.pillarone.riskanalytics.graph.core.graph.model.filters.IComponentNodeFilter;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.ConnectionsTableModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.ConnectionBean;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.ReplicationBean;
import org.pillarone.riskanalytics.graph.formeditor.util.GraphModelUtilities;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class ConnectionsTable extends ULCTable implements ISelectionListener {

    ConnectionsTableModel fTableModel;
    AbstractGraphModel fGraphModel;
    ApplicationContext fApplicationContext;
    List<ISelectionListener> fSelectionListeners;

    public ConnectionsTable(ApplicationContext ctx, AbstractGraphModel model) {
        fApplicationContext = ctx;

        fTableModel = new ConnectionsTableModel(ctx, model);
        fGraphModel = model;
        this.setModel(fTableModel);
        this.getSelectionModel().setSelectionMode(ULCListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        this.setRowSorter(new TableRowSorter(fTableModel));

        int width = ClientContext.getScreenWidth();
        int height = ClientContext.getScreenHeight();
        int preferredWidth = width /2;
        int preferredHeight = preferredWidth * height * 10 / (width * 11 * 2);
        this.setPreferredScrollableViewportSize(new Dimension(preferredWidth, preferredHeight));

        fSelectionListeners = new ArrayList<ISelectionListener>();

        addListeners();

        createContextMenu();
    }

    @SuppressWarnings("serial")
    private void addListeners() {
        this.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent event) {
                modifyConnectionAction();
            }
        });
        this.getSelectionModel().addListSelectionListener(new IListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                List<Connection> selectedConnections = getSelectedConnections();
                for (ISelectionListener listener : fSelectionListeners) {
                    listener.setSelectedConnections(selectedConnections);
                }
            }
        });
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
        ULCPopupMenu connectionsMenu = new ULCPopupMenu();
        ApplicationActionMap actionMap = fApplicationContext.getActionMap(this);

        ULCMenuItem editItem = new ULCMenuItem("edit");
        IAction editConnectionAction = actionMap.get("modifyConnectionAction");
        editItem.addActionListener(editConnectionAction);
        this.registerKeyboardAction(editConnectionAction, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true), ULCComponent.WHEN_FOCUSED);
        connectionsMenu.add(editItem);

        ULCMenuItem deleteItem = new ULCMenuItem("remove");
        IAction removeConnectionAction = actionMap.get("removeConnectionAction");
        deleteItem.addActionListener(removeConnectionAction);
        this.registerKeyboardAction(removeConnectionAction, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, true), ULCComponent.WHEN_FOCUSED);
        connectionsMenu.add(deleteItem);

        connectionsMenu.addSeparator();

        ULCMenuItem showAttachedNodesItem = new ULCMenuItem("show connected");
        showAttachedNodesItem.addActionListener(actionMap.get("showAttachedNodesAction"));
        connectionsMenu.add(showAttachedNodesItem);

        ULCMenuItem clearSelectionItem = new ULCMenuItem("clear all selections");
        clearSelectionItem.addActionListener(actionMap.get("clearSelectionAction"));
        connectionsMenu.add(clearSelectionItem);

        this.setComponentPopupMenu(connectionsMenu);
    }

    @Action
    public void removeConnectionAction() {
        List<Connection> connections = getSelectedConnections();
        for (Connection c : connections) {
            fGraphModel.removeConnection(c);
        }
    }

    @Action
    public void showAttachedNodesAction() {
        List<Connection> connections = getSelectedConnections();
        List<ComponentNode> connectedNodes = fGraphModel.getAttachedNodes(connections);
        for (ISelectionListener listener : fSelectionListeners) {
            listener.setSelectedComponents(connectedNodes);
        }
    }

    @Action
    public void clearSelectionAction() {
        clearSelection();
        for (ISelectionListener listener : fSelectionListeners) {
            listener.clearSelection();
        }
    }

    @Action
    public void modifyConnectionAction() {
        int[] selectedRows = getSelectedRows();
        if (selectedRows != null && selectedRows.length==1) {
            Connection selectedConnection = getSelectedConnections().get(0);
            if (!selectedConnection.isReplicatingConnection()) {
                ConnectionEditDialog dialog = new ConnectionEditDialog(UlcUtilities.getWindowAncestor(this), fGraphModel);
                ConnectionBean bean = dialog.getBeanForm().getModel().getBean();
                bean.setFrom(GraphModelUtilities.getPortName(selectedConnection.getFrom()));
                bean.setFrom(GraphModelUtilities.getPortName(selectedConnection.getTo()));
            } else {
                ReplicationEditDialog dialog = new ReplicationEditDialog(UlcUtilities.getWindowAncestor(this), (ComposedComponentGraphModel) fGraphModel);
                dialog.setVisible(true);
                ReplicationBean bean = dialog.getBeanForm().getModel().getBean();
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

    /////////////////////////////////////////
    // Implementation of ISelectionListener
    /////////////////////////////////////////

    public void applyFilter(IComponentNodeFilter filter) {
        fTableModel.applyFilter(filter);
    }

    public void setSelectedComponents(List<ComponentNode> selection) {
        // Nothing to do here
    }

    public void setSelectedConnections(List<Connection> selection) {
        setSelectedRows(getRowIndices(selection));
    }

    public void clearSelection() {
        super.clearSelection();
    }

    /////////////////////////////////////////
    // Custom methods
    /////////////////////////////////////////

    private List<Connection> getSelectedConnections() {
        int[] selectedRows = getSelectedRows();
        List<Connection> selectedConnection = new ArrayList<Connection>();
        if (selectedRows != null && selectedRows.length > 0) {
            List<Connection> allConnections = fGraphModel.getAllConnections();
            for (int i : selectedRows) {
                int index = convertRowIndexToModel(i);
                selectedConnection.add(allConnections.get(index));
            }
        }
        return selectedConnection;
    }

    private List<Integer> getRowIndices(List<Connection> connections) {
        return fTableModel.getRows(connections.toArray(new Connection[0]));
    }

    private List<Connection> getConnections(int[] rowIndices) {
        List<Connection> connections = new ArrayList<Connection>();
        if (rowIndices != null) {
            for (int row : rowIndices) {
                connections.add(fTableModel.getConnection(row));
            }
        }
        return connections;
    }

    private void setSelectedRows(List<Integer> rowIndices) {
        if (rowIndices != null) {
            if (rowIndices.size() == 0) {
                this.clearSelection();
            } else {
                int lower = rowIndices.get(0);
                int upper = lower;
                for (int i = 1; i < rowIndices.size(); i++) {
                    int row = rowIndices.get(i);
                    if (row > upper+1) {
                        this.addRowSelectionInterval(lower, upper);
                        lower = row;
                        upper = lower;
                    } else {
                        upper = row;
                    }
                }
                this.addRowSelectionInterval(lower, upper);
             }
        }
    }
}