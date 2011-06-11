package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.base.application.ClientContext;
import com.ulcjava.base.application.ULCTableTree;
import com.ulcjava.base.application.util.Dimension;
import org.pillarone.riskanalytics.core.simulation.item.Parameterization;
import org.pillarone.riskanalytics.graph.core.graph.model.ModelGraphModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.DataTableTreeModel;

/**
 * 
 */
public class DataTable extends ULCTableTree {

    ModelGraphModel fGraphModel;
    DataTableTreeModel fTableModel;


    public DataTable(ModelGraphModel model, int periodCount, String dataObjectName) {
        fTableModel = new DataTableTreeModel(model, periodCount, dataObjectName);
        this.setModel(fTableModel);
        fGraphModel = model;
        fGraphModel.addGraphModelChangeListener(fTableModel);
        createView();
    }

    public DataTable(ModelGraphModel model, Parameterization parametrization) {
        fTableModel = new DataTableTreeModel(model, parametrization);
        this.setModel(fTableModel);
        fGraphModel = model;
        fGraphModel.addGraphModelChangeListener(fTableModel);
        createView();
    }

    public void createView() {
        this.createDefaultColumnsFromModel();
        this.setShowGrid(true);
        int width = ClientContext.getScreenWidth();
        int height = ClientContext.getScreenHeight();
        int preferredWidth = width / 2;
        int preferredHeight = preferredWidth * height * 10 / (width * 11 * 4);
        this.setPreferredScrollableViewportSize(new Dimension(preferredWidth, preferredHeight));
    }

    public DataTableTreeModel getModel() {
        return fTableModel;
    }
}
