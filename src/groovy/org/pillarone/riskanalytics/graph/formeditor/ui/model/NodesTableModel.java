package org.pillarone.riskanalytics.graph.formeditor.ui.model;

import com.ulcjava.applicationframework.application.ApplicationContext;
import com.ulcjava.applicationframework.application.ResourceMap;
import com.ulcjava.applicationframework.application.form.model.FormModel;
import com.ulcjava.base.application.table.AbstractTableModel;
import com.ulcjava.base.application.table.ITableModel;
import org.pillarone.riskanalytics.graph.core.graph.model.*;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.filters.ComponentNodeFilterFactory;

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
public class NodesTableModel extends AbstractTableModel implements ITableModel, IFilterChangedListener {

    static final int NAMEID = 0;
    static final int TYPEID = 1;
    //static final int COMMENTID = 2;
    //static final int STARTERID = 3;

    private ApplicationContext fContext;
    private AbstractGraphModel fGraphModel;
    private String[] fColumnNames;
    private IComponentNodeFilter fNodeFilter;

    /**
     * @param ctx   is used basically to access the presentation of the table headers.
     * @param model the graph model used as basis for the table model.
     */
    public NodesTableModel(com.ulcjava.applicationframework.application.ApplicationContext ctx, AbstractGraphModel model) {
        super();
        fContext = ctx;
        fGraphModel = model;
        fColumnNames = getColumnNames();
        fNodeFilter = ComponentNodeFilterFactory.getFilter("None",null);
        fNodeFilter.setGraphModel(fGraphModel);
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
                });
    }

    private String[] getColumnNames() {
        ResourceMap modelMap = fContext.getResourceMap(NodeFormModel.class, FormModel.class);
        ResourceMap resourceMap = fContext.getResourceMap(getClass(), AbstractTableModel.class, modelMap);
        boolean isModel = fGraphModel instanceof ModelGraphModel;
        String[] columnHeaders = new String[2]; //new String[isModel ? 4 : 3];
        columnHeaders[NAMEID] = resourceMap.getString("name.columnHeader");
        columnHeaders[TYPEID] = resourceMap.getString("componentType.columnHeader");
        /*columnHeaders[COMMENTID] = resourceMap.getString("comment.columnHeader");
        if (isModel) {
            columnHeaders[STARTERID] = resourceMap.getString("starter.columnHeader");
        }*/
        return columnHeaders;
    }

    /**
     * The number of component nodes found in the graph model.
     */
    public int getRowCount() {
        return fNodeFilter.getFilteredComponentNodeList().size();
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
        ComponentNode node = fNodeFilter.getFilteredComponentNodeList().get(row);
        switch (column) {
            case NAMEID:
                return node.getName();
            case TYPEID:
                return node.getType().getTypeClass().getName();
            /*case COMMENTID:
                return node.getComment();
            case STARTERID:
                return fGraphModel instanceof ModelGraphModel ? ((ModelGraphModel) fGraphModel).getStartComponents().contains(node) : false;*/
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
        //return fGraphModel instanceof ModelGraphModel && columnIndex == STARTERID;
    }

    /**
     * Only the fourth column is a {@link Boolean} - all the others are of type {@link String}.
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Class getColumnClass(int columnIndex) {
        return String.class;
        //return columnIndex == STARTERID ? Boolean.class : String.class;
    }

    /**
     * Return the name of the column with index <code>col</code> - loaded from the application context.
     */
    @Override
    public String getColumnName(int column) {
        return fColumnNames[column];
    }

    public void applyFilter(IComponentNodeFilter filter) {
        fNodeFilter = filter;
        fireTableDataChanged();
    }
}
