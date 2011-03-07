package org.pillarone.riskanalytics.graph.formeditor.application

import com.ulcjava.base.application.AbstractApplication
import com.ulcjava.base.application.ULCFrame
import com.ulcjava.base.application.util.Dimension
import org.pillarone.riskanalytics.graph.formeditor.ui.model.FormEditorModel
import org.pillarone.riskanalytics.graph.formeditor.ui.view.FormEditorView

class FormEditorApplication extends AbstractApplication {

    ULCFrame mainFrame = new ULCFrame("Form Editor")

    public void start() {
        mainFrame.defaultCloseOperation = ULCFrame.TERMINATE_ON_CLOSE
        mainFrame.size = new Dimension(1000, 750)

        //If argument is null, the window is centered on the screen.
        mainFrame.locationRelativeTo = null
        FormEditorView mainView = new FormEditorView(new FormEditorModel())
        mainFrame.contentPane.add(mainView.content)
        mainFrame.menuBar = mainView.menuBar
        mainFrame.visible = true
        mainFrame.toFront()
    }

}
