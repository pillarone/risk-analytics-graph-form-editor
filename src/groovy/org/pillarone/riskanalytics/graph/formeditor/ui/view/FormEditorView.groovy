package org.pillarone.riskanalytics.graph.formeditor.ui.view

import org.pillarone.riskanalytics.graph.formeditor.ui.model.FormEditorModel
import com.ulcjava.base.application.ULCBoxPane
import com.ulcjava.base.application.ULCMenuBar


class FormEditorView {

    protected FormEditorModel model

    ULCBoxPane content
    ULCMenuBar menuBar


    FormEditorView(FormEditorModel model) {
        this.model = model
        initComponents()
    }

    private void initComponents() {
        content = new ULCBoxPane()
    }
}
