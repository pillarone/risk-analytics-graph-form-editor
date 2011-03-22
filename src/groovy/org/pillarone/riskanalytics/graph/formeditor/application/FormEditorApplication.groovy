package org.pillarone.riskanalytics.graph.formeditor.application

import com.ulcjava.applicationframework.application.SingleFrameApplication
import com.ulcjava.base.application.ULCComponent
import com.ulcjava.base.application.ULCFrame
import com.ulcjava.base.application.ULCMenuBar
import com.ulcjava.base.application.util.Dimension
import org.pillarone.riskanalytics.graph.formeditor.ui.model.FormEditorModel
import org.pillarone.riskanalytics.graph.formeditor.ui.view.FormEditorView

class FormEditorApplication extends SingleFrameApplication {

    FormEditorView mainView

    FormEditorApplication() {
        mainView = new FormEditorView(new FormEditorModel())
    }

    @Override
    protected void initFrame(ULCFrame frame) {
        super.initFrame(frame)
        frame.defaultCloseOperation = ULCFrame.TERMINATE_ON_CLOSE
        frame.size = new Dimension(1000, 750)
        frame.locationRelativeTo = null
    }

    @Override
    protected ULCComponent createStartupMainContent() {
        return mainView.content
    }

    @Override
    protected ULCMenuBar createStartupMenuBar() {
        return mainView.menuBar
    }


}
