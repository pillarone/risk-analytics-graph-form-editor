package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.base.application.*;
import com.ulcjava.base.application.event.ActionEvent;
import com.ulcjava.base.application.event.IActionListener;
import com.ulcjava.base.application.event.ITreeSelectionListener;
import com.ulcjava.base.application.event.TreeSelectionEvent;
import com.ulcjava.base.application.tree.*;
import com.ulcjava.base.application.util.Dimension;
import org.pillarone.riskanalytics.graph.core.palette.model.ComponentDefinition;
import org.pillarone.riskanalytics.graph.core.palette.service.IPaletteServiceListener;
import org.pillarone.riskanalytics.graph.core.palette.service.PaletteService;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.palette.FilteringTreeModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.palette.ITreeFilter;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.palette.TypeTreeNode;

public abstract class AbstractComponentDefinitionTree extends ULCBoxPane implements ISearchListener {

    protected ITreeModel fTreeModel;
    protected ULCTree fTree;
    protected GraphModelEditor fParent; // TODO: this is somewhat ugly

    public AbstractComponentDefinitionTree(GraphModelEditor parent) {
        super();
        fParent = parent;
        fTreeModel = getTreeModel();
        createView();
    }

    protected ITreeModel getTreeModel() {
        final FilteringTreeModel treeModel = new FilteringTreeModel(createTreeModel(), new ITreeFilter() {
            public boolean acceptNode(ITreeNode node) {
                return true;
            }
        });
        PaletteService.getInstance().addPaletteServiceListener(new IPaletteServiceListener() {

            public void componentDefinitionAdded(ComponentDefinition definition) {
                insertNodeForComponentDefinition(definition);
            }

        });
        return treeModel;
    }

    protected FilteringTreeModel getFilteringTreeModel() {
        return (FilteringTreeModel) fTreeModel;
    }

    protected AbstractTreeModel getActualTreeModel() {
        return (AbstractTreeModel) getFilteringTreeModel().getModel();
    }

    protected abstract DefaultTreeModel createTreeModel();

    protected abstract void insertNodeForComponentDefinition(ComponentDefinition definition);

    protected int findInsertIndex(TypeTreeNode node, String newNodeName) {
        if (node.getChildCount() == 0) {
            return 0;
        }

        int index = 0;
        TypeTreeNode current = (TypeTreeNode) node.getChildAt(index);
        while (node.getChildCount() > index && (newNodeName.compareTo(current.getName()) > 0)) {
            index++;
            if (index < node.getChildCount()) {
                current = (TypeTreeNode) node.getChildAt(index);
            }
        }

        return index;
    }

    protected TypeTreeNode findOrCreateNode(TypeTreeNode parent, String childName) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            TypeTreeNode child = (TypeTreeNode) parent.getChildAt(i);
            if (child.getName().equals(childName)) {
                return child;
            }
        }

        TypeTreeNode newNode = new TypeTreeNode("", childName);
        final int insertIndex = findInsertIndex(parent, childName);
        parent.insert(newNode, insertIndex);
        getActualTreeModel().nodesWereInserted(new TreePath(DefaultTreeModel.getPathToRoot(parent)), new int[]{insertIndex});
        return newNode;
    }

    private void createView() {
        // create tree
        fTree = new ULCTree();
        fTree.setDragEnabled(true);
        fTree.setModel(fTreeModel);
        fTree.getSelectionModel().setSelectionMode(ULCTreeSelectionModel.SINGLE_TREE_SELECTION);
        ComponentTypeTreeCellRenderer treeCellRenderer = new ComponentTypeTreeCellRenderer();
        fTree.setCellRenderer(treeCellRenderer);

        ULCScrollPane treeScrollPane = new ULCScrollPane(fTree);
        treeScrollPane.setMinimumSize(new Dimension(200, 600));
        this.add(ULCBoxPane.BOX_EXPAND_EXPAND, treeScrollPane);
        addListeners();
        this.setVisible(true);
    }

    private void addListeners() {
        fTree.getSelectionModel().addTreeSelectionListener(new ITreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent treeSelectionEvent) {
                TypeTreeNode lastPathComponent = (TypeTreeNode) fTree.getSelectionModel().getSelectionPath().getLastPathComponent();
                if (lastPathComponent != null && lastPathComponent.isLeaf()) {
                    ComponentDefinition definition = PaletteService.getInstance().getComponentDefinition(lastPathComponent.getFullName());
                    IHelpViewable helpView = fParent.getHelpView();
                    if (helpView != null) {
                        helpView.showHelp(definition);
                    }
                }
            }
        });
    }

    private class ShowComponentAction implements IActionListener {

        public void actionPerformed(ActionEvent actionEvent) {
            TreePath treePath = fTree.getSelectionModel().getSelectionPath();
            Object selectedNode = treePath.getLastPathComponent();
            if (selectedNode instanceof TypeTreeNode) {
                String clazzName = ((TypeTreeNode) selectedNode).getFullName();
                try {
                    boolean success = fParent.importComponentType(clazzName);
                    if (!success) {
                        ULCAlert alert = new ULCAlert("No Graph Model to show.",
                                "Type " + clazzName + " is only a simple component.", "ok");
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
        ((FilteringTreeModel) fTreeModel).setFilter(new NameFilter(text));
    }

    public class ComponentTypeTreeCellRenderer extends DefaultTreeCellRenderer {
        private ULCPopupMenu fNodePopUpMenu;
        private ULCMenuItem fShowComponentMenuItem;

        public ComponentTypeTreeCellRenderer() {
            super();
            fNodePopUpMenu = new ULCPopupMenu();
            fShowComponentMenuItem = new ULCMenuItem("show");
            fShowComponentMenuItem.addActionListener(new ShowComponentAction());
            fNodePopUpMenu.add(fShowComponentMenuItem);
        }

        public IRendererComponent getTreeCellRendererComponent(ULCTree tree, Object node, boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
            DefaultTreeCellRenderer component = (DefaultTreeCellRenderer) super.getTreeCellRendererComponent(tree, node, selected, expanded, leaf, hasFocus);
            if (node instanceof TypeTreeNode && ((TypeTreeNode) node).isLeaf()) {
                component.setComponentPopupMenu(fNodePopUpMenu);
                component.setToolTipText(((TypeTreeNode) node).getFullName());
                component.setText(((TypeTreeNode) node).getName());
            }
            return component;
        }
    }
}

