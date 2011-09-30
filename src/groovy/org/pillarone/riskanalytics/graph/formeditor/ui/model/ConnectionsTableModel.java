package org.pillarone.riskanalytics.graph.formeditor.ui.model;

import com.ulcjava.applicationframework.application.ApplicationContext;
import com.ulcjava.applicationframework.application.ResourceMap;
import com.ulcjava.applicationframework.application.form.model.FormModel;
import com.ulcjava.base.application.table.AbstractTableModel;
import com.ulcjava.base.application.table.ITableModel;
import org.pillarone.riskanalytics.graph.core.graph.model.*;
import org.pillarone.riskanalytics.graph.core.graph.model.filters.IComponentNodeFilter;
import org.pillarone.riskanalytics.graph.formeditor.util.UIUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Model underlying the connections table in the form editor.
 * It refers directly to an instance of the {@link AbstractGraphModel} from which
 * the list of all connections is obtained.
 * <p/>
 * The table listens to changes in the graph model through a {@link IGraphModelChangeListener}.
 *
 * @author martin.melchior
 */
public class ConnectionsTableModel extends AbstractTableModel implements ITableModel {

    static final int FROMID = 0;
    static final int TOID = 1;

    private ApplicationContext fContext;
    private AbstractGraphModel fGraphModel;
    private String[] fColumnNames;
    private List<Connection> fFilteredConnectionList;
    IComponentNodeFilter fActiveFilter;

    /**
     * @param ctx   is used basically to access the presentation of the table headers.
     * @param model the graph model used as basis for the table model.
     */
    public ConnectionsTableModel(ApplicationContext ctx, AbstractGraphModel model) {
        super();
        fContext = ctx;
        fGraphModel = model;
        fColumnNames = getColumnNames();
        final IGraphModelChangeListener graphListener = new GraphListener();
        model.addGraphModelChangeListener(graphListener);
    }

    private String[] getColumnNames() {
        ResourceMap modelMap = fContext.getResourceMap(ConnectionFormModel.class, FormModel.class);
        ResourceMap resourceMap = fContext.getResourceMap(getClass(), AbstractTableModel.class, modelMap);
        String[] columnHeaders = new String[2];
        columnHeaders[FROMID] = resourceMap.getString("from.columnHeader");
        columnHeaders[TOID] = resourceMap.getString("to.columnHeader");
        return columnHeaders;
    }

    public void applyFilter(IComponentNodeFilter filter) {
        fFilteredConnectionList = new ArrayList<Connection>();
        fFilteredConnectionList.addAll(fGraphModel.getAllConnections());
        if (filter != null) {
            fFilteredConnectionList = filter.filterConnectionsList(fFilteredConnectionList);
        }
        fActiveFilter = filter;
        fireTableDataChanged();
    }

    private List<Connection> getFilteredConnectionList() {
        if (fFilteredConnectionList==null) {
            applyFilter(null);
        }
        return fFilteredConnectionList;
    }

    /**
     * Returns just the number of connections found in the graph model.
     */
    public int getRowCount() {
        return getFilteredConnectionList().size();
    }

    /**
     * Contains exactly two columns:
     * <ul>
     * <li>from</li>
     * <li>to</li>
     * </ul>
     */
    public int getColumnCount() {
        return 2;
    }

    /**
     * Returns the name of the from or the to port respectively.
     * Note that the connections table contains not just the names of the from and the to ports.
     * In a prefix, the name of the components the ports are attached to is included.
     */
    public Object getValueAt(int row, int column) {
        Connection c = getFilteredConnectionList().get(row);
        switch (column) {
            case FROMID:
                return UIUtils.getConnectionEntryName(c.getFrom());
            case TOID:
                return UIUtils.getConnectionEntryName(c.getTo());
            default:
                return null;
        }
    }

    /**
     * Return the column class so that the table knows how to render the vales.
     * Here trivial - could be omitted.
     */
    @Override
    public Class getColumnClass(int columnIndex) {
        return String.class;
    }

    /**
     * Return the name of the column with index <code>col</code> - loaded from the application context.
     */
    @Override
    public String getColumnName(int column) {
        return fColumnNames[column];
    }

    public List<Integer> getRows(Connection[] connections) {
        List<Integer> rowList = new ArrayList<Integer>();
        for (Connection c : connections) {
            int row = getFilteredConnectionList().indexOf(c);
            if (row >= 0 && !rowList.contains(row)) {
                rowList.add(row);
            }
        }
        Collections.sort(rowList);
        return rowList;
    }

    public Connection getConnection(int row) {
        return getFilteredConnectionList().get(row);
    }

    private class GraphListener implements IGraphModelChangeListener {

        public void connectionAdded(Connection c) {
            if (fActiveFilter == null || fActiveFilter.isSelected(c)) {
                fFilteredConnectionList.add(c);
                int position = fFilteredConnectionList.size()-1;
                fireTableRowsInserted(position,position);
            }
        }

        public void connectionRemoved(Connection c) {
            if (fActiveFilter == null || fActiveFilter.isSelected(c)) {
                int position = fFilteredConnectionList.indexOf(c);
                fFilteredConnectionList.remove(position);
                fireTableRowsDeleted(position, position);
            }
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

        public void nodePropertyChanged(ComponentNode node, String propertyName, Object oldValue, Object newValue) {
            fireTableDataChanged(); // TODO could be optimized
        }
    }
}
