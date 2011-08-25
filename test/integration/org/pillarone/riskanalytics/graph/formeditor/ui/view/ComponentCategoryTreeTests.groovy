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


class ComponentCategoryTreeTests extends AbstractSimpleStandaloneTestCase {

    void start() {

        ComponentCategoryTree tree = new ComponentCategoryTree(null)

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

        tree.doExpandRow(4)

        final Object others = model.getChild(root, 3)
        assertEquals ExampleDynamicComponent.name, model.getChild(others, 0).getValueAt(0)

        runVoidCommand(new ServerSideCommand() {

            @Override
            protected void proceedOnServer() {
                PaletteService.instance.addComponentDefinition(new ComponentDefinition(typeClass: ATestComponent))
            }

        })
        assertEquals ATestComponent.name, model.getChild(others, 0).getValueAt(0)

        runVoidCommand(new ServerSideCommand() {

            @Override
            protected void proceedOnServer() {
                PaletteService.instance.addComponentDefinition(new ComponentDefinition(typeClass: BTestComponent))
            }

        })
        tree.doExpandRow(4)
        final Object myCategory = model.getChild(root, 3)

        assertEquals ATestComponent.name, model.getChild(others, 0).getValueAt(0)
        assertEquals BTestComponent.name, model.getChild(myCategory, 0).getValueAt(0)
    }
}
