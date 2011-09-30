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
    @Override
    protected void setUp() {
        super.setUp()    //To change body of overridden methods use File | Settings | File Templates.
    }



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
        assertEquals "org", org.getValueAt(0)
        assertEquals(1, model.getChildCount(root))

        runVoidCommand(new ServerSideCommand() {

            @Override
            protected void proceedOnServer() {
                PaletteService.instance.addComponentDefinition(new ComponentDefinition(typeClass: ATestComponent))
            }

        })
        assertEquals(2, model.getChildCount(root))
        final Object aTest = model.getChild(root, 0)
        assertEquals "aTest", aTest.getValueAt(0)

        tree.doExpandRow(1)

        assertEquals(1, model.getChildCount(aTest))
        assertEquals(ATestComponent.simpleName, model.getChild(aTest, 0).getValueAt(0))

        runVoidCommand(new ServerSideCommand() {

            @Override
            protected void proceedOnServer() {
                PaletteService.instance.addComponentDefinition(new ComponentDefinition(typeClass: BTestComponent))
            }

        })
        assertEquals(2, model.getChildCount(aTest))

        assertEquals(ATestComponent.simpleName, model.getChild(aTest, 0).getValueAt(0))
        assertEquals(BTestComponent.simpleName, model.getChild(aTest, 1).getValueAt(0))
    }
}
