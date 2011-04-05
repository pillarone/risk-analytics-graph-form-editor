package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.base.application.*;
import com.ulcjava.base.application.event.ActionEvent;
import com.ulcjava.base.application.event.IActionListener;
import com.ulcjava.base.application.tree.*;
import com.ulcjava.base.application.util.Dimension;
import org.pillarone.riskanalytics.graph.core.palette.model.ComponentDefinition;
import org.pillarone.riskanalytics.graph.core.palette.service.PaletteService;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.ComponentTypeTreeModelFactory;
import org.pillarone.riskanalytics.graph.formeditor.util.ComponentTypeTreeUtilities;
import org.pillarone.riskanalytics.graph.formeditor.util.PaletteUtilities;

import java.util.*;
import java.util.Map.Entry;

public class ComponentTypeTree extends ULCBoxPane {

    private static final String PATHSEP = ".";

    private ITreeModel fTreeModel;
    private ULCTree fTree;
    private ComponentTypeTreeCellRenderer fTreeCellRenderer;
    private GraphModelEditor fParent;

    public ComponentTypeTree(GraphModelEditor parent) {
        super();
        fParent = parent;
        fTreeModel = ComponentTypeTreeModelFactory.getTree();
        createView();
    }

    public void configureCellRenderer(IActionListener listener) {
    }

    private void createView() {
        // create tree
        fTree = new ULCTree();
        fTree.setDragEnabled(true);
        fTree.setModel(fTreeModel);
        fTree.getSelectionModel().setSelectionMode(ULCTreeSelectionModel.SINGLE_TREE_SELECTION);
        fTreeCellRenderer = new ComponentTypeTreeCellRenderer();
        fTreeCellRenderer.setShowComponentMenuListener(new ShowComponentAction());
        fTree.setCellRenderer(fTreeCellRenderer);

        ULCScrollPane treeScrollPane = new ULCScrollPane(fTree);
        treeScrollPane.setMinimumSize(new Dimension(200, 600));
        this.add(ULCBoxPane.BOX_EXPAND_EXPAND, treeScrollPane);

        this.setVisible(true);
    }

    private class ShowComponentAction implements IActionListener {

        public void actionPerformed(ActionEvent actionEvent) {
            TreePath selectedNode = fTree.getSelectionModel().getSelectionPath();
            String clazzName = ComponentTypeTreeUtilities.getComponentTypeName(selectedNode);
            try {
                boolean success = fParent.importComponentType(clazzName);
                if (!success) {
                    ULCAlert alert = new ULCAlert("No class loaded",
                            "No class with name " + clazzName + " could be loaded as graph model.", "ok");
                    alert.show();
                }
            } catch (ClassNotFoundException ex1) {
                ULCAlert alert = new ULCAlert("No class loaded",
                        "Class not found.", "ok");
                alert.show();
            } catch (Exception ex3) {
                ULCAlert alert = new ULCAlert("No class loaded",
                        "Unkown exception. ", "ok");
                alert.show();
            }
        }
    }
}
