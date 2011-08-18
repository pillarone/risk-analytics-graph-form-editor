package org.pillarone.riskanalytics.graph.formeditor.ui.view

import com.ulcjava.base.application.ULCBoxPane
import com.ulcjava.base.application.ULCFiller
import com.ulcjava.base.application.util.Color
import com.ulcjava.base.application.util.HTMLUtilities
import org.pillarone.riskanalytics.graph.core.graph.model.ComponentNode
import org.pillarone.riskanalytics.graph.core.graph.model.Connection
import org.pillarone.riskanalytics.graph.core.graph.model.filters.IComponentNodeFilter
import org.pillarone.riskanalytics.graph.core.graph.util.UIUtils
import com.ulcjava.base.application.ULCTextArea
import com.ulcjava.base.application.ULCCardPane
import com.ulcjava.base.application.ULCRadioButton
import com.ulcjava.base.application.ULCButton
import com.ulcjava.base.application.event.IActionListener
import com.ulcjava.base.application.event.ActionEvent
import com.ulcjava.base.application.BorderFactory
import com.ulcjava.base.application.ULCButtonGroup
import com.ulcjava.base.application.util.KeyStroke
import com.ulcjava.base.application.event.KeyEvent
import com.ulcjava.base.application.ULCComponent

/**
 * @author fouad.jaada@intuitive-collaboration.com, martin.melchior@fhnw.ch
 */
class CommentView implements ISelectionListener {

    ULCBoxPane content
    ResourceLinkHtmlPane propertiesPane
    ULCBoxPane buttonPane
    ULCCardPane descriptionPane
    ResourceLinkHtmlPane htmlTextPane
    ULCTextArea editableTextPane
    ULCButton editButton
    String currentText
    ComponentNode currentNode

    public CommentView() {
        init()
        attachListeners()
    }

    private void init() {
        content = new ULCBoxPane(true)
        propertiesPane = new ResourceLinkHtmlPane()

        descriptionPane = new ULCCardPane()
        descriptionPane.setBorder(BorderFactory.createTitledBorder("Comment"))
        htmlTextPane = new ResourceLinkHtmlPane()
        editableTextPane = new ULCTextArea()
        editableTextPane.setEditable(true)
        descriptionPane.addCard("text", editableTextPane)
        descriptionPane.addCard("html", htmlTextPane)

        buttonPane = new ULCBoxPane(false)
        editButton = new ULCButton("edit");
        editButton.setEnabled false
        editButton.setToolTipText("Edit the comment.");
        ULCButton okButton = new ULCButton("ok");
        okButton.setEnabled false

        ULCButton cancelButton = new ULCButton("cancel");
        cancelButton.setEnabled false

        editButton.addActionListener(
            new IActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    editableTextPane.setText(currentText)
                    descriptionPane.setSelectedComponent(editableTextPane);
                    editButton.setEnabled false
                    okButton.setEnabled true
                    cancelButton.setEnabled true
                }
            }
        )
        IActionListener okAction = new IActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                currentText = editableTextPane.getText()
                currentNode.comment = currentText
                String htmlText = "<div style='100%'> $currentText </div>"
                htmlTextPane.setText(HTMLUtilities.convertToHtml(htmlText ? htmlText : ""))
                descriptionPane.setSelectedComponent(htmlTextPane);
                editButton.setEnabled true
                okButton.setEnabled false
                cancelButton.setEnabled false
            }
        }
        okButton.addActionListener(okAction)

        IActionListener cancelAction = new IActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                currentText = currentNode.comment
                descriptionPane.setSelectedComponent(htmlTextPane);
                editButton.setEnabled true
                okButton.setEnabled false
                cancelButton.setEnabled false
            }
        }
        cancelButton.addActionListener(cancelAction)
        buttonPane.add(ULCBoxPane.BOX_LEFT_CENTER, editButton)
        buttonPane.add(ULCBoxPane.BOX_EXPAND_CENTER, ULCFiller.createHorizontalGlue())
        buttonPane.add(ULCBoxPane.BOX_LEFT_CENTER, cancelButton)
        buttonPane.add(ULCBoxPane.BOX_LEFT_CENTER, okButton)
        content.add(ULCBoxPane.BOX_EXPAND_TOP, propertiesPane)
        content.add(ULCBoxPane.BOX_EXPAND_EXPAND, descriptionPane)
        content.add(ULCBoxPane.BOX_LEFT_BOTTOM, buttonPane)
    }

    private void attachListeners() {
    }

    public void setSelectedComponents(List<ComponentNode> selectedNodes) {
        currentNode = selectedNodes!=null && selectedNodes.size()>0 ? selectedNodes[-1] : null
        if (currentNode) {
            editButton.setEnabled true
            
            // properties pane
            String title = currentNode.getName()
            String fullName = currentNode.type.getTypeClass().getName()
            //String htmlLink = "<a href='resourceKey:'$fullName > $title </a>"
            String htmlTitle = "<b><h3> $title </h3></b>"
            String htmlClassName = "<code> $fullName </code>"
            String text = "<div style='100%'>${htmlTitle + htmlClassName} </div>"
            propertiesPane.setText(HTMLUtilities.convertToHtml(text ? text : ""))

            // description pane
            currentText = currentNode.comment
            String htmlText = "<div style='100%'> $currentText </div>"
            htmlTextPane.setText(HTMLUtilities.convertToHtml(htmlText ? htmlText : ""))
            descriptionPane.setSelectedComponent(htmlTextPane)
        }
    }

    public void setSelectedConnections(List<Connection> selectedConnections) {
        // nothing to do here
    }
    
    public void applyFilter(IComponentNodeFilter filter) {
        // nothing to do here
    }

    public void clearSelection() {
        // properties pane
        propertiesPane.setText ""

        // description pane
        currentText = ""
        htmlTextPane.setText currentText
        descriptionPane.setComponentAt("html", htmlTextPane)
    }
}

