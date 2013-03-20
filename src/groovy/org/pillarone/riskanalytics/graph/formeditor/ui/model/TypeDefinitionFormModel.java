package org.pillarone.riskanalytics.graph.formeditor.ui.model;

import com.ulcjava.applicationframework.application.form.model.FormModel;
import com.ulcjava.applicationframework.application.form.model.IValidator;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.TypeDefinitionBean;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.checkers.PropertySpellChecker;

/**
 * Model underlying the input form ({@link org.pillarone.riskanalytics.graph.formeditor.ui.view.dialogs.TypeDefinitionForm}) for type definitions.
 * Is based on the generic {@link FormModel} specified for the {@link TypeDefinitionBean}
 * introduced particularly for that purpose.
 *
 * @author martin.melchior
 */
public class TypeDefinitionFormModel extends FormModel<TypeDefinitionBean> {

    public TypeDefinitionFormModel(TypeDefinitionBean bean) {
        super(bean);
    }

    @Override
    protected IValidator[] createValidators() {
        return new IValidator[]{new PropertySpellChecker("name"), new PropertySpellChecker("packageName")};
    }
}
