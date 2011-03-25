package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.applicationframework.application.form.AbstractFormBuilder;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.TypeDefinitionFormModel;

public class TypeDefinitionForm extends AbstractFormBuilder<TypeDefinitionFormModel> {

    public TypeDefinitionForm(TypeDefinitionFormModel bean) {
        super(bean);
    }

    @Override
    protected void initForm() {
        setColumnWeights(0f, 0f, 1f);
        addTextField("name").columns(15);
        addTextField("packageName").columns(15);
        addCheckBox("model", null);
    }
}