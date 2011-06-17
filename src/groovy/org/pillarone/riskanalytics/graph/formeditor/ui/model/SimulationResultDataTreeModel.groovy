package org.pillarone.riskanalytics.graph.formeditor.ui.model

import org.pillarone.riskanalytics.graph.core.graph.model.ModelGraphModel
import com.ulcjava.base.application.tabletree.ITableTreeNode
import com.ulcjava.base.application.tabletree.ITableTreeModel
import com.ulcjava.base.application.tabletree.AbstractTableTreeModel
import java.util.Map.Entry

/**
 *
 */
class SimulationResultDataTreeModel extends AbstractTableTreeModel  implements ITableTreeModel {

    private static final String PATHSEP = ':'
    private ParentNode fRoot

    public SimulationResultDataTreeModel(Map simulationResults) {
        super()
        createTree(simulationResults)
    }

    class DataNode implements ITableTreeNode {
        int index
        ParentNode parentNode
        Map values = [:]

        DataNode(int index, ParentNode parent) {
            this.index = index
            this.parentNode = parent
        }

        ITableTreeNode getChildAt(int i) { throw new IndexOutOfBoundsException("No children attached to this data node in the data tree.") }
        int getChildCount() { return 0 }
        ITableTreeNode getParent() { return parentNode }
        int getIndex(ITableTreeNode child) { throw new IndexOutOfBoundsException("No children attached to this data node in the data tree.") }
        Object getValueAt(int column) { return  column==0 ? index : values[column-1]}
        void setValueAt(int column, Object value) { throw new RuntimeException("No values can be set in the simulation result table." )}
        boolean isLeaf() { return true }
    }

    class ParentNode implements ITableTreeNode {
        def id
        ParentNode parentNode
        Map<Object, ITableTreeNode> children = [:]

        ParentNode(ParentNode parent, def nodeName) {
            this.id = nodeName
            this.parentNode = parent
        }

        ITableTreeNode getChildAt(int i) { return children.values().asList()[i] }
        int getChildCount() { return children.size() }
        ITableTreeNode getParent() { return parentNode }
        int getIndex(ITableTreeNode child) {
            int index = 0
            for (Entry e : children.entrySet()) {
                if (e.value==child) {
                    return index
                }
                index++
            }
            throw new RuntimeException("Child not found.")
        }
        Object getValueAt(int column) { return column==0 ? id : "" }
        boolean isLeaf() { return false }
        void addChild(def id, ITableTreeNode child) {
            children[id] = child
        }
        boolean hasChild(def id) {
            return children.containsKey(id)
        }
    }

    // methods to setup, manipulate and search the tree

    public void createTree(Map simulationResults) {
        fRoot = new ParentNode(null, "Results")
        for (Entry entry : simulationResults.entrySet()) {
            ParentNode packetNode = getNode((String) entry.key)
            for (Entry fieldEntry : ((Map)entry.value).entrySet()) {
                if (!packetNode.hasChild(fieldEntry.key)) {
                    ParentNode fieldNode = new ParentNode(packetNode, fieldEntry.key)
                    packetNode.addChild(fieldEntry.key, fieldNode)
                }
                ParentNode fieldNode = (ParentNode) packetNode.children[fieldEntry.key]
                createDataSubTree(fieldNode, (Map) fieldEntry.value)
            }
        }
    }

    private void createDataSubTree(ParentNode fieldNode, Map data) {
        // determine for each iteration the number of cells to reserve
        int numOfPeriods = data.size()
        int numOfIterations = ((Map)data[0]).size()
        for (int iteration = 1; iteration <= numOfIterations; iteration++) {
            int n = 0
            for (int period = 0; period < numOfPeriods; period++) {
                n = Math.max(n, ((List)data[period][iteration]).size())
            }
            ParentNode iterationNode = getNode(fieldNode, iteration)
            for (int i = 0; i < n; i++) {
                DataNode dataNode = new DataNode(i, iterationNode)
                iterationNode.addChild(i, dataNode)
            }
            for (int period = 0; period < numOfPeriods; period++) {
                List singleValues = (List) data[period][iteration]
                for (int i = 0; i < singleValues.size(); i++) {
                    ((DataNode)iterationNode.getChildAt(i)).values.put(period, singleValues[i])
                }
            }
        }
    }

    private ParentNode getNode(String path) {
        String[] pathElements = path.split(PATHSEP)
        ParentNode node = fRoot
        pathElements[0..-1].each { name ->
            node = getNode(node, name)
        }
        return node
    }

    private ParentNode getNode(ParentNode parent, def id) {
        if (!parent.hasChild(id)) {
            ParentNode child = new ParentNode(parent, id)
            parent.addChild id, child
        }
        return (ParentNode) parent.children[id]
    }

    // Methods overwriting ITableTreeModel

    public int getColumnCount() {
        return 2; // TODO --> get the correct number of periods!
    }

    public Object getValueAt(Object node, int column) {
        return ((ITableTreeNode)node).getValueAt(column)
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
        return fRoot;
    }

    public Object getChild(Object parent, int i) {
        return ((ITableTreeNode)parent).getChildAt(i);
    }

    public int getChildCount(Object parent) {
        return ((ITableTreeNode)parent).getChildCount();
    }

    public boolean isLeaf(Object node) {
        return ((ITableTreeNode)node).leaf;
    }

    public int getIndexOfChild(Object parent, Object child) {
        return ((ITableTreeNode)parent).getIndex((ITableTreeNode) child);
    }

    public String getColumnName(int column) {
        return column==0 ? "Name" : column-1;
    }
}
