package org.pillarone.riskanalytics.graph.formeditor.ui.view

import com.ulcjava.base.application.ULCFrame
import com.ulcjava.testframework.standalone.AbstractSimpleStandaloneTestCase
import com.ulcjava.testframework.operator.ULCFrameOperator
import com.ulcjava.testframework.operator.ULCTreeOperator
import javax.swing.tree.TreeModel
import org.pillarone.riskanalytics.core.example.component.ExampleDynamicComponent
import com.ulcjava.testframework.ServerSideCommand
import org.pillarone.riskanalytics.graph.core.palette.service.PaletteService
import org.pillarone.riskanalytics.graph.core.palette.model.ComponentDefinition
import aTest.ATestComponent
import aTest.BTestComponent


class ComponentTypeTreeTests extends AbstractSimpleStandaloneTestCase {

    void start() {

        ComponentTypeTree tree = new ComponentTypeTree(null)

        ULCFrame frame = new ULCFrame("Test")
        frame.setDefaultCloseOperation(ULCFrame.TERMINATE_ON_CLOSE);
        frame.add(tree);

        frame.visible = true

    }

    void testAddNode() {
        ULCFrameOperator frame = new ULCFrameOperator()
        ULCTreeOperator tree = new ULCTreeOperator(frame)

        final TreeModel model = tree.getUITree().getBasicTree().getModel()
        final Object root = tree.getRoot()


        final Object org = model.getChild(root, 0)
        assertEquals "{0=org}", org.toString()
        assertEquals(1, model.getChildCount(root))

        runVoidCommand(new AddComponentDefinition(ATestComponent))
        assertEquals(2, model.getChildCount(root))
        final Object aTest = model.getChild(root, 0)
        assertEquals "{0=aTest}", aTest.toString()

        tree.doExpandRow(1)

        assertEquals(1, model.getChildCount(aTest))
        assertEquals("{0=${ATestComponent.simpleName}}", model.getChild(aTest, 0).toString())

        runVoidCommand(new AddComponentDefinition(BTestComponent))
        assertEquals(2, model.getChildCount(aTest))

        assertEquals("{0=${ATestComponent.simpleName}}", model.getChild(aTest, 0).toString())
        assertEquals("{0=${BTestComponent.simpleName}}", model.getChild(aTest, 1).toString())
    }
}