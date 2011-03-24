package org.pillarone.riskanalytics.graph.formeditor.ui.model;

import com.ulcjava.applicationframework.application.ApplicationContext;
import com.ulcjava.applicationframework.application.ResourceMap;
import com.ulcjava.applicationframework.application.form.model.FormModel;
import com.ulcjava.base.application.table.AbstractTableModel;
import com.ulcjava.base.application.table.ITableModel;
import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.model.ComponentNode;
import org.pillarone.riskanalytics.graph.core.graph.model.Connection;
import org.pillarone.riskanalytics.graph.core.graph.model.IGraphModelChangeListener;
import org.pillarone.riskanalytics.graph.formeditor.util.GraphModelUtilities;

/**
 * Model underlying the connections table in the form editor.
 * It refers directly to an instance of the {@link AbstractGraphModel} from which 
 * the list of all connections is obtained. 
 * 
 * The table listens to changes in the graph model through a {@link IGraphModelChangeListener}.
 * @author martin.melchior
 */
public class ConnectionsTableModel extends AbstractTableModel implements ITableModel {

	static final int FROMID = 0;
	static final int TOID = 1;
	
	private ApplicationContext fContext;
	private AbstractGraphModel fGraphModel;
	private String[] fColumnNames;
	
	/**
	 * @param ctx is used basically to access the presentation of the table headers.
	 * @param model the graph model used as basis for the table model.
	 */
	public ConnectionsTableModel(ApplicationContext ctx, AbstractGraphModel model) {
		super();
		fContext = ctx;
		fGraphModel = model;
		addGraphModelListeners();
		fColumnNames = getColumnNames();		
	}
	
    private void addGraphModelListeners() {
    	fGraphModel.addGraphModelChangeListener(
                new IGraphModelChangeListener() {
                    public void nodeAdded(ComponentNode node) {
                    }

                    public void nodeRemoved(ComponentNode node) {
                    }

                    public void connectionAdded(Connection c) {
                        fireTableDataChanged();
                    }

                    public void connectionRemoved(Connection node) {
                        fireTableDataChanged();
                    }
                });
    }
    
    private String[] getColumnNames() {
        ResourceMap modelMap = fContext.getResourceMap(ConnectionFormModel.class, FormModel.class);
        ResourceMap resourceMap = fContext.getResourceMap(getClass(), AbstractTableModel.class, modelMap);    	
    	String[] columnHeaders = new String[2];
    	columnHeaders[FROMID] = resourceMap.getString("from.columnHeader");
    	columnHeaders[TOID] = resourceMap.getString("to.columnHeader");
    	return columnHeaders;
    }

    /**
     * Returns just the number of connections found in the graph model.
     */
	public int getRowCount() {
		return fGraphModel.getAllConnections().size();
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
		Connection c = fGraphModel.getAllConnections().get(row);
		switch (column) {
			case FROMID: return GraphModelUtilities.getPortName(c.getFrom());
			case TOID: return GraphModelUtilities.getPortName(c.getTo());
			default: return null;
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
}