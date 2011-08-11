package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.base.application.ULCAlert;
import com.ulcjava.base.application.ULCBoxPane;
import com.ulcjava.base.application.ULCScrollPane;
import com.ulcjava.base.application.ULCTree;
import com.ulcjava.base.application.event.ActionEvent;
import com.ulcjava.base.application.event.IActionListener;
import com.ulcjava.base.application.tree.ITreeModel;
import com.ulcjava.base.application.tree.TreePath;
import com.ulcjava.base.application.tree.ULCTreeSelectionModel;
import com.ulcjava.base.application.util.Dimension;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.palette.FilteringTreeModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.palette.TypeTreeNode;

public abstract class AbstractComponentDefinitionTree extends ULCBoxPane implements  ISearchListener{

    private ITreeModel fTreeModel;
    private ULCTree fTree;
    private GraphModelEditor fParent; // TODO: this is somewhat ugly

    public AbstractComponentDefinitionTree(GraphModelEditor parent) {
        super();
        fParent = parent;
        fTreeModel = getTreeModel();
        createView();
    }

    protected abstract ITreeModel getTreeModel();

    private void createView() {
        // create tree
        fTree = new ULCTree();
        fTree.setDragEnabled(true);
        fTree.setModel(fTreeModel);
        fTree.getSelectionModel().setSelectionMode(ULCTreeSelectionModel.SINGLE_TREE_SELECTION);
        ComponentTypeTreeCellRenderer treeCellRenderer = new ComponentTypeTreeCellRenderer();
        treeCellRenderer.setShowComponentMenuListener(new ShowComponentAction());
        fTree.setCellRenderer(treeCellRenderer);

        ULCScrollPane treeScrollPane = new ULCScrollPane(fTree);
        treeScrollPane.setMinimumSize(new Dimension(200, 600));
        this.add(ULCBoxPane.BOX_EXPAND_EXPAND, treeScrollPane);

        this.setVisible(true);
    }

    private class ShowComponentAction implements IActionListener {

        public void actionPerformed(ActionEvent actionEvent) {
            TreePath treePath = fTree.getSelectionModel().getSelectionPath();
            Object selectedNode = treePath.getLastPathComponent();
            if (selectedNode instanceof TypeTreeNode) {
                String clazzName = ((TypeTreeNode)selectedNode).getFullName();
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

    public void search(String text) {
        ((FilteringTreeModel)fTreeModel).setFilter(new NameFilter(text));
    }
}
