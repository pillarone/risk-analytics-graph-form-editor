package org.pillarone.riskanalytics.graph.formeditor.util

import org.pillarone.riskanalytics.graph.core.graph.model.ComponentNode
import org.pillarone.riskanalytics.graph.core.graph.model.ComposedComponentGraphModel
import org.pillarone.riskanalytics.graph.core.graph.model.InPort
import org.pillarone.riskanalytics.graph.core.graph.model.OutPort
import org.pillarone.riskanalytics.graph.core.loader.ClassRepository
import org.pillarone.riskanalytics.graph.core.loader.ClassType
import org.pillarone.riskanalytics.graph.core.loader.DatabaseClassLoader
import org.pillarone.riskanalytics.graph.core.palette.service.PaletteService
import org.pillarone.riskanalytics.graph.formeditor.examples.ClaimPacket
import org.pillarone.riskanalytics.graph.formeditor.examples.FrequencyPacket
import org.pillarone.riskanalytics.graph.formeditor.examples.SingleNormalClaimsGenerator

class GraphModelUtilitiesTests extends GroovyTestCase {

    void setUp() {
        PaletteService.instance.clearCache()
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
        PaletteService.instance.clearCache()
    }
}
