package org.pillarone.riskanalytics.graph.formeditor.ui.model.palette

import com.ulcjava.base.application.tree.DefaultTreeModel
import org.pillarone.riskanalytics.graph.formeditor.ui.view.INameFilter
import com.ulcjava.base.application.tree.ITreeNode

import com.ulcjava.base.application.tree.TreePath

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */
class FilteringTreeModel extends DefaultTreeModel {

    DefaultTreeModel model
    INameFilter filter
    FilterTreeNode filteredRoot
    Map<ITreeNode, FilterTreeNode> nodeMapping = [:]

    public FilteringTreeModel(DefaultTreeModel treeModel) {
        super(treeModel.root)
        this.model = treeModel
        filteredRoot = new FilterTreeNode(originalNode: (ITreeNode) model.root)
        applyFilter()
    }



    public void setFilter(INameFilter nameFilter) {
        clearFilter()
        this.@filter = nameFilter
        applyFilter()
    }

    public void clearFilter() {
        this.@filter = null
        applyFilter()
    }

    public void applyFilter() {
        synchronizeFilteredTree((ITreeNode) model.root, filteredRoot)
    }

    protected void synchronizeFilteredTree(ITreeNode node, FilterTreeNode filteredNode) {
        nodeMapping[node] = filteredNode
        node?.childCount?.times {childIndex ->

            def childNode = node.getChildAt(childIndex)
            FilterTreeNode filteredChildNode = filteredNode.childNodes.find {it.originalNode == childNode}
            boolean nodeCurrentlyActive = filteredNode.activeIndices.contains(filteredNode.childNodes.indexOf(filteredChildNode))
            if (isAcceptedNode(childNode)) {
                if (!filteredChildNode) {
                    filteredChildNode = new FilterTreeNode(parent: filteredNode, originalChildIndex: childIndex, originalNode: childNode)

                    filteredNode.childNodes << filteredChildNode
                    filteredNode.activeIndices << filteredNode.childNodes.indexOf(filteredChildNode)
                    nodeMapping[childNode] = filteredChildNode
                    nodesWereInserted(new TreePath(getPathToRoot(node) as Object[]), [getIndexOfChild(node, childNode)] as int[])
                } else if (!nodeCurrentlyActive) {
                    filteredNode.activeIndices << filteredNode.childNodes.indexOf(filteredChildNode)
                    filteredNode.activeIndices = filteredNode.activeIndices.sort()
                    nodesWereInserted(new TreePath(getPathToRoot(node) as Object[]), [getIndexOfChild(node, childNode)] as int[])
                }
                synchronizeFilteredTree(childNode, filteredChildNode)
            } else {
                if (filteredChildNode && nodeCurrentlyActive) {
                    removeFilteredChildNodeIndex(filteredChildNode)
                }
            }
        }

        def iterator = filteredNode.childNodes.iterator()
        while (iterator.hasNext()) {
            FilterTreeNode filteredChildNode = iterator.next()
            if (node.getIndex(filteredChildNode.originalNode) < 0) {
                removeFilteredChildNode(filteredChildNode, iterator)
            }
        }
    }

    private def removeFilteredChildNodeIndex(FilterTreeNode filteredChildNode) {
        def filteredNode = filteredChildNode.parent
        int removedIndex = filteredNode.childNodes.indexOf(filteredChildNode)
        def activeIndicesIndex = filteredNode.activeIndices.indexOf(removedIndex)

        filteredNode.activeIndices.remove(activeIndicesIndex)
        nodesWereRemoved(new TreePath(getPathToRoot(filteredNode.originalNode) as Object[]), [activeIndicesIndex] as int[], [filteredChildNode.originalNode] as Object[])
    }

    private def removeFilteredChildNode(FilterTreeNode filteredChildNode, Iterator iterator) {
        def filteredNode = filteredChildNode.parent
        int removedIndex = filteredNode.childNodes.indexOf(filteredChildNode)
        def activeIndicesIndex = filteredNode.activeIndices.indexOf(removedIndex)
        if (activeIndicesIndex >= 0) {
            filteredNode.activeIndices.remove(activeIndicesIndex)
            nodesWereRemoved(new TreePath(getPathToRoot(filteredNode.originalNode) as Object[]), [activeIndicesIndex] as int[], [filteredChildNode.originalNode] as Object[])
        }
        iterator.remove()
        def indicesIterator = filteredNode.activeIndices.iterator()
        def newIndices = []
        while (indicesIterator.hasNext()) {
            Integer i = indicesIterator.next()
            if (i > removedIndex) {
                indicesIterator.remove()
                newIndices << --i
            }
        }
        for (int i in newIndices) {
            filteredNode.activeIndices << i
        }
        filteredNode.activeIndices = filteredNode.activeIndices.sort()

    }

    public Object getChild(Object parent, int index) {
        def filteredNode = nodeMapping[parent]
        int i = filteredNode.activeIndices[index]

        return filteredNode.childNodes[i].originalNode
    }

    public int getChildCount(Object parent) {
        return nodeMapping[parent].activeIndices.size();
    }

    public boolean isLeaf(Object node) {
        return node.childCount == 0;
    }

     public int getIndexOfChild(Object parent, Object child) {
        def filteredNode = nodeMapping[parent]
        int originalIndex = filteredNode.childNodes.indexOf(nodeMapping[child])
        return filteredNode.activeIndices.indexOf(originalIndex)
    }

    protected boolean isAcceptedNode(ITreeNode node) {
        if (filter == null) return true
        boolean nodeAccepted = filter.accept(node)
        if (!nodeAccepted) {
            node.childCount.times {
                nodeAccepted |= isAcceptedNode((node.getChildAt(it)))
            }
        }
        return nodeAccepted
    }

    Object methodMissing(String name, Object args) {
        model.getMetaClass().invokeMethod(model, name, args)
    }

    Object propertyMissing(String name) {
        model.getMetaClass().getProperty(model, name)
    }

    void propertyMissing(String name, Object args) {
        model.getMetaClass().setProperty(model, name, args)
    }
}
