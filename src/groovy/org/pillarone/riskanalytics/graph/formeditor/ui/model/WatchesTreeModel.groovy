package org.pillarone.riskanalytics.graph.formeditor.ui.model

import com.ulcjava.base.application.tabletree.AbstractTableTreeModel
import com.ulcjava.base.application.tabletree.ITableTreeModel
import com.ulcjava.base.application.tabletree.ITableTreeNode
import com.ulcjava.base.application.tree.TreePath
import java.util.Map.Entry
import org.pillarone.riskanalytics.graph.formeditor.ui.IWatchList
import org.pillarone.riskanalytics.graph.formeditor.util.UIUtils

/**
 *
 */
class WatchesTreeModel extends AbstractTableTreeModel implements ITableTreeModel, IWatchList {

    private ParentNode fRoot
    private List<String> fPeriodLabels

    public WatchesTreeModel() {
        super()
        fRoot = new ParentNode("Watches", null)
        fPeriodLabels = [1]
    }

    public void addWatch(String path) {
        if (!fRoot.hasChild(path)) {
            ParentNode node = new ParentNode(path, fRoot)
            fRoot.addChild(path, node)
            structureChanged()
            //nodesWereInserted(new TreePath([fRoot] as Object[], [fRoot.childCount-1] as int[]));

        }
    }

    public void removeWatch(String path) {
        Map watchesToRemove = fRoot.children.findAll {entry -> entry.key == path}
        List<Integer> indices = []
        List<ITableTreeNode> nodes = []
        watchesToRemove.each {key, value ->
            indices << fRoot.getIndex(value)
            nodes << value
            fRoot.children.remove(key)
        }
        nodesWereRemoved(new TreePath([fRoot] as Object[]), indices as int[], nodes as ITableTreeNode[])
    }

    void editWatch(String oldPath, String newPath) {
        if (fRoot && fRoot.children) {
            Map watchesToEdit = fRoot.children.findAll {entry -> ((String) entry.key).startsWith(oldPath)}
            watchesToEdit.keySet().each {String watchOldPath ->
                String suffixPath = watchOldPath.substring(oldPath.length())
                addWatch(newPath + suffixPath)
                removeWatch(watchOldPath)
            }
        }
    }

    public void removeAllWatches() {
        fRoot.children = [:]
        nodeStructureChanged(new TreePath([fRoot] as Object[]))
    }

    public void injectData(Map simulationResults, List<String> periodLabels) {
        if (periodLabels != fPeriodLabels) {
            fPeriodLabels = periodLabels
        }
        for (Entry entry: fRoot.children) {
            ParentNode watchNode = entry.value
            Map dataNode = simulationResults[entry.key]
            if (dataNode) {
                for (Entry fieldEntry: dataNode.entrySet()) {
                    if (!watchNode.hasChild(fieldEntry.key)) {
                        ParentNode fieldNode = new ParentNode(fieldEntry.key, watchNode)
                        watchNode.addChild(fieldEntry.key, fieldNode)
                    }
                    ParentNode fieldNode = (ParentNode) watchNode.children[fieldEntry.key]
                    fieldNode.children = [:]
                    createDataSubTree(fieldNode, (Map) fieldEntry.value)
                }
                nodeStructureChanged(new TreePath([fRoot, watchNode] as Object[]));
            } else {
                watchNode.children = [:]
                nodeStructureChanged(new TreePath([fRoot, watchNode] as Object[]));
            }
        }
    }

    class DataNode implements ITableTreeNode {
        int index
        ITableTreeNode parentNode
        Map values = [:]

        DataNode(int index, ParentNode parent) {
            this.index = index
            this.parentNode = parent
        }

        ITableTreeNode getChildAt(int i) { throw new IndexOutOfBoundsException("No children attached to this data node in the data tree.") }

        int getChildCount() { return 0 }

        ITableTreeNode getParent() { return parentNode }

        int getIndex(ITableTreeNode child) { throw new IndexOutOfBoundsException("No children attached to this data node in the data tree.") }

        Object getValueAt(int column) { return column == 0 ? index : values[column - 1]}

        void setValueAt(int column, Object value) { throw new RuntimeException("No values can be set in the simulation result table.")}

        boolean isLeaf() { return true }
    }

    class ParentNode implements ITableTreeNode {
        def id
        String displayName
        Map<Object, ITableTreeNode> children = [:]
        ParentNode parent;

        ParentNode(def nodeName, ParentNode parent) {
            this.id = nodeName
            this.displayName = UIUtils.getWatchesDisplayName((String)nodeName)
            this.parent = parent
        }

        ITableTreeNode getChildAt(int i) { return children.values().asList()[i] }

        int getChildCount() { return children.size() }

        ITableTreeNode getParent() { return parent}

        int getIndex(ITableTreeNode child) {
            int index = 0
            for (Entry e: children.entrySet()) {
                if (e.value == child) {
                    return index
                }
                index++
            }
            throw new RuntimeException("Child not found.")
        }

        Object getValueAt(int column) { return column == 0 ? displayName : "" }

        boolean isLeaf() { return false }

        void addChild(def id, ITableTreeNode child) {
            children[id] = child
        }

        boolean hasChild(def id) {
            return children.containsKey(id)
        }
    }

    private void createDataSubTree(ParentNode fieldNode, Map data) {
        // determine for each iteration the number of cells to reserve
        int numOfPeriods = data.size()
        int n = 0
        for (int period = 0; period < numOfPeriods; period++) {
            n = Math.max(n, ((List) data[period][1]).size())
        }
        for (int i = 0; i < n; i++) {
            DataNode dataNode = new DataNode(i, fieldNode)
            fieldNode.addChild(i, dataNode)
        }
        for (int period = 0; period < numOfPeriods; period++) {
            List singleValues = (List) data[period][1]
            for (int i = 0; i < singleValues.size(); i++) {
                ((DataNode) fieldNode.getChildAt(i)).values.put(period, singleValues[i])
            }
        }
    }

    public int getColumnCount() {
        return fPeriodLabels.size() + 1
    }

    public Object getValueAt(Object node, int column) {
        return ((ITableTreeNode) node).getValueAt(column)
    }

    public void setValueAt(Object node, Object value, int column) {
        throw new RuntimeException("")
    }

    /*public Class getColumnClass(int column) {
        return column==0 ? String.class : Object.class
    }*/

    public boolean isCellEditable(Object node, int column) {
        return false
    }

    public Object getRoot() {
        return fRoot
    }

    public Object getChild(Object parent, int i) {
        return ((ITableTreeNode) parent).getChildAt(i)
    }

    public int getChildCount(Object parent) {
        return ((ITableTreeNode) parent).getChildCount()
    }

    public boolean isLeaf(Object node) {
        return ((ITableTreeNode) node).leaf
    }

    public int getIndexOfChild(Object parent, Object child) {
        return ((ITableTreeNode) parent).getIndex((ITableTreeNode) child)
    }

    public String getColumnName(int column) {
        return column == 0 ? "Name" : fPeriodLabels[column - 1]
    }
}
