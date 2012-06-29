package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.base.application.*;
import com.ulcjava.base.application.event.ActionEvent;
import com.ulcjava.base.application.event.IActionListener;
import com.ulcjava.base.application.tree.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.model.ModelGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.persistence.GraphPersistenceService;
import org.pillarone.riskanalytics.graph.formeditor.ui.IGraphModelHandler;
import org.pillarone.riskanalytics.graph.formeditor.ui.IModelRenameListener;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.ModelRepositoryTreeModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.ModelRepositoryTreeNode;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.TypeDefinitionBean;

public class ModelRepositoryTree extends ULCBoxPane implements IModelRenameListener {

    private static Log LOG = LogFactory.getLog(ModelRepositoryTree.class);

    private ModelRepositoryTreeModel treeModel;
    private ULCTree tree;
    private IGraphModelHandler graphModelHandler;
    private GraphPersistenceService persistenceService;

    public ModelRepositoryTree(IGraphModelHandler graphModelHandler) {
        super();
        this.graphModelHandler = graphModelHandler;
        treeModel = ModelRepositoryTreeModel.getInstance();
        createView();
    }

    public ModelRepositoryTreeModel getTreeModel() {
        return treeModel;
    }

    /**
     * @see org.pillarone.riskanalytics.graph.formeditor.ui.IModelRenameListener
     * @param modelWithNewName
     * @param oldName
     * @param oldPackageName
     */
    public void modelRenamed(AbstractGraphModel modelWithNewName, String oldName, String oldPackageName) {
        // modify name in the tree view
        ModelRepositoryTreeNode node = treeModel.getModelNode(modelWithNewName);
        if (node!=null) {
            node.setPackageName(modelWithNewName.getPackageName());
            node.setName(modelWithNewName.getName());
            treeModel.nodeChanged(new TreePath(DefaultTreeModel.getPathToRoot(node)));
        }
    }
    
    public void removeModel(AbstractGraphModel model) {
        ModelRepositoryTreeNode node = treeModel.getModelNode(model);
        if (node != null) {
            IMutableTreeNode parent = (IMutableTreeNode) node.getParent();
            final int index = parent.getIndex((ITreeNode) node);
            parent.remove(index);
            treeModel.nodesWereRemoved(parent, new int[]{index}, new Object[]{node});
            treeModel.getLeaves().remove(node);
        }
    }

    private void createView() {
        // create tree
        tree = new ULCTree();
        tree.setDragEnabled(true);
        tree.setModel(treeModel);
        tree.getSelectionModel().setSelectionMode(ULCTreeSelectionModel.SINGLE_TREE_SELECTION);
        CellRenderer treeCellRenderer = new CellRenderer();
        tree.setCellRenderer(treeCellRenderer);

        ULCScrollPane treeScrollPane = new ULCScrollPane(tree);
        // treeScrollPane.setMinimumSize(new Dimension(200, 600));
        this.add(ULCBoxPane.BOX_EXPAND_EXPAND, treeScrollPane);

        this.setVisible(true);
    }

    private class LoadModelAction implements IActionListener {

        public void actionPerformed(ActionEvent actionEvent) {
            TreePath treePath = tree.getSelectionModel().getSelectionPath();
            Object selectedNode = treePath.getLastPathComponent();
            if (selectedNode instanceof ModelRepositoryTreeNode) {
                AbstractGraphModel graphModel = treeModel.getLeaves().get((ModelRepositoryTreeNode) selectedNode);
                if (graphModel != null) {
                    TypeDefinitionBean typeDefBean = new TypeDefinitionBean();
                    typeDefBean.setName(graphModel.getName());
                    typeDefBean.setPackageName(graphModel.getPackageName());
                    typeDefBean.setBaseType(graphModel instanceof ModelGraphModel ? TypeDefinitionBean.MODEL : TypeDefinitionBean.COMPOSED_COMPONENT);
                    graphModelHandler.addModel(graphModel, typeDefBean, true);
                }
            }
        }
    }

    private class DeleteModelAction implements IActionListener {

        public void actionPerformed(ActionEvent actionEvent) {
            TreePath treePath = tree.getSelectionModel().getSelectionPath();
            Object selectedNode = treePath.getLastPathComponent();
            if (selectedNode instanceof ModelRepositoryTreeNode) {
                AbstractGraphModel graphModel = treeModel.getLeaves().get((ModelRepositoryTreeNode)selectedNode);
                if (graphModel != null) {
                    graphModelHandler.removeModel(graphModel);
                }
            }
        }
    }

    private class RenameModelAction implements IActionListener {

        public void actionPerformed(ActionEvent actionEvent) {
            TreePath treePath = tree.getSelectionModel().getSelectionPath();
            Object selectedNode = treePath.getLastPathComponent();
            if (selectedNode instanceof ModelRepositoryTreeNode) {
                AbstractGraphModel graphModel = treeModel.getLeaves().get((ModelRepositoryTreeNode) selectedNode);
                if (graphModel != null) {
                    graphModelHandler.renameModel(graphModel);
                }
            }
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
