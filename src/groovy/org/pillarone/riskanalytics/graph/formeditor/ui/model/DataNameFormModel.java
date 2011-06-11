package org.pillarone.riskanalytics.graph.formeditor.ui.model;

import com.ulcjava.applicationframework.application.form.model.FormModel;
import com.ulcjava.applicationframework.application.form.model.IValidator;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.NameBean;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.checkers.PropertySpellChecker;

/**
 * @author martin.melchior
 */
public class DataNameFormModel extends FormModel<NameBean> {

    public DataNameFormModel(NameBean bean) {
        super(bean);
    }

    protected IValidator[] createValidators() {
        return new IValidator[]{new PropertySpellChecker("name")};
    }
}
