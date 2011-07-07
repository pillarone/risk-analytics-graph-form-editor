package org.pillarone.riskanalytics.graph.formeditor.ui.model

import com.ulcjava.base.application.tabletree.AbstractTableTreeModel
import com.ulcjava.base.application.tabletree.ITableTreeModel
import com.ulcjava.base.application.tabletree.ITableTreeNode
import com.ulcjava.base.application.tree.TreePath
import java.lang.reflect.Field
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolder
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolderFactory
import org.pillarone.riskanalytics.graph.core.graphimport.ComposedComponentGraphImport
import org.pillarone.riskanalytics.graph.core.graph.model.*
import com.ulcjava.base.application.tabletree.IMutableTableTreeNode

/**
 * 
 */
class DataTableTreeModel extends AbstractTableTreeModel implements ITableTreeModel, IGraphModelChangeListener {

    private static final String PATHSEP = ':'
    private DataTreeComponentNode fRoot
    private Parameterization fParametrization

    public DataTableTreeModel(ModelGraphModel model, int periodCount, String dataObjectName) {
        this(model, new Parameterization(name: dataObjectName, periodCount: periodCount))
    }

    public DataTableTreeModel(ModelGraphModel model, Parameterization parametrization) {
        super()
        fRoot = new DataTreeComponentNode(model, null, "")
        createTree()
        fParametrization = parametrization
        injectParametrizationToTree(fParametrization)
    }

    public interface IDataTreeNode extends IMutableTableTreeNode  {
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
            this.name = path.substring(path.lastIndexOf(PATHSEP)+1)
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
                path = parentNode.path+PATHSEP+name
                // TODO do I need to remove this node as child from the prior parent and this as new child to the new parent?
            }
        }
        int getIndex(ITableTreeNode child) { throw new IndexOutOfBoundsException("No children attached to this data node in the data tree.") }
        Object getValueAt(int column) { return column==0 ? name : parameters.get(column-1).getBusinessObject() }
        void setValueAt(Object value, int column) {
            if (column>0) {
                parameters.get(column-1).setValue(value)
            }
        }
        boolean isLeaf() { return true }
        boolean isCellEditable(int column) { return column>0 }
    }

    class DataTreeComponentNode implements IDataTreeNode {
        String path
        String name
        DataTreeComponentNode parentNode
        GraphElement graphElement
        List<IDataTreeNode> children = []
        Class type = null

        DataTreeComponentNode(GraphElement node, DataTreeComponentNode parent, String parentPath) {
            this.name = node.name
            this.parentNode = parent
            this.path = parent==null ? "" : (parentPath.size()==0 ? "" : parentPath + PATHSEP) + node.name
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
                path = parentNode.path+PATHSEP+name
                // TODO do I need to remove this node as child from the prior parent and this as new child to the new parent?
            }
        }
        int getIndex(ITableTreeNode child) { return children.indexOf(child) }
        Object getValueAt(int column) { return column==0 ? name : "" }
        void setValueAt(Object o, int i) {
            throw new RuntimeException("Value of a non-leaf node cannot be changed.")
        }
        boolean isCellEditable(int i) {
            return false  //To change body of implemented methods use File | Settings | File Templates.
        }
        boolean isLeaf() { return false }
    }

    // methods to setup, manipulate and search the tree

    public void createTree() {
        addChildren(fRoot)
    }

    private boolean addChildren(DataTreeComponentNode parent) {
        List<IDataTreeNode> children = getChildren(parent)
        for (IDataTreeNode child : children) { 
            if (child instanceof DataTreeComponentNode) {
                boolean hasChildren = addChildren(child)
                if (hasChildren) {
                    parent.addChild (child)
                }
            } else {
                parent.addChild (child)
            }
        }
        return children != null && children.size()>0
    }

    private static Map<String,Object> getParameterObjects(ComponentNode node) {
        Map<String, Object> result = [:]
        Class componentClass = node.getType().getTypeClass()
        Component component = (Component) componentClass.newInstance()
        Field[] fields = componentClass.declaredFields
        for (Field field in fields) {
            if (field.name.startsWith("parm")) {
                field.accessible = true
                Object value = field.get(component)
                if (!field.type.isPrimitive() && !value) {
                    value = field.type.newInstance()
                }
                result.put(field.name, value)
            }
        }
        return result
    }

    private List<IDataTreeNode> getChildren(DataTreeComponentNode parent) {
        String parentPath = parent.path
        List<IDataTreeNode> children = []
        if (parent.graphElement instanceof ComponentNode) {
            ComponentNode node = (ComponentNode) parent.graphElement
            Map<String,Object> parameterObjects = getParameterObjects(node)
            parameterObjects.each { parameterName, parameterValue ->
                DataTreeParameterNode paramNode = new DataTreeParameterNode(parentPath+PATHSEP+parameterName, parameterValue, parent)
                children << paramNode
            }
        }
        AbstractGraphModel model = null
        if (parent.graphElement instanceof AbstractGraphModel) {
            model = (AbstractGraphModel) parent.graphElement
        } else if (parent.graphElement instanceof ComposedComponentNode){
            ComposedComponentNode cc = (ComposedComponentNode)parent.graphElement
            if (cc.componentGraph == null) {
                ComposedComponentGraphImport importer = new ComposedComponentGraphImport()
                ComposedComponentGraphModel ccModel = (ComposedComponentGraphModel) importer.importGraph(cc.getType().getTypeClass(), "")
                cc.setComponentGraph(ccModel)
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
        if (node instanceof DataTreeComponentNode) {
            ((DataTreeComponentNode)node).children?.each { child ->
                leaves.addAll(getAllLeaves(child))
            }
        } else {
            leaves.add((DataTreeParameterNode)node)
        }
        return leaves
    }

    public IDataTreeNode getDataTreeNode(GraphElement node) {
        if (node instanceof ComponentNode) {
            def dataTreeNode = fRoot.children.find {child ->
                                    ( child instanceof DataTreeComponentNode
                                        && ((DataTreeComponentNode)child).graphElement==node) }
            return dataTreeNode
        }
    }

    // Methods overwriting ITableTreeModel

    public int getColumnCount() {
        return fParametrization.periodCount+1;
    }

    public Object getValueAt(Object parent, int column) {
        return ((IDataTreeNode)parent).getValueAt(column)
    }

    public void setValueAt(Object node, Object value, int column) {
        if (node instanceof DataTreeParameterNode) {
            ((DataTreeParameterNode)node).setValueAt(value, column)
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
        return column==0 ? String.class : Double.class
    }

    public boolean isCellEditable(Object node, int column) {
        return node instanceof DataTreeParameterNode && column > 0
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
        return column==0 ? "Name" : fParametrization.getPeriodLabel(column-1);
    }

    // linking parameterization (--> ParameterizationHolder's) and the leaves in the tree
    public void injectParametrizationToTree(Parameterization parametrization) {
        fParametrization = parametrization
        int periodCount = fParametrization.periodCount
        List<DataTreeParameterNode> leaves = getAllLeaves(fRoot)
        leaves.each { leaf ->
            linkLeafParametrization(leaf, fParametrization)
        }
    }

    protected void linkLeafParametrization(DataTreeParameterNode leaf, Parameterization parametrization) {
        Map<Integer,ParameterHolder> existingHoldersInNode = [:]
        leaf.parameters.each { holder -> existingHoldersInNode[holder.periodIndex] = holder }
        List<ParameterHolder> parameterHolders = parametrization.getParameters( leaf.path )
        for (int periodIndex = 0; periodIndex < parametrization.periodCount; periodIndex++) {
            def matchingPeriodHolders = parameterHolders.findAll { holder -> holder.periodIndex==periodIndex }
            if (matchingPeriodHolders.size()>0) {
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
            fRoot.addChild treeNode
            List<DataTreeParameterNode> leaves = getAllLeaves(treeNode)
            leaves.each { leaf -> linkLeafParametrization(leaf, fParametrization) }
            structureChanged()
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
            structureChanged()
        }
    }

    void nodePropertyChanged(ComponentNode node, String propertyName, Object oldValue, Object newValue) {
        if (propertyName == "name") {
            DataTreeComponentNode treeNode = (DataTreeComponentNode) getDataTreeNode(node)
            treeNode.name = newValue
            String oldPath = treeNode.path
            String newPath = oldPath.substring(0,oldPath.lastIndexOf(PATHSEP))+PATHSEP+newValue
            treeNode.path = newPath
            List<DataTreeParameterNode> leaves = getAllLeaves(treeNode)
            leaves.each { leaf ->
                String leafPath = leaf.path
                if (leafPath.startsWith(oldPath)) {
                    String newLeafPath = newPath+PATHSEP+leafPath.substring(oldPath.length()+1)
                    leaf.parameters.each { it ->
                        it.path = newLeafPath
                    }
                }
            }
            nodeChanged( new TreePath([fRoot,treeNode]), 0)
        } else if (propertyName == "type") {
            nodeRemoved(node)
            nodeAdded(node)
        }
    }

    void connectionAdded(Connection c) { }
    void connectionRemoved(Connection c) { }
    void nodesSelected(List<ComponentNode> nodes) { }
    void connectionsSelected(List<Connection> connections) { }
    void selectionCleared() { }
    void filtersApplied() { }

    // testing utilities
    private void printTree() {
        printNode(fRoot,"");
    }

    private void printNode(IDataTreeNode node, String tab) {
        System.out.println(node.path);
        if (node instanceof DataTreeParameterNode) {
            ((DataTreeParameterNode)node).parameters.get(0).getBusinessObject().toString()
        } else {
            ((DataTreeComponentNode)node).children.each { child ->
                printNode((IDataTreeNode) child, tab+"   ");
            }
        }
    }
}
