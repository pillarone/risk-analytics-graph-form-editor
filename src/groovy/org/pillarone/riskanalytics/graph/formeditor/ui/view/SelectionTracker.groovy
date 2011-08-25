package org.pillarone.riskanalytics.graph.formeditor.ui.view

import com.canoo.ulc.community.fixedcolumntabletree.server.ULCFixedColumnTableTree
import com.ulcjava.base.application.event.ITableTreeModelListener
import com.ulcjava.base.application.event.ITreeSelectionListener
import com.ulcjava.base.application.event.TableTreeModelEvent
import com.ulcjava.base.application.event.TreeSelectionEvent
import com.ulcjava.base.application.tree.TreePath
import com.ulcjava.base.shared.UlcEventCategories
import com.ulcjava.base.shared.UlcEventConstants
import com.ulcjava.base.application.ULCTableTree

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */
class SelectionTracker implements ITableTreeModelListener, ITreeSelectionListener {

    ULCTableTree tableTree
    TreePath selectedPath
    int selectedColumn




    public SelectionTracker(ULCTableTree tableTree) {
        this.tableTree = tableTree
        tableTree.selectionModel.addTreeSelectionListener this
        tableTree.selectionModel.setEventDeliveryMode(UlcEventCategories.SELECTION_CHANGED_EVENT_CATEGORY, UlcEventConstants.ASYNCHRONOUS_MODE)
        tableTree.setEventDeliveryMode(UlcEventCategories.SELECTION_CHANGED_EVENT_CATEGORY, UlcEventConstants.ASYNCHRONOUS_MODE)
    }

    public void tableTreeStructureChanged(TableTreeModelEvent tableTreeModelEvent) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void tableTreeNodeStructureChanged(TableTreeModelEvent tableTreeModelEvent) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void tableTreeNodesInserted(TableTreeModelEvent tableTreeModelEvent) {
        selectedAffectedNode(tableTreeModelEvent)
    }

    public void tableTreeNodesRemoved(TableTreeModelEvent tableTreeModelEvent) {
        selectedAffectedNode(tableTreeModelEvent)
    }

    private void selectedAffectedNode(TableTreeModelEvent tableTreeModelEvent) {
    }

    private restoreColumnSelection() {
        tableTree.setColumnSelectionInterval(selectedColumn, selectedColumn)
    }

    public void tableTreeNodesChanged(TableTreeModelEvent tableTreeModelEvent) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void valueChanged(TreeSelectionEvent event) {

        def currentSelection

        event.paths.each {pathsElement ->
            if (event.isAddedPath(pathsElement)) {
                currentSelection = pathsElement
            }
        }
        selectedColumn = tableTree.getSelectedColumn()
    }


}
