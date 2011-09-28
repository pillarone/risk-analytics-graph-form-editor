package org.pillarone.riskanalytics.graph.formeditor.ui.view

import com.ulcjava.base.application.ULCDialog
import com.ulcjava.base.application.ULCBoxPane
import com.ulcjava.base.application.ULCLabel
import com.ulcjava.base.application.ULCTextField
import com.ulcjava.base.application.ULCFiller
import com.ulcjava.base.application.ULCButton
import com.ulcjava.base.application.util.Dimension
import com.ulcjava.base.application.BorderFactory
import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel
import com.ulcjava.base.application.event.IActionListener
import org.pillarone.riskanalytics.graph.formeditor.ui.model.ModelRepositoryTreeNode
import com.ulcjava.base.application.ULCTree
import com.ulcjava.base.application.UlcUtilities
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.pillarone.riskanalytics.graph.core.graph.persistence.GraphPersistenceService
import com.ulcjava.base.application.tree.AbstractTreeModel
import com.ulcjava.base.application.tree.DefaultTreeModel
import com.ulcjava.base.application.tree.TreePath


class ModelRenameDialog extends ULCDialog {

    private ULCBoxPane content
    private AbstractGraphModel graphModel
    private ULCTextField nameField
    private ULCTextField packageField
    private ULCButton cancelButton
    private ULCButton enterButton

    ModelRenameDialog(AbstractGraphModel graphModel, ULCTree tree, ModelRepositoryTreeNode node) {
        this.graphModel = graphModel
        content = new ULCBoxPane(2, 3)
        content.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        content.add(ULCBoxPane.BOX_LEFT_CENTER, new ULCLabel("name"))
        nameField = new ULCTextField(graphModel.name)
        content.add(ULCBoxPane.BOX_EXPAND_CENTER, nameField)

        content.add(ULCBoxPane.BOX_LEFT_CENTER, new ULCLabel("package"))
        packageField = new ULCTextField(graphModel.packageName)
        content.add(ULCBoxPane.BOX_EXPAND_CENTER, packageField)

        content.add(2, ULCFiller.createGlue())

        cancelButton = new ULCButton("cancel")
        content.add(ULCBoxPane.BOX_LEFT_CENTER, cancelButton)
        enterButton = new ULCButton("ok")
        content.add(ULCBoxPane.BOX_RIGHT_CENTER, enterButton)

        cancelButton.addActionListener([actionPerformed: { dispose() }] as IActionListener)
        enterButton.addActionListener([actionPerformed: {
            graphModel.setPackageName(packageField.text)
            graphModel.setName(nameField.text)
            ApplicationHolder.application.mainContext.getBean(GraphPersistenceService).save(graphModel)

            node.packageName = graphModel.packageName
            node.name = graphModel.name
            final AbstractTreeModel model = tree.model
            model.nodeChanged(new TreePath(DefaultTreeModel.getPathToRoot(node) as Object[]))

            dispose()
        }] as IActionListener)

        add(content)
        setSize(new Dimension(300, 100))
        setModal(true)
        setResizable(false)
        setLocationRelativeTo(UlcUtilities.getWindowAncestor(tree))
        setTitle("Rename ${graphModel.packageName}.${graphModel.name}")
    }
}
