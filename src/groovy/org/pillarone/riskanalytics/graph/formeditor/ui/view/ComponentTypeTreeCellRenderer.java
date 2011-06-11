package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.base.application.*;
import com.ulcjava.base.application.event.IActionListener;
import com.ulcjava.base.application.tree.DefaultMutableTreeNode;
import com.ulcjava.base.application.tree.DefaultTreeCellRenderer;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.TypeTreeNode;

/**
 *
 */
public class ComponentTypeTreeCellRenderer extends DefaultTreeCellRenderer {
    private ULCPopupMenu fNodePopUpMenu;
    private ULCMenuItem fShowComponentMenuItem;


    public ComponentTypeTreeCellRenderer() {
        super();
        fNodePopUpMenu = new ULCPopupMenu();
        fShowComponentMenuItem = new ULCMenuItem("show");
        fNodePopUpMenu.add(fShowComponentMenuItem);
    }

    public void setShowComponentMenuListener(IActionListener listener) {
        fShowComponentMenuItem.addActionListener(listener);
    }

    public IRendererComponent getTreeCellRendererComponent(ULCTree tree, Object node, boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        IRendererComponent component = super.getTreeCellRendererComponent(tree, node, selected, expanded, leaf, hasFocus);
        if (node instanceof TypeTreeNode && ((TypeTreeNode)node).isLeaf()) {
            setPopUpMenu((ULCComponent) component, (TypeTreeNode) node);
            setToolTip((ULCComponent) component, (TypeTreeNode) node);
        }
        return component;
    }

    void setPopUpMenu(ULCComponent component, DefaultMutableTreeNode node) {
        component.setComponentPopupMenu(fNodePopUpMenu);
    }

    void setToolTip(ULCComponent component, TypeTreeNode node) {
        component.setToolTipText(node.getPackagePath()+"."+node.getName());
    }
}