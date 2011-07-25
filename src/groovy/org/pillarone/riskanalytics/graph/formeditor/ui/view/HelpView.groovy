package org.pillarone.riskanalytics.graph.formeditor.ui.view

import com.ulcjava.base.application.ULCBoxPane
import com.ulcjava.base.application.util.HTMLUtilities
import org.pillarone.riskanalytics.graph.core.graph.util.UIUtils
import com.canoo.ulc.graph.ULCGraph
import com.canoo.ulc.graph.model.Vertex
import com.ulcjava.base.application.ULCFiller
import com.ulcjava.base.application.ULCHtmlPane
import com.ulcjava.base.application.util.Color

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */
class HelpView implements IVertexHelpListener {

    ULCBoxPane content
    ULCHtmlPane label
    ULCGraph fULCGraph

    public HelpView(ULCGraph fULCGraph) {
        this.fULCGraph = fULCGraph
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
        label = new ULCHtmlPane()
    }

    private void layoutComponents() {
        content.add(ULCBoxPane.BOX_LEFT_TOP, label)
        content.add(ULCBoxPane.BOX_EXPAND_EXPAND, new ULCFiller())
    }

    private void attachListeners() {
        VertexSelectionListener listener = new VertexSelectionListener()
        listener.addVertexHelpListener(this)
        fULCGraph.getSelectionModel().addGraphSelectionListener(listener);
    }

    public void updateView() {
        Set<Vertex> selectedVertices = fULCGraph.getSelectionModel().getSelectedVertices();
        if (selectedVertices.size() == 1) {
            String templateId = ((Vertex) selectedVertices.toArray()[0]).getTemplateId();
            String value = UIUtils.getPropertyValue(null, "COMPONENT_DEFINITION_HELP", "['$templateId']")
            label.setText(HTMLUtilities.convertToHtml(value ? value : ""))
        }

    }

}

