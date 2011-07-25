package org.pillarone.riskanalytics.graph.formeditor.ui.view

import com.canoo.ulc.graph.IGraphSelectionListener

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */

class VertexSelectionListener implements IGraphSelectionListener {
    List<IVertexHelpListener> vertexHelpListeners = new ArrayList<IVertexHelpListener>();

    public void selectionChanged() {
        for (IVertexHelpListener helpListener : vertexHelpListeners) {
            helpListener.updateView();
        }
    }

    public void addVertexHelpListener(IVertexHelpListener listener) {
        vertexHelpListeners.add(listener);
    }

    public void removeVertexHelpListener(IVertexHelpListener listener) {
        vertexHelpListeners.remove(listener);
    }
}
