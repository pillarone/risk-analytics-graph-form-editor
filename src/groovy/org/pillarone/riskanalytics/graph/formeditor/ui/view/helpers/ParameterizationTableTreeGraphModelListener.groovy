package org.pillarone.riskanalytics.graph.formeditor.ui.view.helpers

import com.ulcjava.base.application.tabletree.DefaultTableTreeModel
import com.ulcjava.base.application.tree.TreePath
import org.pillarone.riskanalytics.application.ui.base.model.ComponentTableTreeNode
import org.pillarone.riskanalytics.application.ui.base.model.SimpleTableTreeNode
import org.pillarone.riskanalytics.application.ui.parameterization.model.ParameterizationTableTreeModel
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.graph.core.graph.model.ComponentNode
import org.pillarone.riskanalytics.graph.core.graph.model.Connection
import org.pillarone.riskanalytics.graph.core.graph.model.IGraphModelChangeListener
import org.pillarone.riskanalytics.graph.core.graph.model.Port

class ParameterizationTableTreeGraphModelListener implements IGraphModelChangeListener {

    ParameterizationTableTreeModel treeModel

    ParameterizationTableTreeGraphModelListener(ParameterizationTableTreeModel treeModel) {
        this.treeModel = treeModel
    }

    void connectionAdded(Connection c) {

    }

    void connectionRemoved(Connection c) {

    }

    void nodeAdded(ComponentNode node) {
        Component component = node.type.typeClass.newInstance()
        SimpleTableTreeNode root = getRoot()
        component.name = node.name
        if (component.hasParameters()) {
            final ComponentTableTreeNode newNode = treeModel.builder.createNewComponentNode(root, component)
            treeModel.nodesWereInserted(new TreePath(DefaultTableTreeModel.getPathToRoot(root) as Object[]), [root.getIndex(newNode)] as int[])
        }
    }

    private SimpleTableTreeNode getRoot() {
        return (SimpleTableTreeNode) treeModel.root
    }

    void nodeRemoved(ComponentNode node) {
        final SimpleTableTreeNode root = getRoot()

        final ComponentTableTreeNode nodeToRemove = root.children.find {
            if (it instanceof ComponentTableTreeNode) {
                return it.component.name == node.name
            }
            return false
        }

        if(nodeToRemove != null) {
            final int index = root.getIndex(nodeToRemove)
            root.remove(index)
            treeModel.builder.removeParameterFromNodes(nodeToRemove)
            treeModel.nodesWereRemoved(new TreePath(DefaultTableTreeModel.getPathToRoot(root)), [index] as int[], [nodeToRemove] as Object[])
        }

    }

    void outerPortAdded(Port p) {

    }

    void outerPortRemoved(Port p) {

    }

    void nodePropertyChanged(ComponentNode node, String propertyName, Object oldValue, Object newValue) {

    }

}
