package org.pillarone.riskanalytics.graph.formeditor.ui.view.helpers

import com.ulcjava.base.application.IRendererComponent
import com.ulcjava.base.application.ULCTableTree
import com.ulcjava.base.application.tabletree.DefaultTableTreeCellRenderer
import org.pillarone.riskanalytics.graph.formeditor.ui.model.EnumGraphElementInfo
import org.pillarone.riskanalytics.graph.formeditor.ui.model.treetable.GraphElementNode
import org.pillarone.riskanalytics.graph.formeditor.util.UIUtils

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */
class InfoTableTreeCellRenderer extends DefaultTableTreeCellRenderer {


    @Override
    public IRendererComponent getTableTreeCellRendererComponent(ULCTableTree tableTree, Object value, boolean selected, boolean hasFocus, boolean expanded, boolean leaf, Object node) {
        InfoTableTreeCellRenderer rendererComponent = (InfoTableTreeCellRenderer) super.getTableTreeCellRendererComponent(tableTree, value, selected, hasFocus, expanded, leaf, node);
        EnumGraphElementInfo info = getEnumGraphElementInfo(node)
        if (info?.getIcon()) {
            rendererComponent.setIcon(UIUtils.getIcon(info.getIcon()))
            rendererComponent.setToolTipText(info.getDisplayValue())
        }
        return rendererComponent;
    }

    private EnumGraphElementInfo getEnumGraphElementInfo(GraphElementNode node) {
        return EnumGraphElementInfo.getEnumGraphElementInfo(node.info)
    }

    private EnumGraphElementInfo getEnumGraphElementInfo(def node) {
        return null
    }


}
