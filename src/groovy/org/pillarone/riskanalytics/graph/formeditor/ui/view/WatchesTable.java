package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.base.application.*;
import com.ulcjava.base.application.datatype.IDataType;
import com.ulcjava.base.application.datatype.ULCNumberDataType;
import com.ulcjava.base.application.event.ActionEvent;
import com.ulcjava.base.application.event.IActionListener;
import com.ulcjava.base.application.tabletree.*;
import com.ulcjava.base.application.tree.TreePath;
import com.ulcjava.base.application.util.Color;
import com.ulcjava.base.application.util.Dimension;
import com.ulcjava.base.shared.UlcEventConstants;
import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.IDataTreeNode;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.WatchesTreeModel;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class WatchesTable extends ULCBoxPane {

    WatchesTreeModel fTableModel;
    ULCTableTree fTableTree;

    public WatchesTable() {
        super(true);
        fTableModel = new WatchesTreeModel();
        createView();
        addCellRenderers();
    }

    public void createView() {
        ClientContext.setModelUpdateMode(fTableModel, UlcEventConstants.SYNCHRONOUS_MODE);
        fTableTree = new ULCTableTree(fTableModel);
        //fTableTree.createDefaultColumnsFromModel();
        fTableTree.setShowGrid(true);

        int width = ClientContext.getScreenWidth();
        int height = ClientContext.getScreenHeight();
        int preferredWidth = width / 2;
        int preferredHeight = preferredWidth * height * 10 / (width * 11 * 4);
        fTableTree.setPreferredScrollableViewportSize(new Dimension(preferredWidth, preferredHeight));

        ULCScrollPane scrollPane = new ULCScrollPane(fTableTree);
        this.add(ULCBoxPane.BOX_EXPAND_EXPAND, scrollPane);
        this.setBorder(BorderFactory.createEmptyBorder());

        createContextMenu();
    }

    public WatchesTreeModel getModel() {
        return fTableModel;
    }

    public ULCTableTree getTable() {
        return fTableTree;
    }

    private void createContextMenu() {
        ULCPopupMenu menu = new ULCPopupMenu();

        ULCMenuItem removeItem = new ULCMenuItem("remove");
        removeItem.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                TreePath[] selectedPaths = fTableTree.getSelectedPaths();
                for (TreePath treePath : selectedPaths) {
                    if (treePath.getPathCount()==2) {
                        String path = (String) ((ITableTreeNode) treePath.getLastPathComponent()).getValueAt(0);
                        fTableModel.removeWatch(path);
                    }
                }
            }
        });
        menu.add(removeItem);

        ULCMenuItem removeAllItem = new ULCMenuItem("remove all");
        removeAllItem.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                fTableModel.removeAllWatches();
            }
        });
        menu.add(removeAllItem);

        menu.addSeparator();

        ULCMenuItem expandItem = new ULCMenuItem("expand");
        expandItem.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                TreePath[] selectedPaths = fTableTree.getSelectedPaths();
                fTableTree.expandPaths(selectedPaths, true);
            }
        });
        menu.add(expandItem);

        ULCMenuItem expandAllItem = new ULCMenuItem("expand all");
        expandAllItem.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                fTableTree.expandAll();
            }
        });
        menu.add(expandAllItem);
        ULCMenuItem collapseItem = new ULCMenuItem("collapse");
        collapseItem.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                TreePath[] selectedPaths = fTableTree.getSelectedPaths();
                fTableTree.collapsePaths(selectedPaths, true);
            }
        });
        menu.add(collapseItem);
        ULCMenuItem collapseAllItem = new ULCMenuItem("collapse all");
        collapseAllItem.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                fTableTree.collapseAll();
            }
        });
        menu.add(collapseAllItem);

        fTableTree.setComponentPopupMenu(menu);
    }

    private void addCellRenderers() {
        DefaultTableTreeCellRenderer defaultTableTreeCellRenderer = new DefaultTableTreeCellRenderer();
        for (int i = 0; i < fTableTree.getColumnModel().getColumnCount(); i++) {
            ULCTableTreeColumn column = fTableTree.getColumnModel().getColumn(i);
            column.setMaxWidth(200);
            column.setCellRenderer(defaultTableTreeCellRenderer);
        }
    }
}