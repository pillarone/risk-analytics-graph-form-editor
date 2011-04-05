package org.pillarone.riskanalytics.graph.formeditor.application

import com.ulcjava.applicationframework.application.SingleFrameApplication
import com.ulcjava.base.application.util.Dimension

import org.pillarone.riskanalytics.graph.formeditor.ui.view.GraphModelEditor
import com.ulcjava.base.application.*

class FormEditorApplication extends SingleFrameApplication {

    @Override
    protected ULCComponent createStartupMainContent() {
        return getContentView()
    }

    @Override
    protected void initFrame(ULCFrame frame) {
        super.initFrame(frame)
        frame.defaultCloseOperation = ULCFrame.TERMINATE_ON_CLOSE
        frame.size = new Dimension(1000, 750)
        frame.locationRelativeTo = null
    }

    /**
     * Sets up the content of the main application window.
     * @return the component with the content of the main application window.
     */
    protected ULCComponent getContentView() {
        GraphModelEditor contentArea = new GraphModelEditor(getContext());

        ULCToolBar toolBar = contentArea.getToolBar();
        toolBar.add(ULCFiller.createHorizontalGlue());
        ULCComponent icon = new ULCLabel();
        icon.setName("logo.Label");
        toolBar.add(icon);
        toolBar.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 5));
        getMainView().setToolBar(toolBar);

        return contentArea.getContentView();
    }
}
