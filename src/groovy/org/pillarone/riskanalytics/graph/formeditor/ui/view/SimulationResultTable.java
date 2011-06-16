package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.base.application.ClientContext;
import com.ulcjava.base.application.ULCTableTree;
import com.ulcjava.base.application.util.Dimension;
import org.pillarone.riskanalytics.core.simulation.item.Parameterization;
import org.pillarone.riskanalytics.graph.core.graph.model.ModelGraphModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.DataTableTreeModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.SimulationResultDataTreeModel;

import java.util.Map;

/**
 * 
 */
public class SimulationResultTable extends ULCTableTree {

    SimulationResultDataTreeModel fTableModel;


    public SimulationResultTable(Map simulationOutput) {
        fTableModel = new SimulationResultDataTreeModel(simulationOutput);
        this.setModel(fTableModel);
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
}
