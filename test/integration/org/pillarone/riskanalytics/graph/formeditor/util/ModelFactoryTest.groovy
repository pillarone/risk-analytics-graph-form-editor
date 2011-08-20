package org.pillarone.riskanalytics.graph.formeditor.util

import org.pillarone.riskanalytics.graph.core.graph.model.ModelGraphModel
import org.pillarone.riskanalytics.graph.core.graph.model.ComponentNode
import org.pillarone.riskanalytics.graph.core.palette.model.ComponentDefinition
import org.pillarone.riskanalytics.core.model.StochasticModel
import org.pillarone.riskanalytics.graph.formeditor.examples.SingleClaimsGenerator
import org.pillarone.riskanalytics.graph.formeditor.examples.SimpleFrequencyGenerator
import org.pillarone.riskanalytics.graph.formeditor.examples.Aggregator
import org.pillarone.riskanalytics.graph.formeditor.examples.ExcessOfLoss
import org.pillarone.riskanalytics.core.simulation.engine.SimulationRunner

import org.pillarone.riskanalytics.core.simulation.item.Parameterization

import org.pillarone.riskanalytics.core.simulation.item.parameter.DoubleParameterHolder

/**
 *
 */
class ModelFactoryTest extends GroovyTestCase {

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

    Parameterization getParameters() {
        Parameterization p = new Parameterization(periodCount : 1, name: "dummy")
        p.addParameter(new DoubleParameterHolder("freq:parmMean", 0, 10.0))
        p.addParameter(new DoubleParameterHolder("claims:parmMean", 0, 100.0))
        p.addParameter(new DoubleParameterHolder("claims:parmStdev", 0, 20.0))
        p.addParameter(new DoubleParameterHolder("xl:parmRetention", 0, 100.0))
        p.addParameter(new DoubleParameterHolder("xl:parmLimit", 0, 40.0))
        return p
    }

    void testInitAndWireModel() {
        ModelGraphModel graphModel = getModel()
        StochasticModel model = new ModelFactory().getModelInstance(graphModel)
        model.init()
        assertTrue model.getAllComponents().size()==4
        assertTrue model.getStartComponents().size()==1

        model.wire()
        assertTrue model["freq"].allOutputTransmitter.size()==1
        assertTrue model["claims"].allOutputTransmitter.size()==1
        assertTrue model["xl"].allOutputTransmitter.size()==1
    }

    void testRunModel() {
        ModelGraphModel graphModel = getModel()
        Parameterization data = getParameters()
        ProbeSimulationService service = new ProbeSimulationService()
        SimulationRunner runner = service.getSimulationRunner(graphModel, data)
        runner.start()
        Map output = service.output
        assertNotNull(output)
        assertTrue(output.size()>0)
    }

}
