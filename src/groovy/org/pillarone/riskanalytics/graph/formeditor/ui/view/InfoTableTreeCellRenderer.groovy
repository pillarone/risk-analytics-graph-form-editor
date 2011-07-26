package org.pillarone.riskanalytics.graph.formeditor.ui.view

import org.pillarone.riskanalytics.graph.formeditor.util.UIUtils
import com.ulcjava.base.application.IRendererComponent
import com.ulcjava.base.application.ULCTableTree
import com.ulcjava.base.application.tabletree.DefaultTableTreeCellRenderer
import org.pillarone.riskanalytics.graph.formeditor.ui.model.EnumGraphElementInfo

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */
class InfoTableTreeCellRenderer extends DefaultTableTreeCellRenderer {


    @Override
    public IRendererComponent getTableTreeCellRendererComponent(ULCTableTree tableTree, Object value, boolean selected, boolean hasFocus, boolean expanded, boolean leaf, Object node) {
        InfoTableTreeCellRenderer rendererComponent = (InfoTableTreeCellRenderer) super.getTableTreeCellRendererComponent(tableTree, value, selected, hasFocus, expanded, leaf, node);
        EnumGraphElementInfo info = EnumGraphElementInfo.getEnumGraphElementInfo(value)
        rendererComponent.setIcon(info.getIcon() ? UIUtils.getIcon(info.getIcon()) : null)
        return rendererComponent;
    }
}
