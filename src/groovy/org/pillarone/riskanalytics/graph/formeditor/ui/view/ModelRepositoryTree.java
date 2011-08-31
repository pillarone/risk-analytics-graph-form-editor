package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.base.application.*;
import com.ulcjava.base.application.event.ActionEvent;
import com.ulcjava.base.application.event.IActionListener;
import com.ulcjava.base.application.tree.*;
import com.ulcjava.base.application.util.Dimension;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.ModelRepositoryTreeModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.ModelRepositoryTreeNode;

public class ModelRepositoryTree extends ULCBoxPane {

    private static Log LOG = LogFactory.getLog(ModelRepositoryTree.class);

    private ModelRepositoryTreeModel fTreeModel;
    private ULCTree fTree;
    private GraphModelEditor fParent; // TODO: this is somewhat ugly

    public ModelRepositoryTree(GraphModelEditor parent) {
        super();
        fParent = parent;
        fTreeModel = ModelRepositoryTreeModel.getInstance();
        createView();
    }

    public ModelRepositoryTreeModel getTreeModel() {
        return fTreeModel;
    }

    private void createView() {
        // create tree
        fTree = new ULCTree();
        fTree.setDragEnabled(true);
        fTree.setModel(fTreeModel);
        fTree.getSelectionModel().setSelectionMode(ULCTreeSelectionModel.SINGLE_TREE_SELECTION);
        CellRenderer treeCellRenderer = new CellRenderer();
        treeCellRenderer.setLoadModelMenuListener(new LoadModelAction());
        fTree.setCellRenderer(treeCellRenderer);

        ULCScrollPane treeScrollPane = new ULCScrollPane(fTree);
        // treeScrollPane.setMinimumSize(new Dimension(200, 600));
        this.add(ULCBoxPane.BOX_EXPAND_EXPAND, treeScrollPane);

        this.setVisible(true);
    }

    private class LoadModelAction implements IActionListener {

        public void actionPerformed(ActionEvent actionEvent) {
            TreePath treePath = fTree.getSelectionModel().getSelectionPath();
            Object selectedNode = treePath.getLastPathComponent();
            if (selectedNode instanceof ModelRepositoryTreeNode) {
                final String modelName = ((ModelRepositoryTreeNode)selectedNode).getName();
                final String packageName = ((ModelRepositoryTreeNode)selectedNode).getPackageName();
                try {
                    fParent.loadModel(modelName, packageName);
                } catch (Exception ex) {
                    ULCAlert alert = new ULCAlert("Model could not be loaded", "Reason: " + ex.getMessage(), "ok");
                    LOG.error("Model could not be loaded", ex);
                    alert.show();
                }
            }
        }
    }

    private class CellRenderer extends DefaultTreeCellRenderer {
        private ULCPopupMenu fNodePopUpMenu;
        private ULCMenuItem fShowComponentMenuItem;


        CellRenderer() {
            super();
            fNodePopUpMenu = new ULCPopupMenu();
            fShowComponentMenuItem = new ULCMenuItem("load");
            fNodePopUpMenu.add(fShowComponentMenuItem);
        }

        public void setLoadModelMenuListener(IActionListener listener) {
            fShowComponentMenuItem.addActionListener(listener);
        }

        public IRendererComponent getTreeCellRendererComponent(ULCTree tree, Object node, boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
            IRendererComponent component = super.getTreeCellRendererComponent(tree, node, selected, expanded, leaf, hasFocus);
            if (node instanceof ModelRepositoryTreeNode && ((ModelRepositoryTreeNode)node).isLeaf()) {
                setPopUpMenu((ULCComponent) component, (ModelRepositoryTreeNode) node);
            }
            setToolTip((ULCComponent) component, (ModelRepositoryTreeNode) node);
            return component;
        }

        void setPopUpMenu(ULCComponent component, ModelRepositoryTreeNode node) {
            component.setComponentPopupMenu(fNodePopUpMenu);
        }

        void setToolTip(ULCComponent component, ModelRepositoryTreeNode node) {
            component.setToolTipText(node.getPackageName()+"."+node.getName());
        }
    }
}
