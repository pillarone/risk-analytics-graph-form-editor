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
import org.pillarone.riskanalytics.graph.formeditor.util.ParameterUtilities
import org.pillarone.riskanalytics.core.simulation.item.Parameterization

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
        assertTrue(tableModel.getRoot().children.size()==3)
        DataTreeComponentNode xlNode = (DataTreeComponentNode) tableModel.getRoot().children[2]
        assertTrue(xlNode.childCount==2)
        assertTrue(tableModel.getAllLeaves(tableModel.root).size()==5)

        DataTreeParameterNode parm1 = (DataTreeParameterNode) xlNode.children[0]
        assertTrue(parm1.name=="parmRetention")
        assertTrue(parm1.path=='xl:parmRetention')
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
        assertTrue(parm2.path=='xl:parmLimit')
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

        assertTrue(tableModel.getRoot().children.size()==3)
        DataTreeComponentNode xlNode = (DataTreeComponentNode) tableModel.getRoot().children[2]
        assertTrue(xlNode.childCount==2)
        assertTrue(tableModel.getAllLeaves(tableModel.root).size()==5)

        ComponentNode freq2 = model.createComponentNode(new ComponentDefinition(typeClass: SimpleFrequencyGenerator.class), "freq2")
        ComponentNode claims2 = model.createComponentNode(new ComponentDefinition(typeClass: SingleClaimsGenerator.class), "claims2")
        ComponentNode xl2 = model.createComponentNode(new ComponentDefinition(typeClass: ExcessOfLoss.class), "xl2")

        assertTrue(tableModel.getRoot().children.size()==6)
        DataTreeComponentNode xlNode2 = (DataTreeComponentNode) tableModel.getRoot().children[5]
        assertTrue(xlNode2.childCount==2)
        assertTrue(tableModel.getAllLeaves(tableModel.root).size()==10)
    }

    void testSetValues() {
        ModelGraphModel model = getModel()
        DataTableTreeModel tableModel = new DataTableTreeModel(model, 2, "data")

        DataTreeComponentNode freqNode = (DataTreeComponentNode) tableModel.getRoot().children[0]

        List<ParameterHolder> holders = tableModel.getParametrization().getParameters("freq:parmMean")
        ParameterHolder holderPeriod00 = holders.findAll {
            holder -> holder.periodIndex==0
        }[0]
        assertTrue(holderPeriod00.businessObject==1.0)
        ParameterHolder holderPeriod01 = holders.findAll {
            holder -> holder.periodIndex==1
        }[0]
        assertTrue(holderPeriod01.businessObject==1.0)

        tableModel.setValueAt(100, freqNode.children[0], 1)
        tableModel.setValueAt(50, freqNode.children[0], 2)

        holders = tableModel.getParametrization().getParameters("freq:parmMean")
        ParameterHolder holderPeriod10 = holders.findAll {
            holder -> holder.periodIndex==0
        }[0]
        assertTrue holderPeriod00==holderPeriod10
        assertTrue(holderPeriod10.businessObject==100.0)

        ParameterHolder holderPeriod11 = holders.findAll {
            holder -> holder.periodIndex==1
        }[0]
        assertTrue holderPeriod01==holderPeriod11
        assertTrue(holderPeriod11.businessObject==50.0)


        String dataFileContent =
"""
package models.core

periodCount = 1

components {
    freq {
        parmMean[0] = 10.0
    }
    claims {
        parmMean[0] = 100.0
        parmStdev[0] = 20.0
    }
    xl {
        parmLimit[0] = 50.0
        parmRetention[0] = 100.0
    }
}
"""
        Parameterization params = ParameterUtilities.loadParametrization(dataFileContent);

        tableModel = new DataTableTreeModel(model, params)

        freqNode = (DataTreeComponentNode) tableModel.getRoot().children[0]
        holders = tableModel.getParametrization().getParameters("freq:parmMean")
        holderPeriod00 = holders.findAll {
            holder -> holder.periodIndex==0
        }[0]
        assertTrue(holderPeriod00.businessObject==10.0)

        tableModel.setValueAt(50, freqNode.children[0], 1)

        holders = tableModel.getParametrization().getParameters("freq:parmMean")
        holderPeriod10 = holders.findAll {
            holder -> holder.periodIndex==0
        }[0]
        assertTrue holderPeriod00==holderPeriod10
        assertTrue(holderPeriod10.businessObject==50.0)
    }


}
