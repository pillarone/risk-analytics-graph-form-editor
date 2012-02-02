package org.pillarone.riskanalytics.graph.formeditor.ui.model.treetable

import com.ulcjava.base.application.event.ITableTreeModelListener
import com.ulcjava.base.application.event.TableTreeModelEvent
import com.ulcjava.base.application.tabletree.AbstractTableTreeModel
import com.ulcjava.base.application.tabletree.ITableTreeNode
import com.ulcjava.base.application.tree.TreePath
import org.pillarone.riskanalytics.graph.core.graph.model.filters.IComponentNodeFilter
import org.pillarone.riskanalytics.graph.core.graph.model.*

class FilteringTableTreeModel extends AbstractTableTreeModel implements ITableTreeModelListener {

    AbstractTableTreeModel model
    Object filter
    FilterTableTreeNode filteredRoot
    def nodeMapping = [:]

    public FilteringTableTreeModel(AbstractTableTreeModel model, Object filter) {
        this.@model = model
        this.@filter = filter
        filteredRoot = new FilterTableTreeNode(originalNode: (ITableTreeNode) model.root)
        applyFilter()
        this.@model.addTableTreeModelListener this
        addGraphModelChangeListener(model)
    }

    private void addGraphModelChangeListener(NodesTableTreeModel model) {
        final IGraphModelChangeListener graphListener = new GraphListener()
        model.getGraphModel().addGraphModelChangeListener(graphListener)
    }

    private void addGraphModelChangeListener(AbstractTableTreeModel model) {

    }

    public void setFilter(Object newFilter) {
        this.@filter = newFilter
        applyFilter()
    }

    public void applyFilter() {
        synchronizeFilteredTree(model.root, filteredRoot)
    }

    private void synchronizeTreePath(TreePath path) {
        ITableTreeNode node = (ITableTreeNode) findValidSynchronizationStart(path)

        synchronizeFilteredTree(node, (FilterTableTreeNode) nodeMapping[node])
    }

    private def findValidSynchronizationStart(TreePath path) {
        ITableTreeNode node = (ITableTreeNode) path.lastPathComponent
        while (!(nodeMapping[node] && isAcceptedNode(node)) && node.parent != null) {
            node = node.parent
        }
        return node
    }



    protected void synchronizeFilteredTree(ITableTreeNode node, FilterTableTreeNode filteredNode) {
        nodeMapping[node] = filteredNode

        // check whether the (original) child node fulfills the filter criterium
        node.childCount.times {childIndex ->
            ITableTreeNode childNode = node.getChildAt(childIndex)
            FilterTableTreeNode filteredChildNode = filteredNode.childNodes.find {it.originalNode == childNode}
            boolean nodeCurrentlyActive = filteredNode.activeIndices.contains(filteredNode.childNodes.indexOf(filteredChildNode))
            if (isAcceptedNode(childNode)) {
                if (!filteredChildNode) {
                    filteredChildNode = new FilterTableTreeNode(parent: filteredNode, originalChildIndex: childIndex, originalNode: childNode)
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

        // remove filteredChildNodes fir which there is no original node any more
        def iterator = filteredNode.childNodes.iterator()
        while (iterator.hasNext()) {
            def filteredChildNode = iterator.next()
            if (node.getIndex(filteredChildNode.originalNode) < 0) {
                removeFilteredChildNode(filteredChildNode, iterator)
            }
        }
    }

    private def removeFilteredChildNodeIndex(FilterTableTreeNode filteredChildNode) {
        def filteredNode = filteredChildNode.parent
        int removedIndex = filteredNode.childNodes.indexOf(filteredChildNode)
        def activeIndicesIndex = filteredNode.activeIndices.indexOf(removedIndex)

        filteredNode.activeIndices.remove(activeIndicesIndex)
        nodesWereRemoved(new TreePath(getPathToRoot(filteredNode.originalNode) as Object[]), [activeIndicesIndex] as int[], [filteredChildNode.originalNode] as Object[])
    }

    private def removeFilteredChildNode(FilterTableTreeNode filteredChildNode, Iterator iterator) {
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

    /**
     * Returns the path to the root.
     *
     * @param node the node to get the path for
     * @return the path to the root
     */
    public static ITableTreeNode[] getPathToRoot(ITableTreeNode node) {
        List result = new ArrayList();
        result.add(node);
        while (node.getParent() != null) {
            node = node.getParent();
            result.add(node);
        }

        Collections.reverse(result);
        return (ITableTreeNode[]) result.toArray(new ITableTreeNode[result.size()]);
    }


    public int getColumnCount() {
        return model.columnCount;
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
        FilterTableTreeNode filteredNode = (FilterTableTreeNode) nodeMapping[parent]
        int i = filteredNode.activeIndices[index]
        return filteredNode.childNodes[i].originalNode
    }

    public int getChildCount(Object parent) {
        return ((FilterTableTreeNode) nodeMapping[parent]).activeIndices.size();
    }

    public boolean isLeaf(Object node) {
        return ((ITableTreeNode) node).childCount == 0;
    }

    public int getIndexOfChild(Object parent, Object child) {
        FilterTableTreeNode filteredNode = (FilterTableTreeNode) nodeMapping[parent]
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

    public void tableTreeStructureChanged(TableTreeModelEvent event) {
        tableTreeNodeStructureChanged(event)
    }

    public void tableTreeNodeStructureChanged(TableTreeModelEvent event) {
        synchronizeTreePath(event.treePath)
    }

    public void tableTreeNodesInserted(TableTreeModelEvent event) {
        synchronizeTreePath(event.treePath)
    }

    public void tableTreeNodesRemoved(TableTreeModelEvent event) {
        synchronizeTreePath(event.treePath)
    }

    public void tableTreeNodesChanged(TableTreeModelEvent event) {
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

    protected TreePath extractPath(TableTreeModelEvent event) {
        def pathElements = event.getTreePath().getPath().toList()
        def elements = new ArrayList(pathElements)
        if (event.children) {
            elements.addAll(event.children.toList())
        }

        TreePath path = new TreePath(elements as Object[])
        return path
    }

    protected boolean isAcceptedNode(ITableTreeNode node) {
        boolean nodeAccepted = filter.acceptNode(node) || (node.parent && filter.acceptNode(node.parent))
        ITableTreeNode parent = node.parent
        //node will be showed if at leasr one of its parent or child is accepted
        while (parent && !nodeAccepted) {
            nodeAccepted = parent.parent && filter.acceptNode(parent.parent)
            parent = parent.parent
        }
        if (!nodeAccepted) {
            node.childCount.times {
                nodeAccepted |= isAcceptedNode((node.getChildAt(it)))
            }
        }
        return nodeAccepted
    }

    protected boolean isAcceptedNode(GraphElementNode node) {
        boolean nodeAccepted = false
        GraphElement element = ((GraphElementNode) node).element
        if ((element instanceof ComponentNode) || (element instanceof Port)) {
            nodeAccepted = filter.isSelected(element)
        } else {
            if (node.parent && isAcceptedNode(node.parent)) {
                nodeAccepted = true
            } else {
                nodeAccepted = false
            }
        }
        /*if (!nodeAccepted) {
            node.childCount.times {
                nodeAccepted |= isAcceptedNode((node.getChildAt(it)))
            }
        }*/
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

    private class GraphListener implements IGraphModelChangeListener {

        public void connectionAdded(Connection c) {
            GraphElementNode from = model.findNode(c.from)
            from.updateColumnValues()
            nodeChanged(model.getTreePath(from));
            GraphElementNode toNode = model.findNode(c.to)
            toNode.updateColumnValues()
            nodeChanged(model.getTreePath(toNode));
        }

        public void connectionRemoved(Connection c) {
            GraphElementNode from = model.findNode(c.from)
            from.updateColumnValues()
            nodeChanged(model.getTreePath(from));
            GraphElementNode toNode = model.findNode(c.to)
            toNode.updateColumnValues()
            nodeChanged(model.getTreePath(toNode));
        }

        public void nodeAdded(ComponentNode node) {
            GraphElementNode tableNode = model.addChild(getRoot(), node);
            TreePath selectedPath = new TreePath([getRoot(), tableNode] as Object[]);
            // expandPaths([selectedPath] as TreePath[], true)
        }

        public void nodeRemoved(ComponentNode node) {
            GraphElementNode treeNode = model.findNode(node);
            model.removeChild(getRoot(), treeNode);
        }

        public void outerPortAdded(Port p) {
            model.addChild(getRoot(), p);
        }

        public void outerPortRemoved(Port p) {
            GraphElementNode treeNode = model.findNode(p);
            model.removeChild(getRoot(), treeNode);
        }


        public void nodePropertyChanged(ComponentNode node, String propertyName, Object oldValue, Object newValue) {
            GraphElementNode treeNode = model.findNode(node);
            treeNode.updateColumnValues();
            nodeChanged(model.getTreePath(treeNode));
            //nodeChanged(model.getTreePath(treeNode), model.NAMEID);
        }
    }
}

class FilterTableTreeNode {

    FilterTableTreeNode parent
    List<FilterTableTreeNode> childNodes
    List<Integer> activeIndices
    int originalChildIndex
    ITableTreeNode originalNode

    public FilterTableTreeNode() {
        childNodes = []
        activeIndices = []
    }

}

interface INodeFilter extends IComponentNodeFilter {

    public boolean isSelected(ITableTreeNode node)

}