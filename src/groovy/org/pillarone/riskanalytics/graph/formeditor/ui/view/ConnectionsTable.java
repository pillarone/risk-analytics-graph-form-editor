package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.applicationframework.application.ApplicationContext;
import com.ulcjava.base.application.ClientContext;
import com.ulcjava.base.application.ULCListSelectionModel;
import com.ulcjava.base.application.ULCTable;
import com.ulcjava.base.application.event.IListSelectionListener;
import com.ulcjava.base.application.event.ListSelectionEvent;
import com.ulcjava.base.application.table.TableRowSorter;
import com.ulcjava.base.application.util.Color;
import com.ulcjava.base.application.util.Dimension;
import org.pillarone.riskanalytics.graph.core.graph.model.*;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.ConnectionsTableModel;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class ConnectionsTable extends ULCTable {

    ConnectionsTableModel fTableModel;
    AbstractGraphModel fGraphModel;

    public ConnectionsTable(ApplicationContext ctx, AbstractGraphModel model) {
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
        final IGraphModelChangeListener graphListener = new GraphListener();
        model.addGraphModelChangeListener(graphListener);
        this.getSelectionModel().addListSelectionListener(
            new IListSelectionListener() {
                public void valueChanged(ListSelectionEvent listSelectionEvent) {
                    int[] rows = getSelectedRows();
                    List<Connection> connections = getConnections(rows);
                    fGraphModel.setSelectedConnections(connections, graphListener);
                }
            }
        );
        this.setSelectionBackground(Color.green);
    }

    private class GraphListener implements IGraphModelChangeListener {

        public void connectionAdded(Connection c) {
            fTableModel.fireTableDataChanged();
        }

        public void connectionRemoved(Connection c) {
            fTableModel.fireTableDataChanged();
        }

        public void nodeAdded(ComponentNode node) {
            // nothing to do
        }

        public void nodeRemoved(ComponentNode node) {
            // nothing to do
        }

        public void outerPortAdded(Port p) {
            // nothing to do
        }

        public void outerPortRemoved(Port p) {
            // nothing to do
        }

        public void nodesSelected(List<ComponentNode> nodes) {
            // nothing to do here
        }

        public void connectionsSelected(List<Connection> connections) {
            clearSelection();
            setSelectedRows(getRowIndices(connections));
        }

        public void selectionCleared() {
            clearSelection();
        }

        public void filtersApplied() {
            fTableModel.fireTableStructureChanged();
        }

        public void nodePropertyChanged(ComponentNode node, String propertyName, Object oldValue, Object newValue) {
            fTableModel.fireTableDataChanged();
        }
    }

    public List<Integer> getRowIndices(List<Connection> connections) {
        return fTableModel.getRows(connections.toArray(new Connection[0]));
    }

    public List<Connection> getConnections(int[] rows) {
        List<Connection> connections = new ArrayList<Connection>();
        if (rows != null) {
            for (int row : rows) {
                connections.add(fTableModel.getConnection(row));
            }
        }
        return connections;
    }

    public void setSelectedRows(List<Integer> rowIndices) {
        if (rowIndices.size()>0) {
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