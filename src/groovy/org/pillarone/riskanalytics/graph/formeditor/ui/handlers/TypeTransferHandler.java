package org.pillarone.riskanalytics.graph.formeditor.ui.handlers;

import com.ulcjava.base.application.ULCComponent;
import com.ulcjava.base.application.dnd.DataFlavor;
import com.ulcjava.base.application.dnd.DnDTreeData;
import com.ulcjava.base.application.dnd.TransferHandler;
import com.ulcjava.base.application.dnd.Transferable;
import com.ulcjava.base.application.tree.TreePath;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.TypeTreeNode;
import org.pillarone.riskanalytics.graph.formeditor.ui.view.GraphModelEditable;
import org.pillarone.riskanalytics.graph.formeditor.util.ComponentTypeTreeUtilities;

public class TypeTransferHandler extends TransferHandler {

    private GraphModelEditable fModelEditView;

    public GraphModelEditable getModelEditView() {
        return fModelEditView;
    }

    public void setModelEditView(GraphModelEditable modelEditView) {
        this.fModelEditView = modelEditView;
    }

    @Override
    public boolean importData(ULCComponent targetComponent, Transferable transferable) {
        Object dragData0 = transferable.getTransferData(DataFlavor.DRAG_FLAVOR);
        DnDTreeData dragData = (DnDTreeData) dragData0;
        TreePath[] paths = dragData.getTreePaths();
        Object selected = paths[0].getLastPathComponent();
        if (selected instanceof TypeTreeNode) {
            String componentType = ((TypeTreeNode) selected).getPackagePath();
            if (componentType != null && fModelEditView != null) {
                fModelEditView.newNodeAction(componentType);
                return true;
            }
        }
        return false;
    }

    /**
     * Do nothing on the export side
     */
    @Override
    public void exportDone(ULCComponent src, Transferable t, int action) {
    }
}
