package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.base.application.ClientContext;
import com.ulcjava.base.application.ULCMenuItem;
import com.ulcjava.base.application.ULCPopupMenu;
import com.ulcjava.base.application.ULCTableTree;
import com.ulcjava.base.application.datatype.IDataType;
import com.ulcjava.base.application.datatype.ULCNumberDataType;
import com.ulcjava.base.application.event.ActionEvent;
import com.ulcjava.base.application.event.IActionListener;
import com.ulcjava.base.application.tabletree.DefaultTableTreeCellRenderer;
import com.ulcjava.base.application.tabletree.DefaultTableTreeModel;
import com.ulcjava.base.application.tabletree.ULCTableTreeColumn;
import com.ulcjava.base.application.tree.TreePath;
import com.ulcjava.base.application.util.Dimension;
import org.pillarone.riskanalytics.graph.core.graph.model.ComponentNode;
import org.pillarone.riskanalytics.graph.core.graph.model.Connection;
import org.pillarone.riskanalytics.graph.core.graph.model.ModelGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.model.filters.IComponentNodeFilter;
import org.pillarone.riskanalytics.graph.core.graph.model.filters.NoneComponentNodeFilter;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.DataTableTreeModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.IDataTreeNode;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.SimulationResultDataTreeModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.treetable.FilteringTableTreeModel;

import java.util.List;
import java.util.Map
import org.pillarone.riskanalytics.graph.formeditor.ui.model.treetable.NodeNameFilter;

/**
 *
 */
public class SimulationResultTable extends ULCTableTree implements ISelectionListener {

    FilteringTableTreeModel fTableModel;

    public SimulationResultTable(Map simulationOutput, List<String> periodLabels) {
        createView();
        setData(simulationOutput, periodLabels);
        setRenderers();
    }

    public void setData(Map simulationOutput, List<String> periodLabels) {
        NodeNameFilter noFilter = new NodeNameFilter(null);
        SimulationResultDataTreeModel originalModel = new SimulationResultDataTreeModel(simulationOutput, periodLabels);
        fTableModel = new FilteringTableTreeModel(originalModel, noFilter);
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

    private void setRenderers() {
        DefaultTableTreeCellRenderer defaultTableTreeCellRenderer = new DefaultTableTreeCellRenderer();
        for (int i = 0; i < getColumnModel().getColumnCount(); i++) {
            ULCTableTreeColumn column = getColumnModel().getColumn(i);
            column.setMaxWidth(200);
            if (i == 0) {
                column.setCellRenderer(defaultTableTreeCellRenderer);
            } else {
                IDataType dataType = new ULCNumberDataType<Double>(ClientContext.getLocale());
                column.setCellRenderer(new BasicCellRenderer(i, dataType));
            }
        }
    }

    public void applyFilter(NodeNameFilter filter) {
        fTableModel.setFilter(filter)
    }

    void applyFilter(IComponentNodeFilter filter) {
    }



    public void setSelectedComponents(List<ComponentNode> selection) {
        for (ComponentNode cn : selection) {
            SimulationResultDataTreeModel.ParentNode node = fTableModel.findNode(cn.getName());
            if (node != null) {
                TreePath treePath = new TreePath(DefaultTableTreeModel.getPathToRoot(node));
                makeVisible(treePath);
                scrollCellToVisible(treePath, 0);
                if (!getSelectionModel().isPathSelected(treePath))
                    getSelectionModel().setSelectionPath(treePath);
            }
        }
    }

    public void nodeSelected(String path){}

    public void setSelectedConnections(List<Connection> selection) {
    }
}
