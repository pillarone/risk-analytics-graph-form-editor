package org.pillarone.riskanalytics.graph.formeditor.ui.model

import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolder
import org.pillarone.riskanalytics.graph.core.graph.model.ComponentNode
import org.pillarone.riskanalytics.graph.core.graph.model.ModelGraphModel
import org.pillarone.riskanalytics.graph.core.palette.model.ComponentDefinition
import org.pillarone.riskanalytics.graph.formeditor.examples.Aggregator
import org.pillarone.riskanalytics.graph.formeditor.examples.ExcessOfLoss
import org.pillarone.riskanalytics.graph.formeditor.examples.SimpleFrequencyGenerator
import org.pillarone.riskanalytics.graph.formeditor.examples.SingleClaimsGenerator
import org.pillarone.riskanalytics.graph.formeditor.ui.model.DataTableTreeModel.DataTreeComponentNode
import org.pillarone.riskanalytics.graph.formeditor.ui.model.DataTableTreeModel.DataTreeParameterNode

class DataTableModelTests extends GroovyTestCase {

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


    void testCreateTree() {
        ModelGraphModel model = getModel()
        assertTrue(model.allComponentNodes.size()==4)

        DataTableTreeModel tableModel = new DataTableTreeModel(model, 2, "data")
        
        assertTrue(tableModel.getRoot() instanceof DataTreeComponentNode)
        assertTrue(tableModel.getRoot().children.size()==1)

        DataTreeComponentNode xlNode = (DataTreeComponentNode) tableModel.getRoot().children[0]
        assertTrue(xlNode.childCount==2)
        assertTrue(tableModel.getAllLeaves(tableModel.root).size()==2)

        DataTreeParameterNode parm1 = (DataTreeParameterNode) xlNode.children[0]
        assertTrue(parm1.name=="parmRetention")
        assertTrue(parm1.path==':xl:parmRetention')
        assertTrue(parm1.parameters.size()==2)
        assertTrue(parm1.parameters[0] instanceof ParameterHolder)
        assertTrue(((ParameterHolder)parm1.parameters[0]).periodIndex==0)
        assertTrue(((ParameterHolder)parm1.parameters[0]).path==parm1.path)
        assertTrue(parm1.parameters[1] instanceof ParameterHolder)
        assertTrue(((ParameterHolder)parm1.parameters[1]).periodIndex==1)
        assertTrue(((ParameterHolder)parm1.parameters[1]).path==parm1.path)
        assertTrue(tableModel.getParametrization().parameters.contains(parm1.parameters[0]))
        assertTrue(tableModel.getParametrization().parameters.contains(parm1.parameters[1]))

        DataTreeParameterNode parm2 = (DataTreeParameterNode) xlNode.children[1]
        assertTrue(parm2.name=="parmLimit")
        assertTrue(parm2.path==':xl:parmLimit')
        assertTrue(parm2.parameters.size()==2)
        assertTrue(parm2.parameters[0] instanceof ParameterHolder)
        assertTrue(((ParameterHolder)parm2.parameters[0]).periodIndex==0)
        assertTrue(((ParameterHolder)parm2.parameters[0]).path==parm2.path)
        assertTrue(parm1.parameters[1] instanceof ParameterHolder)
        assertTrue(((ParameterHolder)parm2.parameters[1]).periodIndex==1)
        assertTrue(((ParameterHolder)parm2.parameters[1]).path==parm2.path)
        assertTrue(tableModel.getParametrization().parameters.contains(parm2.parameters[0]))
        assertTrue(tableModel.getParametrization().parameters.contains(parm2.parameters[1]))
    }

    void testModelListenerTree() {
        ModelGraphModel model = getModel()
        DataTableTreeModel tableModel = new DataTableTreeModel(model, 2, "data")
        model.addGraphModelChangeListener tableModel

        assertTrue(tableModel.getRoot().children.size()==1)
        DataTreeComponentNode xlNode = (DataTreeComponentNode) tableModel.getRoot().children[0]
        assertTrue(xlNode.childCount==2)
        assertTrue(tableModel.getAllLeaves(tableModel.root).size()==2)

        ComponentNode freq2 = model.createComponentNode(new ComponentDefinition(typeClass: SimpleFrequencyGenerator.class), "freq2")
        ComponentNode claims2 = model.createComponentNode(new ComponentDefinition(typeClass: SingleClaimsGenerator.class), "claims2")
        ComponentNode xl2 = model.createComponentNode(new ComponentDefinition(typeClass: ExcessOfLoss.class), "xl2")

        assertTrue(tableModel.getRoot().children.size()==2)
        DataTreeComponentNode xlNode2 = (DataTreeComponentNode) tableModel.getRoot().children[1]
        assertTrue(xlNode2.childCount==2)
        assertTrue(tableModel.getAllLeaves(tableModel.root).size()==4)
    }


}
