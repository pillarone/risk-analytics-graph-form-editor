package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.base.application.*;
import com.ulcjava.base.application.event.ActionEvent;
import com.ulcjava.base.application.event.IActionListener;
import com.ulcjava.base.application.tree.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.commons.ApplicationHolder;
import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.persistence.GraphPersistenceService;
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
                final String modelName = ((ModelRepositoryTreeNode) selectedNode).getName();
                final String packageName = ((ModelRepositoryTreeNode) selectedNode).getPackageName();
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

    private class DeleteModelAction implements IActionListener {

        GraphPersistenceService fPersistenceService;

        public void actionPerformed(ActionEvent actionEvent) {
            TreePath treePath = fTree.getSelectionModel().getSelectionPath();
            Object selectedNode = treePath.getLastPathComponent();
            if (selectedNode instanceof ModelRepositoryTreeNode) {
                final String modelName = ((ModelRepositoryTreeNode) selectedNode).getName();
                final String packageName = ((ModelRepositoryTreeNode) selectedNode).getPackageName();
                try {
                    final AbstractGraphModel graphModel = getPersistenceService().load(modelName, packageName);
                    getPersistenceService().delete(graphModel);
                    IMutableTreeNode parent = (IMutableTreeNode) ((ModelRepositoryTreeNode) selectedNode).getParent();
                    final int index = parent.getIndex((ITreeNode) selectedNode);
                    parent.remove(index);
                    fTreeModel.nodesWereRemoved(parent, new int[]{index}, new Object[]{selectedNode});

                } catch (Exception ex) {
                    ULCAlert alert = new ULCAlert("Model could not be deleted", "Reason: " + ex.getMessage(), "ok");
                    LOG.error("Model could not be deleted", ex);
                    alert.show();
                }
            }
        }

        private GraphPersistenceService getPersistenceService() {
            if (fPersistenceService == null) {
                org.springframework.context.ApplicationContext ctx = ApplicationHolder.getApplication().getMainContext();
                fPersistenceService = ctx.getBean(GraphPersistenceService.class);
            }
            return fPersistenceService;
        }
    }

    private class RenameModelAction implements IActionListener {

        GraphPersistenceService fPersistenceService;

        public void actionPerformed(ActionEvent actionEvent) {
            TreePath treePath = fTree.getSelectionModel().getSelectionPath();
            Object selectedNode = treePath.getLastPathComponent();
            if (selectedNode instanceof ModelRepositoryTreeNode) {
                final String modelName = ((ModelRepositoryTreeNode) selectedNode).getName();
                final String packageName = ((ModelRepositoryTreeNode) selectedNode).getPackageName();
                try {
                    final AbstractGraphModel graphModel = getPersistenceService().load(modelName, packageName);
                    new ModelRenameDialog(graphModel, fTree, (ModelRepositoryTreeNode) selectedNode).setVisible(true);

                } catch (Exception ex) {
                    ULCAlert alert = new ULCAlert("Model could not be deleted", "Reason: " + ex.getMessage(), "ok");
                    LOG.error("Model could not be deleted", ex);
                    alert.show();
                }
            }

        }

        private GraphPersistenceService getPersistenceService() {
            if (fPersistenceService == null) {
                org.springframework.context.ApplicationContext ctx = ApplicationHolder.getApplication().getMainContext();
                fPersistenceService = ctx.getBean(GraphPersistenceService.class);
            }
            return fPersistenceService;
        }
    }

    private class CellRenderer extends DefaultTreeCellRenderer {
        private ULCPopupMenu fNodePopUpMenu;
        private ULCMenuItem fShowComponentMenuItem;
        private ULCMenuItem fDeleteMenuItem;


        CellRenderer() {
            super();
            fNodePopUpMenu = new ULCPopupMenu();
            fShowComponentMenuItem = new ULCMenuItem("load");
            fShowComponentMenuItem.addActionListener(new LoadModelAction());
            fNodePopUpMenu.add(fShowComponentMenuItem);
            fDeleteMenuItem = new ULCMenuItem("delete");
            fDeleteMenuItem.addActionListener(new DeleteModelAction());
            fNodePopUpMenu.add(fDeleteMenuItem);
            ULCMenuItem renameMenuItem = new ULCMenuItem("rename");
            renameMenuItem.addActionListener(new RenameModelAction());
            fNodePopUpMenu.add(renameMenuItem);

        }

        public IRendererComponent getTreeCellRendererComponent(ULCTree tree, Object node, boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
            IRendererComponent component = super.getTreeCellRendererComponent(tree, node, selected, expanded, leaf, hasFocus);
            if (node instanceof ModelRepositoryTreeNode && ((ModelRepositoryTreeNode) node).isLeaf()) {
                setPopUpMenu((ULCComponent) component, (ModelRepositoryTreeNode) node);
            }
            setToolTip((ULCComponent) component, (ModelRepositoryTreeNode) node);
            return component;
        }

        void setPopUpMenu(ULCComponent component, ModelRepositoryTreeNode node) {
            component.setComponentPopupMenu(fNodePopUpMenu);
        }

        void setToolTip(ULCComponent component, ModelRepositoryTreeNode node) {
            component.setToolTipText(node.getPackageName() + "." + node.getName());
        }
    }
}
