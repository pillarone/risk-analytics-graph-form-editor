package org.pillarone.riskanalytics.graph.formeditor.ui.handlers;

import com.ulcjava.base.application.ULCComponent;
import com.ulcjava.base.application.dnd.DataFlavor;
import com.ulcjava.base.application.dnd.DnDTreeData;
import com.ulcjava.base.application.dnd.TransferHandler;
import com.ulcjava.base.application.dnd.Transferable;
import com.ulcjava.base.application.tree.DefaultMutableTreeNode;
import com.ulcjava.base.application.tree.TreePath;
import org.pillarone.riskanalytics.graph.formeditor.ui.view.SingleModelEditView;
import org.pillarone.riskanalytics.graph.formeditor.util.ComponentTypeTreeUtilities;

public class TypeTransferHandler extends TransferHandler {

    private SingleModelEditView fModelEditView;

    public SingleModelEditView getModelEditView() {
        return fModelEditView;
    }

    public void setModelEditView(SingleModelEditView modelEditView) {
        this.fModelEditView = modelEditView;
    }

    @Override
    public boolean importData(ULCComponent targetComponent, Transferable transferable) {
        Object dragData0 = transferable.getTransferData(DataFlavor.DRAG_FLAVOR);
        DnDTreeData dragData = (DnDTreeData) dragData0;
        TreePath[] paths = dragData.getTreePaths();
        String componentType = ComponentTypeTreeUtilities.getComponentTypeName(paths[0]);
        if (fModelEditView == null) {
            return false;
        } else {
            fModelEditView.newNodeAction(componentType);
            return true;
        }
    }

    /**
     * Do nothing on the export side
     */
    @Override
    public void exportDone(ULCComponent src, Transferable t, int action) {
    }


}
