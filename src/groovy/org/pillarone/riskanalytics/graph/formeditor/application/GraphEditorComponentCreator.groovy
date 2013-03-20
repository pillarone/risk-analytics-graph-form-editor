package org.pillarone.riskanalytics.graph.formeditor.application

import org.pillarone.riskanalytics.application.ui.extension.ComponentCreator
import com.ulcjava.base.application.ULCComponent
import com.ulcjava.base.application.BorderFactory
import com.ulcjava.base.application.ULCLabel
import com.ulcjava.base.application.ULCFiller
import com.ulcjava.base.application.ULCToolBar
import org.pillarone.riskanalytics.graph.formeditor.ui.view.GraphModelEditor
import com.ulcjava.applicationframework.application.ApplicationContext
import com.ulcjava.base.application.ULCBoxPane


class GraphEditorComponentCreator implements ComponentCreator {

    ULCComponent createComponent(ApplicationContext context) {
        ULCBoxPane content = new ULCBoxPane(1,2)

        GraphModelEditor contentArea = new GraphModelEditor(context);

        ULCToolBar toolBar = contentArea.getToolBar();
        toolBar.add(ULCFiller.createHorizontalGlue());
        ULCComponent icon = new ULCLabel();
        icon.setName("logo.Label");
        toolBar.add(icon);
        toolBar.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 5));

        content.add(ULCBoxPane.BOX_LEFT_TOP, toolBar)
        content.add(ULCBoxPane.BOX_EXPAND_EXPAND, contentArea.contentView)

        return content
    }
}
