package org.pillarone.riskanalytics.graph.formeditor.ui.model;

import com.ulcjava.applicationframework.application.ApplicationContext;
import com.ulcjava.applicationframework.application.ResourceMap;
import com.ulcjava.applicationframework.application.form.model.FormModel;
import com.ulcjava.base.application.table.AbstractTableModel;
import com.ulcjava.base.application.table.ITableModel;
import org.pillarone.riskanalytics.graph.core.graph.model.*;

import java.util.List;

/**
 * Model underlying the nodes table in the form editor.
 * It refers directly to an instance of the {@link AbstractGraphModel} from which
 * the list of all components is obtained.
 * <p/>
 * The table listens to changes in the graph model through a {@link IGraphModelChangeListener}.
 * <p/>
 * For {@link ComposedComponentGraphModel}'s the table consists of 3 columns (name, type, comment),
 * for {@link ModelGraphModel}'s the table contains an additional boolean column to indicate whether a given
 * component is a starter component.
 *
 * @author martin.melchior
 */
public class NodesTableModel extends AbstractTableModel implements ITableModel {

    static final int NAMEID = 0;
    static final int TYPEID = 1;

    private ApplicationContext fContext;
    private AbstractGraphModel fGraphModel;
    private String[] fColumnNames;

    /**
     * @param ctx   is used basically to access the presentation of the table headers.
     * @param model the graph model used as basis for the table model.
     */
    public NodesTableModel(com.ulcjava.applicationframework.application.ApplicationContext ctx, AbstractGraphModel model) {
        super();
        fContext = ctx;
        fGraphModel = model;
        fColumnNames = getColumnNames();
        addGraphModelListeners();
    }

    private void addGraphModelListeners() {
        fGraphModel.addGraphModelChangeListener(
                new IGraphModelChangeListener() {
                    public void connectionAdded(Connection c) {
                    }

                    public void connectionRemoved(Connection c) {
                    }

                    public void nodeAdded(ComponentNode node) {
                        fireTableDataChanged();
                    }

                    public void nodeRemoved(ComponentNode node) {
                        fireTableDataChanged();
                    }

                    public void outerPortAdded(Port p) {
                        // Nothing to do here
                    }

                    public void outerPortRemoved(Port p) {
                        // Nothing to do here
                    }

                    public void nodesSelected(List<ComponentNode> nodes) {
                        // Nothing to do here
                    }

                    public void connectionsSelected(List<Connection> connections) {
                        // Nothing to do here
                    }

                    public void selectionCleared() {
                        // Nothing to do here
                    }

                    public void filtersApplied() {
                        reset();
                    }

                    public void nodePropertyChanged(ComponentNode node, String propertyName, Object oldValue, Object newValue) {
                        // Nothing to do here
                    }
                });
    }

    private String[] getColumnNames() {
        ResourceMap modelMap = fContext.getResourceMap(NodeFormModel.class, FormModel.class);
        ResourceMap resourceMap = fContext.getResourceMap(getClass(), AbstractTableModel.class, modelMap);
        String[] columnHeaders = new String[2]; 
        columnHeaders[NAMEID] = resourceMap.getString("name.columnHeader");
        columnHeaders[TYPEID] = resourceMap.getString("componentType.columnHeader");
        return columnHeaders;
    }

    /**
     * The number of component nodes found in the graph model.
     */
    public int getRowCount() {
        return fGraphModel.getFilteredComponentsList().size();
    }

    /**
     * Three or four columns depending whether the underlying model is a {@link ModelGraphModel} or
     * a {@link ComposedComponentGraphModel}.
     */
    public int getColumnCount() {
        return 2;
        // return fGraphModel instanceof ModelGraphModel ? 4 : 3;
    }

    public Object getValueAt(int row, int column) {
        ComponentNode node = fGraphModel.getFilteredComponentsList().get(row);
        switch (column) {
            case NAMEID:
                return node.getName();
            case TYPEID:
                return node.getType().getTypeClass().getName();
            default:
                return null;
        }
    }

    /**
     * Only the fourth column is editable which indicates whether a given component is a starter component.
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    /**
     * Only the fourth column is a {@link Boolean} - all the others are of type {@link String}.
     */
    @SuppressWarnings("rawtypes")
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

    public void reset() {
        fireTableDataChanged();
    }
}
