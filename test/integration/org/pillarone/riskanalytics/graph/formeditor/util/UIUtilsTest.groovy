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
import org.pillarone.riskanalytics.graph.core.palette.model.ComponentDefinition

class UIUtilsTest extends GroovyTestCase {

    void testGetConnectionEntryName() {
        ComponentDefinition compDef = PaletteService.getInstance().getComponentDefinition(org.pillarone.riskanalytics.graph.formeditor.examples.ExcessOfLoss)
        ComponentNode node = ComponentNode.createInstance(compDef, "dummy")
        assertTrue(UIUtils.getConnectionEntryName(node.getPort("inClaims")).equals("dummy > claims"))
    }

    void testGetPortFromConnectionEntryName() {
        ComposedComponentGraphModel model = new ComposedComponentGraphModel("MyComposedComponent", "my.packag3")
        ComponentNode node = model.createComponentNode(PaletteService.instance.getComponentDefinition(SingleNormalClaimsGenerator), "subClaimsGenerator")
        InPort inPort = model.createOuterInPort(FrequencyPacket, "inFrequency")
        OutPort outPort = model.createOuterOutPort(ClaimPacket, "outClaims")
        model.createConnection(inPort, node.getPort("inFrequency"))
        model.createConnection(outPort, node.getPort("outClaims"))

        Port resolvedPort = UIUtils.getPortFromConnectionEntryName("claims generator > claims", model, false)
        Port port = node.getPort("outClaims")
        assertTrue(resolvedPort==port);
        resolvedPort = UIUtils.getPortFromConnectionEntryName("claims generator > claims", model, true)
        assertTrue(resolvedPort==null)
        resolvedPort = UIUtils.getPortFromConnectionEntryName("claims generator > frequency", model, true)
        port = node.getPort("inFrequency")
        assertTrue(resolvedPort==port);
        resolvedPort = UIUtils.getPortFromConnectionEntryName("claims generator > frequency", model, false)
        assertTrue(resolvedPort==null)
        resolvedPort = UIUtils.getPortFromConnectionEntryName("frequency", model, true)
        assertTrue(resolvedPort==inPort);
        resolvedPort = UIUtils.getPortFromConnectionEntryName("claims", model, false)
        assertTrue(resolvedPort==outPort);
    }

    void testGetWatchesDisplayName() {
        String pathsep = GraphModelUtilities.PATHSEP
        assertTrue(UIUtils.getWatchesDisplayName("model"+pathsep+"claimsGenerator"+pathsep+"outClaims").equals("model > claims generator > claims"))
    }
}