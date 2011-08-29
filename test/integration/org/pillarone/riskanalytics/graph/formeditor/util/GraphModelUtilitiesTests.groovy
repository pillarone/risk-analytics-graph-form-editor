package org.pillarone.riskanalytics.graph.formeditor.util

import org.pillarone.riskanalytics.graph.core.graphimport.GraphImportService
import org.pillarone.riskanalytics.graph.core.loader.ClassRepository
import org.pillarone.riskanalytics.graph.core.loader.ClassType
import org.pillarone.riskanalytics.graph.core.loader.DatabaseClassLoader
import org.pillarone.riskanalytics.graph.core.palette.service.PaletteService
import org.pillarone.riskanalytics.graph.formeditor.examples.ClaimPacket
import org.pillarone.riskanalytics.graph.formeditor.examples.FrequencyPacket
import org.pillarone.riskanalytics.graph.formeditor.examples.SingleNormalClaimsGenerator
import org.pillarone.riskanalytics.graph.core.graph.model.*

class GraphModelUtilitiesTests extends GroovyTestCase {

    void setUp() {
        PaletteService.instance.reset()
        Thread.currentThread().contextClassLoader = new DatabaseClassLoader(Thread.currentThread().contextClassLoader)
    }

    void testExportToApplication() {
        ComposedComponentGraphModel model = new ComposedComponentGraphModel("MyComposedComponent", "my.packag3")
        ComponentNode node = model.createComponentNode(PaletteService.instance.getComponentDefinition(SingleNormalClaimsGenerator), "claimsGenerator")
        InPort inPort = model.createOuterInPort(FrequencyPacket, "inFrequency")
        OutPort outPort = model.createOuterOutPort(ClaimPacket, "outClaims")

        model.createConnection(inPort, node.getPort("inFrequency"))
        model.createConnection(outPort, node.getPort("outClaims"))

        GraphModelUtilities.exportToApplication(model)
        assertNotNull(PaletteService.instance.getComponentDefinition("my.packag3.MyComposedComponent"))
        assertEquals(3, ClassRepository.count()) //Component + 2 closures
        ClassRepository component = ClassRepository.findByName("my.packag3.MyComposedComponent")
        assertEquals(ClassType.COMPONENT, component.classType)
        assertEquals(2, ClassRepository.countByClassType(ClassType.DEPENDENCY))
    }

    void tearDown() {
        PaletteService.instance.reset()
    }


    void testGetGraphElementPath() {
        String ccFile = """
package models;

import org.pillarone.riskanalytics.core.model.StochasticModel;
import org.pillarone.riskanalytics.graph.formeditor.examples.AggregateNormalClaimGenerator;
import org.pillarone.riskanalytics.graph.formeditor.examples.Aggregator;
import org.pillarone.riskanalytics.graph.formeditor.examples.ExcessOfLoss;
import org.pillarone.riskanalytics.graph.formeditor.examples.PoissonLognormalCompoundClaims;

public class ModelA
    extends StochasticModel
{

    /**
     *
     */
    PoissonLognormalCompoundClaims compoundClaims;
    /**
     *
     */
    AggregateNormalClaimGenerator aggregateClaims;
    /**
     *
     */
    Aggregator aggregateGross;
    /**
     *
     */
    Aggregator aggregateCeded;
    /**
     *
     */
    ExcessOfLoss xl1;
    /**
     *
     */
    ExcessOfLoss xl2;

    public void initComponents() {
        aggregateCeded = new Aggregator();
        aggregateClaims = new AggregateNormalClaimGenerator();
        xl1 = new ExcessOfLoss();
        aggregateGross = new Aggregator();
        compoundClaims = new PoissonLognormalCompoundClaims();
        xl2 = new ExcessOfLoss();
        addStartComponent compoundClaims
        addStartComponent aggregateClaims
    }

    public void wireComponents() {
        xl1 .inClaims = compoundClaims.outClaims;
        xl2 .inClaims = compoundClaims.outClaims;
        aggregateCeded.inClaims = xl1 .outCededClaims;
        aggregateCeded.inClaims = xl2 .outCededClaims;
        aggregateGross.inClaims = aggregateClaims.outClaims;
        aggregateGross.inClaims = compoundClaims.outAggregateClaims;
    }

}
        """
        GraphImportService importService = new GraphImportService();
        ModelGraphModel  model = (ModelGraphModel) importService.importGraph(ccFile);
        
        ComponentNode xl1 = (ComponentNode) model.getAllComponentNodes().findAll {it -> it.name=="xl1"}[0]
        String xl1Path = GraphModelUtilities.getPath(xl1, model)
        assertTrue(xl1Path=="xl1")

        GraphElement inClaims = xl1.getPort("inClaims")
        String xl1InClaimsPath = GraphModelUtilities.getPath(inClaims, model)
        assertTrue(xl1InClaimsPath=="xl1:inClaims")

        ComposedComponentNode cc = (ComposedComponentNode) model.getAllComponentNodes().findAll {it -> it.name=="compoundClaims"}[0]
        assertTrue(GraphModelUtilities.getPath(cc, model)=="compoundClaims")

        ComponentNode freq = (ComponentNode) cc.componentGraph.getAllComponentNodes().findAll {it -> it.name=="subFrequencyGen"}[0]
        assertTrue(GraphModelUtilities.getPath(freq, model)=="compoundClaims:subFrequencyGen")

        GraphElement outFreq = freq.getPort("outFrequency")
        assertTrue(GraphModelUtilities.getPath(outFreq, model)=="compoundClaims:subFrequencyGen:outFrequency")
        
    }
}