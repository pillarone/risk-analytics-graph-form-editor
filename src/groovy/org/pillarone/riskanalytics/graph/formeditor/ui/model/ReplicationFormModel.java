package org.pillarone.riskanalytics.graph.formeditor.ui.model;

import com.ulcjava.applicationframework.application.form.model.FormModel;
import com.ulcjava.applicationframework.application.form.model.IValidator;
import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.ReplicationBean;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.checkers.PropertySpellChecker;

/**
 * Model underlying the input form ({@link org.pillarone.riskanalytics.graph.formeditor.ui.view.ReplicationForm}) for replicating connections.
 * Is based on the generic {@link FormModel} specified for the {@link ReplicationBean}
 * introduced particularly for that purpose.
 *
 * @author martin.melchior
 */
public class ReplicationFormModel extends FormModel<ReplicationBean> {

    public ReplicationFormModel(ReplicationBean bean, AbstractGraphModel graphModel) {
        super(bean);
    }

    @Override
    protected IValidator[] createValidators() {
        return new IValidator[]{new PropertySpellChecker("outer")};
    }
}
