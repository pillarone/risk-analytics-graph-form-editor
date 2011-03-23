package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.applicationframework.application.form.AbstractFormBuilder;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.TypeImportFormModel;

public class TypeImportForm extends AbstractFormBuilder<TypeImportFormModel> {
    
    public TypeImportForm(TypeImportFormModel formModel) {
        super(formModel);
    }
    
    @Override
    protected void initForm() {
    	setColumnWeights(0f, 0f, 1f);
    	addTextField("clazzName").columns(25);
    }
}