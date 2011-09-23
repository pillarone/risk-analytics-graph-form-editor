package org.pillarone.riskanalytics.graph.formeditor.ui.view

import com.ulcjava.base.application.ULCBoxPane
import com.ulcjava.base.application.ULCButton
import com.ulcjava.base.application.ULCToolBar
import com.ulcjava.base.application.event.ActionEvent
import com.ulcjava.base.application.event.IActionListener
import com.ulcjava.base.application.util.Font
import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel
import org.pillarone.riskanalytics.graph.core.graph.model.ComponentNode
import org.pillarone.riskanalytics.graph.core.graph.model.GraphElement
import org.pillarone.riskanalytics.graph.core.graph.model.filters.IComponentNodeFilter
import org.pillarone.riskanalytics.graph.formeditor.ui.model.treetable.NodeNameFilter

/**
 *
 */
class BreadCrumbsModelSelector extends ULCBoxPane {

    LinkedList<GraphElement> currentPath
    Map<GraphElement, ULCButton> cachedLabels = [:]
    ULCToolBar breadcrumbPane
    HelpView helpView
    static Font LABELFONT = new Font("SansSerif", Font.PLAIN, 11);

    public BreadCrumbsModelSelector() {
        createView()
    }

    public void setCurrentPath(LinkedList<GraphElement> modelPath) {
        currentPath = modelPath
        showCurrentPath()
    }

    public void setGraphModel(AbstractGraphModel model) {
        graphModel = model
        LinkedList<GraphElement> path = new LinkedList<GraphElement>()
        path << model
        setCurrentPath(path)
    }

    void applyFilter(IComponentNodeFilter filter) {
        // Filter not applicable here
    }
    void applyFilter(NodeNameFilter filter) {
        // Filter not applicable here
    }

    private void createView() {
        breadcrumbPane = new ULCToolBar();
        breadcrumbPane.setOrientation(ULCToolBar.HORIZONTAL);
        breadcrumbPane.setFloatable(false);
        add breadcrumbPane
    }

    private void showCurrentPath() {
        clearBreadCrumbs()
        for (GraphElement el : currentPath) {
            ULCButton label = null
            if (!cachedLabels.containsKey (el)) {
                label = new ULCButton()
                label.setText(el.displayName)
                label.setFont(LABELFONT)
                final GraphElement elm = el
                label.addActionListener(new IActionListener() {
                    void actionPerformed(ActionEvent actionEvent) {
                        if (elm instanceof ComponentNode) {
                            helpView.showHelp((ComponentNode) elm)
                        } else if (elm instanceof AbstractGraphModel) {
                            helpView.showHelp((AbstractGraphModel) elm)
                        }
                    }
                })
                cachedLabels[el] = label
            } else {
                label = cachedLabels[el]
            }
            breadcrumbPane.add label
        }
    }

    private void clearBreadCrumbs() {
        breadcrumbPane.removeAll()
    }
}
