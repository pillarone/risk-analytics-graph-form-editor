package org.pillarone.riskanalytics.graph.formeditor.ui.model.palette

import com.ulcjava.base.application.tree.AbstractTreeModel
import com.ulcjava.base.application.event.ITreeModelListener
import com.ulcjava.base.application.tree.ITreeNode
import com.ulcjava.base.application.tree.ITreeModel
import com.ulcjava.base.application.tree.TreePath
import com.ulcjava.base.application.event.TreeModelEvent


class FilteringTreeModel extends AbstractTreeModel implements ITreeModelListener {

    ITreeModel model
    ITreeFilter filter
    FilterTreeNode filteredRoot
    Map<ITreeNode, FilterTreeNode> nodeMapping = [:]

    public FilteringTreeModel(ITreeModel model, ITreeFilter filter) {
        this.@model = model
        this.@filter = filter
        filteredRoot = new FilterTreeNode(originalNode: (ITreeNode) model.root)
        applyFilter()
        this.@model.addTreeModelListener(this)
    }

    public void setFilter(ITreeFilter newFilter) {
        this.@filter = newFilter
        applyFilter()
    }

    public void applyFilter() {
        synchronizeFilteredTree((ITreeNode) model.root, filteredRoot)
    }

    protected void reapplyFilter() {
        synchronizeFilteredTree((ITreeNode) model.root, filteredRoot)
    }

    private void synchronizeTreePath(TreePath path) {
        def node = findValidSynchronizationStart(path)

        synchronizeFilteredTree(node, nodeMapping[node])
    }

    private def findValidSynchronizationStart(TreePath path) {
        ITreeNode node = (ITreeNode) path.lastPathComponent
        while (!(nodeMapping[node] && isAcceptedNode(node)) && node.parent != null) {
            node = node.parent
        }
        return node
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
                    addFilterChildNode(filteredNode, filteredChildNode)
                    addActiveIndex(filteredNode, filteredNode.childNodes.indexOf(filteredChildNode))
                    nodeMapping[childNode] = filteredChildNode
                    nodesWereInserted(new TreePath(getPathToRoot(node) as Object[]), [getIndexOfChild(node, childNode)] as int[])
                } else if (!nodeCurrentlyActive) {
                    addActiveIndex(filteredNode, filteredNode.childNodes.indexOf(filteredChildNode))
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

    protected addFilterChildNode(FilterTreeNode parent, FilterTreeNode child) {
        final int insertIndex = model.getIndexOfChild(parent.originalNode, child.originalNode)
        parent.childNodes.add(insertIndex, child)
        List<Integer> newIndices = []
        for(Integer i in parent.activeIndices) {
            if(i >= insertIndex) {
                i++
            }
            newIndices << i
        }
        parent.activeIndices = newIndices.sort()
    }

    protected void addActiveIndex(FilterTreeNode filterTreeNode, int index) {
        if (!filterTreeNode.activeIndices.contains(index)) {
            filterTreeNode.activeIndices << index
        }
        filterTreeNode.activeIndices = filterTreeNode.activeIndices.sort()
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
            addActiveIndex(filteredNode, i)
        }
        filteredNode.activeIndices = filteredNode.activeIndices.sort()

    }

    /**
     * Returns the path to the root.
     *
     * @param node the node to get the path for
     * @return the path to the root
     */
    private ITreeNode[] getPathToRoot(ITreeNode node) {
        List result = new ArrayList();
        result.add(node);
        while (node.getParent() != null) {
            node = node.getParent();
            result.add(node);
        }

        Collections.reverse(result);
        return (ITreeNode[]) result.toArray(new ITreeNode[result.size()]);
    }


    public int getColumnCount() {
        return model.columnCount;
    }

    public void setColumnCount(int newColumnCount) {
        model.columnCount = newColumnCount
    }

    public String getColumnName(int column) {
        model.getColumnName(column)
    }

    public Object getValueAt(Object node, int column) {
        return model.getValueAt(node, column)
    }

    public Object getRoot() {
        return model.root;
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

    public boolean isCellEditable(Object node, int columnIndex) {
        return model.isCellEditable(node, columnIndex);
    }

    public void setValueAt(Object value, Object node, int column) {
        model.setValueAt(value, node, column);
    }

    // event forwarding by reapplying the filter

    public void treeStructureChanged(TreeModelEvent event) {
        treeNodeStructureChanged(event)
    }

    public void treeNodeStructureChanged(TreeModelEvent event) {
        synchronizeTreePath(event.treePath)
    }

    public void treeNodesInserted(TreeModelEvent event) {
        synchronizeTreePath(event.treePath)
    }

    public void treeNodesRemoved(TreeModelEvent event) {
        synchronizeTreePath(event.treePath)
    }

    public void treeNodesChanged(TreeModelEvent event) {
        List childIndices = []
        event.children.each {
            int childIndex = getIndexOfChild(event.treePath.lastPathComponent, it)
            if (childIndex >= 0) {
                childIndices << childIndex
            }
        }
        if (!childIndices.empty) {
            nodesChanged(event.treePath, childIndices as int[])
        }
    }

    protected TreePath extractPath(TreeModelEvent event) {
        def pathElements = event.getTreePath().getPath().toList()
        def elements = new ArrayList(pathElements)
        if (event.children) {
            elements.addAll(event.children.toList())
        }

        TreePath path = new TreePath(elements as Object[])
        return path
    }

    protected boolean isAcceptedNode(ITreeNode node) {
        boolean nodeAccepted = filter.acceptNode(node)
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

interface ITreeFilter {

    boolean acceptNode(ITreeNode node)

}
