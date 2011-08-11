package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.base.application.IRendererComponent;
import com.ulcjava.base.application.ULCMenuItem;
import com.ulcjava.base.application.ULCPopupMenu;
import com.ulcjava.base.application.ULCTree;
import com.ulcjava.base.application.event.IActionListener;
import com.ulcjava.base.application.tree.DefaultTreeCellRenderer;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.palette.TypeTreeNode;

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
        DefaultTreeCellRenderer component = (DefaultTreeCellRenderer) super.getTreeCellRendererComponent(tree, node, selected, expanded, leaf, hasFocus);
        if (node instanceof TypeTreeNode && ((TypeTreeNode)node).isLeaf()) {
            component.setComponentPopupMenu(fNodePopUpMenu);
            component.setToolTipText(((TypeTreeNode) node).getFullName());
            component.setText(((TypeTreeNode) node).getName());
        }
        return component;
    }
}