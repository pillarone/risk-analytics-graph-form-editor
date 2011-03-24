package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.base.application.*;
import com.ulcjava.base.application.event.IActionListener;
import com.ulcjava.base.application.tree.DefaultMutableTreeNode;
import com.ulcjava.base.application.tree.DefaultTreeCellRenderer;

/**
 * Created by IntelliJ IDEA.
 * User: martin.melchior
 * Date: 24.03.11
 * Time: 18:04
 * To change this template use File | Settings | File Templates.
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
        if (node instanceof DefaultMutableTreeNode ) {
            setPopUpMenu((ULCComponent) component, (DefaultMutableTreeNode ) node);
        }
        setToolTip((ULCComponent) component, node);
        return component;
    }

    void setPopUpMenu(ULCComponent component, DefaultMutableTreeNode node) {
        component.setComponentPopupMenu(fNodePopUpMenu);
    }

    void setToolTip(ULCComponent component, Object node) {
        component.setToolTipText("");
    }
}