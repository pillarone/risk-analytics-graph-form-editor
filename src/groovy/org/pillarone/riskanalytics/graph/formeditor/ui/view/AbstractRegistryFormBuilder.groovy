package org.pillarone.riskanalytics.graph.formeditor.ui.view

import com.ulcjava.applicationframework.application.form.AbstractFormBuilder
import com.ulcjava.applicationframework.application.form.TextFieldParameter
import com.ulcjava.base.application.ULCComponent
import com.ulcjava.base.application.event.IActionListener
import com.ulcjava.base.application.util.KeyStroke
import com.ulcjava.base.application.ULCTextField
import com.ulcjava.base.application.event.IValueChangedListener
import com.ulcjava.base.application.event.ValueChangedEvent
import com.ulcjava.base.application.event.IKeyListener
import com.ulcjava.base.application.event.KeyEvent
import com.ulcjava.applicationframework.application.form.model.FormModel

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */
abstract class AbstractRegistryFormBuilder<T extends FormModel> extends AbstractFormBuilder {

    List<TextFieldParameter> textFieldParameters = new ArrayList<TextFieldParameter>();

    public AbstractRegistryFormBuilder(T model) {
        super(model)
    }

    @Override
    protected TextFieldParameter addTextField(String propertyName) {
        TextFieldParameter textFieldParameter = super.addTextField(propertyName)
        textFieldParameter.getWidget().name = propertyName
        textFieldParameters.add(textFieldParameter)
        return textFieldParameter
    }

    public void registerKeyboardAction(KeyStroke enter, IActionListener action) {
        for (TextFieldParameter textFieldParameter: textFieldParameters) {
            ULCTextField textField = textFieldParameter.getWidget()
            textField.registerKeyboardAction(action, enter, ULCComponent.WHEN_FOCUSED);
        }
    }

    public void addKeyListener() {
        for (TextFieldParameter textFieldParameter: textFieldParameters) {
            ULCTextField textField = textFieldParameter.getWidget()
            textField.addKeyListener([keyTyped: {KeyEvent keyEvent ->
                ((FormModel) model).updatePresentationState()
            }] as IKeyListener)
        }
    }

    ULCComponent getComponent(String componentName) {
        for (TextFieldParameter textFieldParameter: textFieldParameters) {
            ULCTextField textField = textFieldParameter.getWidget()
            if (textField.name == componentName)
                return textField
        }
        return null
    }


}
