package org.pillarone.riskanalytics.graph.formeditor.ui.model

import org.pillarone.riskanalytics.core.example.component.TestComposedComponent
import org.pillarone.riskanalytics.graph.core.palette.model.ComponentDefinition
import org.pillarone.riskanalytics.graph.formeditor.ui.model.treetable.GraphElementNode

import org.pillarone.riskanalytics.graph.formeditor.ui.model.treetable.SimpleTableTreeNode
import org.pillarone.riskanalytics.graph.core.graph.model.*
import org.pillarone.riskanalytics.graph.formeditor.examples.*
import org.pillarone.riskanalytics.graph.formeditor.ui.model.treetable.NodesTableTreeModel

class NodesTableTreeModelTest extends GroovyTestCase {

    ModelGraphModel getModel() {
        ModelGraphModel model = new ModelGraphModel()
        ComponentNode freq = model.createComponentNode(new ComponentDefinition(typeClass: SimpleFrequencyGenerator.class), "freq")
        ComponentNode claims = model.createComponentNode(new ComponentDefinition(typeClass: SingleClaimsGenerator.class), "claims")
        ComponentNode xl = model.createComponentNode(new ComponentDefinition(typeClass: ExcessOfLoss.class), "xl")
        ComponentNode aggregator = model.createComponentNode(new ComponentDefinition(typeClass: Aggregator.class), "aggregator")
        model.createConnection(freq.getPort("outFrequency"), claims.getPort("inFrequency"))
        model.createConnection(claims.getPort("outClaims"), xl.getPort("inClaims"))
        model.createConnection(xl.getPort("outRetainedClaims"), aggregator.getPort("inClaims"))
        return model
    }

    void testWithModel() {
        ModelGraphModel model = getModel()

        // constructor
        NodesTableTreeModel tableModel = new NodesTableTreeModel(null, model)

        // getRoot(), getChildCount()
        GraphElementNode root = tableModel.getRoot()
        assertNotNull root
        assert root.childCount == 4

        // isLeaf(), findNode(), getChild(), getValueAt()
        assertFalse root.isLeaf()
        GraphElementNode freqNode = tableModel.findNode(model.getAllComponentNodes().get(0))
        assertNotNull freqNode
        assertFalse freqNode.isLeaf()
        assert tableModel.getValueAt(freqNode, NodesTableTreeModel.NAMEID) == "freq"
        assert tableModel.getValueAt(freqNode, NodesTableTreeModel.TYPEID) == "SimpleFrequencyGenerator (org.pillarone.riskanalytics.graph.formeditor.examples)"
        assert tableModel.getValueAt(freqNode, NodesTableTreeModel.INFOID) == null
        GraphElementNode freqOutNode = tableModel.findNode(model.getAllComponentNodes().get(0).outPorts.get(0))
        assertNotNull freqOutNode
        assertTrue freqOutNode.isLeaf()
        assert tableModel.getValueAt(freqOutNode, NodesTableTreeModel.NAMEID) == "frequency"
        assert tableModel.getValueAt(freqOutNode, NodesTableTreeModel.TYPEID) == "FrequencyPacket (org.pillarone.riskanalytics.graph.formeditor.examples)"
        assert tableModel.getValueAt(freqOutNode, NodesTableTreeModel.INFOID) == null

        GraphElementNode xlNode = tableModel.getChild(root, 2)
        assertNotNull xlNode
        assertFalse xlNode.isLeaf()
        assert tableModel.getValueAt(xlNode, NodesTableTreeModel.NAMEID) == "xl"
        assert tableModel.getValueAt(xlNode, NodesTableTreeModel.TYPEID) == "ExcessOfLoss (org.pillarone.riskanalytics.graph.formeditor.examples)"
        assert tableModel.getValueAt(xlNode, NodesTableTreeModel.INFOID) == null

        assert tableModel.getIndexOfChild(root, xlNode) == 2

        ComponentNode cc = model.createComponentNode(new ComponentDefinition(typeClass: TestComposedComponent.class), "cc")
        tableModel.addChild(root, cc)
        assertTrue root.childCount == 5
        SimpleTableTreeNode ccNode = root.getChildByName("cc")

        SimpleTableTreeNode c1 = ccNode.getChildByName("Component1")
        assert c1.childCount == 4
        SimpleTableTreeNode c2 = ccNode.getChildByName("Component2")
        assert c2.childCount == 4

        tableModel.removeChild(root, freqNode)
        assertTrue root.childCount==4
        assertFalse root.children.contains(freqNode)
        assertTrue root.getIndex(freqNode)==-1
    }

    // TODO once the GraphModelChangeListener has been re-organized...
    /*public void testWithListener() {
        ModelGraphModel model = getModel()
        NodesTableTreeModel tableModel = new NodesTableTreeModel(null, model)
        model.addGraphModelChangeListener tableModel

        ComponentNode cc = model.createComponentNode(new ComponentDefinition(typeClass: TestComposedComponent.class), "cc")
        tableModel.addChild(root, cc)
        assertTrue root.childCount == 5
        SimpleTableTreeNode ccNode = root.getChildByName("cc")

        SimpleTableTreeNode c1 = ccNode.getChildByName("Component1")
        assert c1.childCount == 4
        SimpleTableTreeNode c2 = ccNode.getChildByName("Component2")
        assert c2.childCount == 4

        tableModel.removeChild(root, freqNode)
        assertTrue root.childCount==4
        assertFalse root.children.contains(freqNode)
        assertTrue root.getIndex(freqNode)==-1

    }*/

}
