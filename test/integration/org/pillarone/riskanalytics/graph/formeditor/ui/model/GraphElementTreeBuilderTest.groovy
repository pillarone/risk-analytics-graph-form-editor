package org.pillarone.riskanalytics.graph.formeditor.ui.model

import org.pillarone.riskanalytics.graph.core.graph.model.ComponentNode
import org.pillarone.riskanalytics.graph.core.graph.model.ModelGraphModel
import org.pillarone.riskanalytics.graph.core.palette.model.ComponentDefinition
import org.pillarone.riskanalytics.graph.formeditor.examples.Aggregator
import org.pillarone.riskanalytics.graph.formeditor.examples.ExcessOfLoss
import org.pillarone.riskanalytics.graph.formeditor.examples.PoissonFrequencyGenerator
import org.pillarone.riskanalytics.graph.formeditor.examples.SingleNormalClaimsGenerator
import org.pillarone.riskanalytics.graph.formeditor.ui.model.treetable.GraphElementTreeBuilder
import org.pillarone.riskanalytics.graph.formeditor.ui.model.treetable.GraphElementNode
import org.pillarone.riskanalytics.graph.core.graph.model.ComposedComponentGraphModel
import org.pillarone.riskanalytics.graph.core.graph.model.InPort
import org.pillarone.riskanalytics.graph.formeditor.examples.ClaimPacket
import org.pillarone.riskanalytics.graph.core.graph.model.OutPort
import org.pillarone.riskanalytics.graph.formeditor.examples.FrequencyPacket
import org.pillarone.riskanalytics.graph.formeditor.ui.model.treetable.SimpleTableTreeNode
import org.pillarone.riskanalytics.core.example.component.TestComposedComponent

class GraphElementTreeBuilderTest extends GroovyTestCase {

    ModelGraphModel getModel() {
        ModelGraphModel model = new ModelGraphModel()
        ComponentNode freq = model.createComponentNode(new ComponentDefinition(typeClass: PoissonFrequencyGenerator.class), "freq")
        ComponentNode claims = model.createComponentNode(new ComponentDefinition(typeClass: SingleNormalClaimsGenerator.class), "claims")
        ComponentNode xl = model.createComponentNode(new ComponentDefinition(typeClass: ExcessOfLoss.class), "xl")
        ComponentNode aggregator = model.createComponentNode(new ComponentDefinition(typeClass: Aggregator.class), "aggregator")
        model.createConnection(freq.getPort("outFrequency"), claims.getPort("inFrequency"))
        model.createConnection(claims.getPort("outClaims"), xl.getPort("inClaims"))
        model.createConnection(xl.getPort("outRetainedClaims"), aggregator.getPort("inClaims"))

        ComponentNode cc = model.createComponentNode(new ComponentDefinition(typeClass: TestComposedComponent.class), "cc")

        return model
    }

    ComposedComponentGraphModel getComposedComponent() {
        ComposedComponentGraphModel cc = new ComposedComponentGraphModel()
        ComponentNode freq = cc.createComponentNode(new ComponentDefinition(typeClass: PoissonFrequencyGenerator.class), "freq")
        ComponentNode claims = cc.createComponentNode(new ComponentDefinition(typeClass: SingleNormalClaimsGenerator.class), "claims")
        ComponentNode xl = cc.createComponentNode(new ComponentDefinition(typeClass: ExcessOfLoss.class), "xl")
        ComponentNode aggregator = cc.createComponentNode(new ComponentDefinition(typeClass: Aggregator.class), "aggregator")
        cc.createConnection(freq.getPort("outFrequency"), claims.getPort("inFrequency"))
        cc.createConnection(claims.getPort("outClaims"), xl.getPort("inClaims"))
        cc.createConnection(xl.getPort("outRetainedClaims"), aggregator.getPort("inClaims"))
        InPort outerInPort = cc.createOuterInPort(ClaimPacket.class, "externalClaims")
        cc.createConnection(outerInPort, xl.getPort("inClaims"))
        OutPort frequency = cc.createOuterOutPort(FrequencyPacket.class, "frequency")
        cc.createConnection(freq.getPort("outFrequency"), frequency)
        return cc
    }


    void testConstructorModel() {
        ModelGraphModel model = getModel()
        GraphElementTreeBuilder treeBuilder = new GraphElementTreeBuilder(model)

        GraphElementNode root = treeBuilder.getRoot()
        assertNotNull root
        assert root.childCount == 5

        SimpleTableTreeNode freq = root.getChildByName("freq")
        assert freq.childCount == 1

        SimpleTableTreeNode claims = root.getChildByName("claims")
        assert claims.childCount == 2

        SimpleTableTreeNode xl = root.getChildByName("xl")
        assert xl.childCount == 3

        SimpleTableTreeNode aggr = root.getChildByName("aggregator")
        assert aggr.childCount == 2

        SimpleTableTreeNode cc = root.getChildByName("cc")
        assert cc.childCount == 6

        SimpleTableTreeNode c1 = cc.getChildByName("Component1")
        assert c1.childCount == 4
        SimpleTableTreeNode c2 = cc.getChildByName("Component2")
        assert c2.childCount == 4

    }

    void testConstructorCC() {
        ComposedComponentGraphModel model = getComposedComponent()
        GraphElementTreeBuilder treeBuilder = new GraphElementTreeBuilder(model)

        GraphElementNode root = treeBuilder.getRoot()
        assertNotNull root
        assert root.childCount == 6

        SimpleTableTreeNode freq = root.getChildByName("freq")
        assert freq.childCount == 1

        SimpleTableTreeNode claims = root.getChildByName("claims")
        assert claims.childCount == 2

        SimpleTableTreeNode xl = root.getChildByName("xl")
        assert xl.childCount == 3

        SimpleTableTreeNode aggr = root.getChildByName("aggregator")
        assert aggr.childCount == 2

        SimpleTableTreeNode outFreq = root.getChildByName("frequency")
        assert outFreq.childCount == 0
        SimpleTableTreeNode extClaims = root.getChildByName("external claims")
        assert extClaims.childCount == 0

    }
}
