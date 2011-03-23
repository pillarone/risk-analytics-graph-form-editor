package org.pillarone.riskanalytics.graph.formeditor.application

import com.ulcjava.applicationframework.application.SingleFrameApplication
import com.ulcjava.base.application.util.Dimension
import org.pillarone.riskanalytics.graph.formeditor.ui.view.ComponentTypeTree
import org.pillarone.riskanalytics.graph.formeditor.ui.view.FormEditorModelsView
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
        ULCBoxPane modelEdit = new ULCBoxPane(true);
        ULCSeparator separator = new ULCSeparator();
        separator.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        modelEdit.add(ULCBoxPane.BOX_EXPAND_BOTTOM, ULCFiller.createVerticalStrut(3));
        modelEdit.add(ULCBoxPane.BOX_EXPAND_BOTTOM, separator);
        FormEditorModelsView contentView = new FormEditorModelsView(getContext());
        modelEdit.add(ULCBoxPane.BOX_EXPAND_EXPAND, contentView.getContentView());

        ComponentTypeTree treePane = new ComponentTypeTree();

        ULCSplitPane splitPane = new ULCSplitPane(ULCSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(200);
        splitPane.setDividerSize(5);
        splitPane.setLeftComponent(treePane);
        splitPane.setRightComponent(modelEdit);

        ULCToolBar toolBar = contentView.getToolBar();
        toolBar.add(ULCFiller.createHorizontalGlue());
        ULCComponent icon = new ULCLabel();
        icon.setName("logo.Label");
        toolBar.add(icon);
        toolBar.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 5));
        getMainView().setToolBar(toolBar);

        return splitPane;
    }
}
