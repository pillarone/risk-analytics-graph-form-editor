package org.pillarone.riskanalytics.graph.formeditor.ui.model;

import com.ulcjava.applicationframework.application.form.model.FormModel;
import com.ulcjava.applicationframework.application.form.model.IValidator;
import com.ulcjava.applicationframework.application.form.model.PropertyValidator;
import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.model.ComponentNode;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.NameBean;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.checkers.PropertySpellChecker;

/**
 * @author martin.melchior
 */
public class NodeNameFormModel extends FormModel<NameBean> {

    private AbstractGraphModel fGraphModel;
    private String fNodeName;

    public NodeNameFormModel(NameBean bean, AbstractGraphModel graphModel) {
        super(bean);
        fGraphModel = graphModel;
    }

    /**
     * Two validators are included at the moment:
     * <ul>
     * <li>The name must not start with a number but must start with a literal.</li>
     * <li>The name must be unique within the graph model. Note that this means that the name
     * needs to be unique at the given hierarchy level.</li>
     * </ul>
     */
    @Override
    protected IValidator[] createValidators() {
        return new IValidator[]{new PropertySpellChecker("name"), new AvailabilityChecker()};
    }

    /**
     * Checks whether the name entered in the form has not yet been chosen elsewhere in the graph model.
     *
     * @author martin.melchior
     */
    @SuppressWarnings("serial")
    private class AvailabilityChecker extends PropertyValidator<String> {
        public AvailabilityChecker() {
            super("name");
        }

        /**
         */
        public String validateValue(String value) {
            for (ComponentNode n : fGraphModel.getAllComponentNodes()) {
                if (n.getName().equals(value)) {
                    return "Name already " + value + " already exists.";
                }
            }
            return null;
        }
    }
}
