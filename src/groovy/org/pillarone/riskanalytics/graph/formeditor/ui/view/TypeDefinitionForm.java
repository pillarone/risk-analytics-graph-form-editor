package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.applicationframework.application.form.AbstractFormBuilder;
import com.ulcjava.applicationframework.application.form.TextFieldParameter;
import com.ulcjava.base.application.DefaultComboBoxModel;
import com.ulcjava.base.application.ULCComponent;
import com.ulcjava.base.application.event.IActionListener;
import com.ulcjava.base.application.util.KeyStroke;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.TypeDefinitionFormModel;

import java.util.ArrayList;
import java.util.List;

public class TypeDefinitionForm extends AbstractFormBuilder<TypeDefinitionFormModel> {

    List<TextFieldParameter> textFieldParameters = new ArrayList<TextFieldParameter>();

    public TypeDefinitionForm(TypeDefinitionFormModel bean) {
        super(bean);
    }

    @Override
    protected void initForm() {
        setColumnWeights(0f, 0f, 1f);
        DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel(new String[]{"Model", "Composed Component"});
        comboBoxModel.setSelectedItem("Model");
        addComboBox("baseType", comboBoxModel);
        textFieldParameters.add(addTextField("name").columns(15));
        textFieldParameters.add(addTextField("packageName").columns(15));

    }

    public void registerKeyboardAction(KeyStroke enter, IActionListener action){
        for(TextFieldParameter textFieldParameter: textFieldParameters){
            textFieldParameter.getWidget().registerKeyboardAction(action, enter, ULCComponent.WHEN_FOCUSED);
        }
    }


}