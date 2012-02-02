package org.pillarone.riskanalytics.graph.formeditor.ui.view.dialogs

import com.ulcjava.base.application.event.IActionListener
import com.ulcjava.base.application.util.Dimension
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel
import org.pillarone.riskanalytics.graph.core.graph.persistence.GraphPersistenceService
import org.pillarone.riskanalytics.graph.formeditor.ui.IModelRenameListener
import com.ulcjava.base.application.*

class ModelRenameDialog extends ULCDialog {

    private ULCBoxPane content
    private AbstractGraphModel graphModel
    private ULCTextField nameField
    private ULCTextField packageField
    private ULCButton cancelButton
    private ULCButton enterButton

    private List<IModelRenameListener> modelRenameListener = []

    ModelRenameDialog(ULCComponent parent) {
        content = new ULCBoxPane(2, 3)
        content.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        content.add(ULCBoxPane.BOX_LEFT_CENTER, new ULCLabel("name"))
        nameField = new ULCTextField(30)
        content.add(ULCBoxPane.BOX_EXPAND_CENTER, nameField)

        content.add(ULCBoxPane.BOX_LEFT_CENTER, new ULCLabel("package"))
        packageField = new ULCTextField(30)
        content.add(ULCBoxPane.BOX_EXPAND_CENTER, packageField)

        content.add(2, ULCFiller.createGlue())

        cancelButton = new ULCButton("cancel")
        content.add(ULCBoxPane.BOX_LEFT_CENTER, cancelButton)
        enterButton = new ULCButton("ok")
        content.add(ULCBoxPane.BOX_RIGHT_CENTER, enterButton)

        cancelButton.addActionListener([actionPerformed: { dispose() }] as IActionListener)
        enterButton.addActionListener([actionPerformed: {
            String oldName = graphModel.name
            String oldPackageName = graphModel.packageName
            graphModel.setPackageName(packageField.text)
            graphModel.setName(nameField.text)
            GraphPersistenceService persistenceService = ApplicationHolder.application.mainContext.getBean(GraphPersistenceService)
            boolean propagateChange = true
            try {
                persistenceService.save(graphModel)
            } catch (Exception ex) {
                graphModel.setPackageName(oldPackageName)
                graphModel.setName(oldName)
                try {
                    persistenceService.delete(graphModel)
                    graphModel.setPackageName(packageField.text)
                    graphModel.setName(nameField.text)
                    persistenceService.save(graphModel)
                } catch (Exception ex1) {
                    ULCAlert alert = new ULCAlert("Model not renamed.", "Model could not be renamed - probably due to a problem in the repository.", "ok")
                    alert.show()
                    graphModel.setPackageName(oldPackageName)
                    graphModel.setName(oldName)
                    propagateChange = false
                }
            }
            if (propagateChange) {
                modelRenameListener.each { listener ->
                    listener.modelRenamed(graphModel, oldName, oldPackageName)
                }
            }
            dispose()
        }] as IActionListener)

        add(content)
        setSize(new Dimension(300, 100))
        setModal(true)
        setResizable(false)
        setLocationRelativeTo(UlcUtilities.getWindowAncestor(parent))
        setTitle("Rename")
    }
    
    void setGraphModel(AbstractGraphModel model) {
        graphModel = model
        nameField.setText(model.name)
        packageField.setText(model.packageName)
        setTitle("Rename ${graphModel.packageName}.${graphModel.name}")
    }
    
    void addModelRenameListener(IModelRenameListener listener) {
        modelRenameListener << listener
    }

    void removeModelRenameListener(IModelRenameListener listener) {
        if (modelRenameListener.contains(listener)) {
            modelRenameListener.remove(listener)
        }
    }
    
}
