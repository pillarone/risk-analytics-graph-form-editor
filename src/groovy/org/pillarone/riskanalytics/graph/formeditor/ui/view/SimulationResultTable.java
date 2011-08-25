package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.base.application.ClientContext;
import com.ulcjava.base.application.ULCMenuItem;
import com.ulcjava.base.application.ULCPopupMenu;
import com.ulcjava.base.application.ULCTableTree;
import com.ulcjava.base.application.event.ActionEvent;
import com.ulcjava.base.application.event.IActionListener;
import com.ulcjava.base.application.tree.TreePath;
import com.ulcjava.base.application.util.Dimension;
import org.pillarone.riskanalytics.core.simulation.item.Parameterization;
import org.pillarone.riskanalytics.graph.core.graph.model.ModelGraphModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.DataTableTreeModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.SimulationResultDataTreeModel;

import java.util.List;
import java.util.Map;

/**
 * 
 */
public class SimulationResultTable extends ULCTableTree {

    SimulationResultDataTreeModel fTableModel;

    public SimulationResultTable(Map simulationOutput, List<String> periodLabels) {
        createView();
        setData(simulationOutput, periodLabels);
    }

    public void setData(Map simulationOutput, List<String> periodLabels) {
        fTableModel = new SimulationResultDataTreeModel(simulationOutput, periodLabels);
        this.setModel(fTableModel);
        this.expandAll();
    }

    public void createView() {
        this.createDefaultColumnsFromModel();
        this.setShowGrid(true);
        int width = ClientContext.getScreenWidth();
        int height = ClientContext.getScreenHeight();
        int preferredWidth = width / 2;
        int preferredHeight = preferredWidth * height * 10 / (width * 11 * 4);
        this.setPreferredScrollableViewportSize(new Dimension(preferredWidth, preferredHeight));
        createContextMenu();
    }

    private void createContextMenu() {
        ULCPopupMenu menu = new ULCPopupMenu();

        ULCMenuItem expandItem = new ULCMenuItem("expand");
        expandItem.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                TreePath[] selectedPaths = SimulationResultTable.this.getSelectedPaths();
                SimulationResultTable.this.expandPaths(selectedPaths, true);
            }
        });
        menu.add(expandItem);
        ULCMenuItem expandAllItem = new ULCMenuItem("expand all");
        expandAllItem.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                SimulationResultTable.this.expandAll();
            }
        });
        menu.add(expandAllItem);
        ULCMenuItem collapseItem = new ULCMenuItem("collapse");
        collapseItem.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                TreePath[] selectedPaths = SimulationResultTable.this.getSelectedPaths();
                SimulationResultTable.this.collapsePaths(selectedPaths, true);
            }
        });
        menu.add(collapseItem);
        ULCMenuItem collapseAllItem = new ULCMenuItem("collapse all");
        collapseAllItem.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                SimulationResultTable.this.collapseAll();
            }
        });
        menu.add(collapseAllItem);

        this.setComponentPopupMenu(menu);
    }

}
