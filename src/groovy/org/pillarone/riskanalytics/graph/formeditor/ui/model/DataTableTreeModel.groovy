package org.pillarone.riskanalytics.graph.formeditor.ui.model

import com.ulcjava.base.application.tabletree.AbstractTableTreeModel
import com.ulcjava.base.application.tabletree.IMutableTableTreeNode
import com.ulcjava.base.application.tabletree.ITableTreeNode
import com.ulcjava.base.application.tree.TreePath
import java.lang.reflect.Field
import org.pillarone.riskanalytics.core.packets.Packet
import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolder
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolderFactory
import org.pillarone.riskanalytics.graph.core.graphimport.ComposedComponentGraphImport
import org.pillarone.riskanalytics.graph.formeditor.util.ParameterUtilities
import org.pillarone.riskanalytics.graph.core.graph.model.*
import models.core.CoreModel
import org.pillarone.riskanalytics.core.model.Model

/**
 *
 */
class DataTableTreeModel extends AbstractTableTreeModel implements IGraphModelChangeListener {

    static final String PATHSEP = ':'
    private DataTreeComponentNode fRoot
    private Parameterization fParametrization

    public DataTableTreeModel(AbstractGraphModel model, int periodCount, String dataObjectName) {
        this(model, new Parameterization(name: dataObjectName, periodCount: periodCount))
    }

    public DataTableTreeModel(AbstractGraphModel model, Parameterization parametrization) {
        fRoot = new DataTreeComponentNode(model, null, "")
        createTree()
        fParametrization = parametrization
        injectParametrizationToTree(fParametrization)
    }

    // methods to setup, manipulate and search the tree
    public void createTree() {
        addChildren(fRoot)

        // add nodes to specify the packets if graph model is associated with a composed component
        if (fRoot.graphElement instanceof ComposedComponentGraphModel) {
            for (InPort p: ((ComposedComponentGraphModel) fRoot.graphElement).outerInPorts) {
                DataTreePacketNode packetNode = new DataTreePacketNode(p, fRoot)
                packetNode.parentNode = fRoot
                fRoot.addChild packetNode
            }
        }
    }

    private boolean addChildren(DataTreeComponentNode parent) {
        List<IDataTreeNode> children = getChildren(parent)
        for (IDataTreeNode child: children) {
            if (child instanceof DataTreeComponentNode) {
                boolean hasChildren = addChildren(child)
                if (hasChildren) {
                    parent.addChild(child)
                }
            } else {
                parent.addChild(child)
            }
        }
        return children != null && children.size() > 0
    }

    private List<IDataTreeNode> getChildren(DataTreeComponentNode parent) {
        String parentPath = parent.path
        List<IDataTreeNode> children = []
        if (parent.graphElement instanceof ComponentNode) {
            ComponentNode node = (ComponentNode) parent.graphElement
            Map<String, Object> parameterObjects = ParameterUtilities.getParameterObjects(node)
            parameterObjects.each { parameterName, parameterValue ->
                DataTreeParameterNode paramNode = new DataTreeParameterNode(parentPath + PATHSEP + parameterName, parameterValue, parent)
                children << paramNode
            }
        }
        AbstractGraphModel model = null
        if (parent.graphElement instanceof AbstractGraphModel) {
            model = (AbstractGraphModel) parent.graphElement
        } else if (parent.graphElement instanceof ComposedComponentNode) {
            ComposedComponentNode cc = (ComposedComponentNode) parent.graphElement
            if (cc.componentGraph == null) {
                cc.setComponentGraph(cc.getComponentGraph())
            }
            model = cc.componentGraph
        }
        if (model) {
            model.allComponentNodes.each { node ->
                children << new DataTreeComponentNode(node, parent, parentPath)
            }
        }
        return children
    }


    public List<DataTreeParameterNode> getAllLeaves(ITableTreeNode node) {
        List<DataTreeParameterNode> leaves = new ArrayList<DataTreeParameterNode>()
        if (node instanceof DataTreePacketNode) {
            return leaves
        } else if (node instanceof DataTreeComponentNode) {
            ((DataTreeComponentNode) node).children?.each { child ->
                leaves.addAll(getAllLeaves(child))
            }
        } else {
            leaves.add((DataTreeParameterNode) node)
        }
        return leaves
    }

    public IDataTreeNode getDataTreeNode(GraphElement node) {
        if (node instanceof ComponentNode) {
            def dataTreeNode = fRoot.children.find {child ->
                (child instanceof DataTreeComponentNode
                        && ((DataTreeComponentNode) child).graphElement == node)
            }
            return dataTreeNode
        }
    }

    // Methods overwriting ITableTreeModel

    public int getColumnCount() {
        return fParametrization.periodCount + 1;
    }

    public Object getValueAt(Object parent, int column) {
        return ((IDataTreeNode) parent).getValueAt(column)
    }

    public void setValueAt(Object value, Object node, int column) {
        if (node instanceof DataTreeParameterNode) {
            ((DataTreeParameterNode) node).setValueAt(value, column)
            /*Object[] path = [node]
            ITableTreeNode parent = ((ITableTreeNode)node).getParent()
            while (parent != null) {
                path << parent
                parent = parent.getParent()
            }
            path = path.reverse()
            nodeChanged(new TreePath(path), column)*/
        }
    }

    public Class getColumnClass(int column) {
        return column == 0 ? String.class : Double.class
    }

    public boolean isCellEditable(Object node, int column) {
        return node instanceof DataTreeParameterNode && column > 0
    }

    public Object getRoot() {
        return fRoot;
    }

    public Object getChild(Object parent, int i) {
        return ((ITableTreeNode) parent).getChildAt(i);
    }

    public int getChildCount(Object parent) {
        return ((ITableTreeNode) parent).getChildCount();
    }

    public boolean isLeaf(Object node) {
        return ((ITableTreeNode) node).leaf;
    }

    public int getIndexOfChild(Object parent, Object child) {
        return ((ITableTreeNode) parent).getIndex((ITableTreeNode) child);
    }

    public String getColumnName(int column) {
        return column == 0 ? "Name" : fParametrization.getPeriodLabel(column - 1);
    }

    // linking parametrization (--> ParameterizationHolder's) and the leaves in the tree
    public void injectParametrizationToTree(Parameterization parametrization) {
        fParametrization = parametrization
        List<DataTreeParameterNode> leaves = getAllLeaves(fRoot)
        leaves.each { leaf ->
            linkLeafParametrization(leaf, fParametrization)
        }

        int periodCount = fParametrization.periodCount
        List<ITableTreeNode> packetNodes = fRoot.children.findAll {node -> node instanceof DataTreePacketNode}.toList()
        for (ITableTreeNode packetNode0: packetNodes) {
            DataTreePacketNode packetNode = (DataTreePacketNode) packetNode0
            for (int i = 0; i < periodCount; i++) {
                final Packet packet = (Packet) packetNode.type.newInstance()
                PacketParameter param = new PacketParameter()
                param.path = packetNode.path
                param.periodIndex = i
                param.packetValue = packet
                ParameterHolder parameterHolder = new PacketHolder(param)
                packetNode.parameters.add parameterHolder
                parametrization.addParameter(parameterHolder)
            }
        }
    }

    protected void linkLeafParametrization(DataTreeParameterNode leaf, Parameterization parametrization) {
        Map<Integer, ParameterHolder> existingHoldersInNode = [:]

        // remember first the parameter holders that are linked to the tree node already
        // these will be replaced by the holders from teh parametrization
        leaf.parameters.each { holder -> existingHoldersInNode[holder.periodIndex] = holder }

        // then get all the parameter holders included in the parametrization given the model path
        List<ParameterHolder> parameterHolders = parametrization.getParameters(leaf.path)
        for (int periodIndex = 0; periodIndex < parametrization.periodCount; periodIndex++) {
            def matchingPeriodHolders = parameterHolders.findAll { holder -> holder.periodIndex == periodIndex }
            if (matchingPeriodHolders.size() > 0) {
                if (existingHoldersInNode.containsKey(periodIndex)) {
                    leaf.parameters.remove(existingHoldersInNode.get(periodIndex))
                }
                leaf.parameters.add(matchingPeriodHolders[0])
            } else {
                ParameterHolder holder = ParameterHolderFactory.getHolder(leaf.path, periodIndex, leaf.paramObject)
                leaf.parameters.add(holder)
                parametrization.addParameter(holder)
            }
        }
    }

    public Parameterization getParametrization() {
        return fParametrization
    }

    // graph change event handling
    void nodeAdded(ComponentNode node) {
        DataTreeComponentNode treeNode = new DataTreeComponentNode(node, fRoot, "")
        boolean hasDataNodes = addChildren(treeNode)
        if (hasDataNodes) {
            int index = fRoot.childCount
            fRoot.addChild treeNode
            List<DataTreeParameterNode> leaves = getAllLeaves(treeNode)
            leaves.each { leaf -> linkLeafParametrization(leaf, fParametrization) }
            nodesWereInserted(new TreePath(fRoot), [index] as int[])
        }
    }

    void nodeRemoved(ComponentNode node) {
        IDataTreeNode treeNode = getDataTreeNode(node)
        if (treeNode) {
            List<DataTreeParameterNode> leaves = getAllLeaves(treeNode)
            leaves.each { leaf ->
                leaf.parameters.each {
                    fParametrization.removeParameter(it)
                }
            }
            fRoot.children.remove(treeNode)
             nodeStructureChanged(new TreePath([fRoot] as Object[]))
        }
    }

    void nodePropertyChanged(ComponentNode node, String propertyName, Object oldValue, Object newValue) {
        if (propertyName == "name") {
            DataTreeComponentNode treeNode = (DataTreeComponentNode) getDataTreeNode(node)
            String oldName = treeNode.name
            treeNode.name = newValue
            String oldPath = treeNode.path.substring(0, treeNode.path.lastIndexOf(oldName))
            String newPath = oldPath + newValue
            treeNode.path = newPath
            List<DataTreeParameterNode> leaves = getAllLeaves(treeNode)
            leaves.each { leaf ->
                String leafPath = leaf.path
                if (leafPath.startsWith(oldPath)) {
                    String newLeafPath = newPath + PATHSEP + leafPath.substring(oldPath.length() + 1)
                    leaf.parameters.each { it ->
                        it.path = newLeafPath
                    }
                }
            }
            nodeChanged(new TreePath([fRoot, treeNode] as Object[]), 0)
        } else if (propertyName == "type") {
            nodeRemoved(node)
            nodeAdded(node)
        }
    }

    void connectionAdded(Connection c) { }

    void connectionRemoved(Connection c) { }

    void outerPortAdded(Port p) {
        if (fRoot.graphElement instanceof ComposedComponentGraphModel) {
            DataTreePacketNode packetNode = new DataTreePacketNode(p, fRoot)
            packetNode.parentNode = fRoot
            fRoot.addChild packetNode
            nodeStructureChanged(new TreePath([fRoot] as Object[]))
        }
    }

    void outerPortRemoved(Port p) {
        if (fRoot.graphElement instanceof ComposedComponentGraphModel) {
            String nodeName = p.name
            IDataTreeNode node = findNode(nodeName)
            if (node instanceof DataTreePacketNode) {
                int index = fRoot.children.indexOf(node)
                fRoot.remove index
                node.parameters.each { holder -> fParametrization.removeParameter holder }
                nodesWereRemoved(new TreePath([fRoot] as Object[]), [index] as int[], [node] as IDataTreeNode[])
            }
        }
    }

    void nodesSelected(List<ComponentNode> nodes) { }

    void connectionsSelected(List<Connection> connections) { }

    void selectionCleared() { }

    void filtersApplied() { }

    // testing utilities
    private void printTree() {
        printNode(fRoot, "");
    }

    private void printNode(IDataTreeNode node, String tab) {
        System.out.println(node.path);
        if (node instanceof DataTreeParameterNode) {
            ((DataTreeParameterNode) node).parameters.get(0).getBusinessObject().toString()
        } else {
            ((DataTreeComponentNode) node).children.each { child ->
                printNode((IDataTreeNode) child, tab + "   ");
            }
        }
    }

    public void save() {
        //the model doesn't exist, only on runtime
        // set org.pillarone.riskanalytics.core.model.Model as default
        fParametrization.setModelClass(Model.class)
        fParametrization.save()
    }

    class PacketHolder extends ParameterHolder {

        Packet value

        PacketHolder(Parameter p) {
            super(p)
        }

        @Override
        void setParameter(Parameter parameter) {
            this.value = parameter.packetValue
        }

        @Override
        Object getBusinessObject() {
            return value
        }

        @Override protected void updateValue(Object newValue) {
            value = (Packet) newValue
        }

        @Override
        void applyToDomainObject(Parameter parameter) {
            parameter.packetValue = value
        }

        @Override
        Parameter createEmptyParameter() {
            return null  //To change body of implemented methods use File | Settings | File Templates.
        }

    }

    class PacketParameter extends Parameter {
        Packet packetValue

        Class persistedClass() {
            PacketParameter
        }
    }

    public IDataTreeNode findNode(String name) {
        for (IDataTreeNode node: fRoot.getChildren()) {
            if ((node instanceof DataTreeComponentNode || node instanceof DataTreePacketNode) && node.name.equals(name))
                return node
        }
        return null
    }
}

public interface IDataTreeNode extends IMutableTableTreeNode {
    String getPath()

    String getName()

    Class getType()
}

class DataTreeParameterNode implements IDataTreeNode {
    String path
    String name
    DataTreeComponentNode parentNode
    Object paramObject
    Class type
    List<ParameterHolder> parameters

    DataTreeParameterNode(String path, Object paramObject, DataTreeComponentNode parent) {
        this.name = path.substring(path.lastIndexOf(DataTableTreeModel.PATHSEP) + 1)
        this.path = path
        this.parentNode = parent
        this.paramObject = paramObject
        this.type = paramObject.class
        parameters = []
    }

    ITableTreeNode getChildAt(int i) { return null }

    int getChildCount() { return 0 }

    void insert(IMutableTableTreeNode iMutableTableTreeNode, int i) {
        throw new RuntimeException("No child can be inserted in a data leaf node.")
    }

    void remove(int i) {
        throw new RuntimeException("There are no children that could be removed in a data leaf node.")
    }

    ITableTreeNode getParent() { return parentNode }

    void setParent(IMutableTableTreeNode iMutableTableTreeNode) {
        if (parentNode instanceof DataTreeComponentNode) {
            parentNode = (DataTreeComponentNode) iMutableTableTreeNode
            path = parentNode.path + DataTableTreeModel.PATHSEP + name
            // TODO do I need to remove this node as child from the prior parent and this as new child to the new parent?
        }
    }

    int getIndex(ITableTreeNode child) { throw new IndexOutOfBoundsException("No children attached to this data node in the data tree.") }

    Object getValueAt(int column) { return column == 0 ? name : parameters.get(column - 1).getBusinessObject() }

    void setValueAt(Object value, int column) {
        if (column > 0) {
            ParameterHolder holder = parameters.get(column - 1)
            holder.setValue(value)
        }
    }

    boolean isLeaf() { return true }

    boolean isCellEditable(int column) { return column > 0 }
}

class DataTreeComponentNode implements IDataTreeNode {
    String path
    String name
    DataTreeComponentNode parentNode
    GraphElement graphElement
    List<IDataTreeNode> children = []
    Class type = null

    DataTreeComponentNode() {}

    DataTreeComponentNode(GraphElement node, DataTreeComponentNode parent, String parentPath) {
        this.name = node.name
        this.parentNode = parent
        this.path = parent == null ? "" : (parentPath.size() == 0 ? "" : parentPath + DataTableTreeModel.PATHSEP) + node.name
        graphElement = node
    }

    void addChild(IDataTreeNode child) { insert(child, children.size()) }

    void insert(IMutableTableTreeNode child, int i) {
        if (child instanceof IDataTreeNode) {
            if (i < getChildCount()) {
                children.add(i, (IDataTreeNode) child)
            } else {
                children << (IDataTreeNode) child
            }
        }
    }

    ITableTreeNode getChildAt(int i) { return children[i] }

    int getChildCount() { return children.size() }

    void remove(int i) {
        if (i < getChildCount()) {
            children.remove(i)
        }
    }

    ITableTreeNode getParent() { return parentNode }

    void setParent(IMutableTableTreeNode iMutableTableTreeNode) {
        if (parentNode instanceof DataTreeComponentNode) {
            parentNode = (DataTreeComponentNode) iMutableTableTreeNode
            path = parentNode.path + DataTableTreeModel.PATHSEP + name
            // TODO do I need to remove this node as child from the prior parent and this as new child to the new parent?
        }
    }

    int getIndex(ITableTreeNode child) { return children.indexOf(child) }

    Object getValueAt(int column) { return column == 0 ? name : "" }

    void setValueAt(Object o, int i) {
        throw new RuntimeException("Value of a non-leaf node cannot be changed.")
    }

    boolean isCellEditable(int i) {
        return false
    }

    boolean isLeaf() { return false }
}

class DataTreePacketNode extends DataTreeComponentNode {

    List<ParameterHolder> parameters

    DataTreePacketNode(Port port, DataTreeComponentNode root) {
        this.name = port.name
        this.parentNode = root
        this.path = "provider_" + port.name + DataTableTreeModel.PATHSEP + "parmPacket"
        graphElement = port
        type = port.getPacketType()

        parameters = []

        Packet packet = (Packet) type.newInstance()
        Field[] fields = type.getDeclaredFields().findAll { field ->
            !field.name.startsWith("\$") && !field.name.startsWith("_") && !field.name.startsWith("metaClass")
        }
        for (Field field: fields) {
            Object value
            if (field.type.isPrimitive()) {
                switch (field.type) {
                    case Boolean.TYPE: value = Boolean.FALSE
                        break
                    case Integer.TYPE:
                    case Long.TYPE: value = 0
                        break
                    case Float.TYPE:
                    case Double.TYPE: value = 0.0
                        break
                }
            } else {
                value = field.type.newInstance()
            }
            DataTreePacketFieldNode fieldNode = new DataTreePacketFieldNode(field.name, this.path, value, this)
            this.addChild fieldNode
        }
    }
}

class DataTreePacketFieldNode extends DataTreeParameterNode {

    DataTreePacketFieldNode(String fieldName, String packetPath, Object paramObject, DataTreePacketNode parent) {
        super(packetPath + DataTableTreeModel.PATHSEP + fieldName, paramObject, parent)
        this.name = fieldName
        this.paramObject = paramObject
        this.type = paramObject.class
    }

    Object getValueAt(int column) {
        return column == 0 ? name : ((DataTreePacketNode) parent).parameters.get(column - 1).getBusinessObject()."$name"
    }

    void setValueAt(Object value, int column) {
        if (column > 0) {
            ParameterHolder holder = ((DataTreePacketNode) parent).parameters.get(column - 1)
            holder.getBusinessObject()."$name" = value
        }
    }
}


