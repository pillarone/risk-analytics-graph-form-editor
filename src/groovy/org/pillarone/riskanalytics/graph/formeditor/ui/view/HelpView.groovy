package org.pillarone.riskanalytics.graph.formeditor.ui.view

import com.ulcjava.base.application.ULCBoxPane
import com.ulcjava.base.application.util.HTMLUtilities
import org.pillarone.riskanalytics.graph.core.graph.util.UIUtils
import com.canoo.ulc.graph.ULCGraph
import com.canoo.ulc.graph.model.Vertex
import com.ulcjava.base.application.ULCFiller
import com.ulcjava.base.application.ULCHtmlPane
import com.ulcjava.base.application.util.Color
import com.canoo.ulc.graph.IGraphSelectionListener

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */
class HelpView implements IVertexHelpListener {

    ULCBoxPane content
    ResourceLinkHtmlPane label

    public HelpView() {
        init()
    }

    private void init() {
        initComponents()
        layoutComponents()
        attachListeners()
    }

    private void initComponents() {
        content = new ULCBoxPane(1, 2)
        content.setBackground(Color.white)
        label = new ResourceLinkHtmlPane()
    }

    private void layoutComponents() {
        content.add(ULCBoxPane.BOX_LEFT_TOP, label)
        content.add(ULCBoxPane.BOX_EXPAND_EXPAND, new ULCFiller())
    }

    private void attachListeners() {
    }

    public void updateView(String propertyKey) {
        String value = UIUtils.getPropertyValue(null, "COMPONENT_DEFINITION_HELP", "['$propertyKey']")
        value = "<div style='100%'>$value</div>"
        label.setText(HTMLUtilities.convertToHtml(value ? value : ""))
    }


}

