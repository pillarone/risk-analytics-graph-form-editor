package org.pillarone.riskanalytics.graph.formeditor.ui.model;

import com.ulcjava.applicationframework.application.form.model.FormModel;
import com.ulcjava.applicationframework.application.form.model.IValidator;
import com.ulcjava.applicationframework.application.form.model.PropertyValidator;
import org.pillarone.riskanalytics.graph.core.graph.model.*;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.NameBean;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.checkers.PropertySpellChecker;

/**
 * @author martin.melchior
 */
public class PortNameFormModel extends FormModel<NameBean> {

    private ComposedComponentGraphModel fGraphModel;
    private String portType;

    public PortNameFormModel(NameBean bean, ComposedComponentGraphModel graphModel) {
        super(bean);
        fGraphModel = graphModel;
    }

    public PortNameFormModel(NameBean bean, ComposedComponentGraphModel graphModel, String portType) {
        this(bean, graphModel);
        this.portType = portType;
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
        return new IValidator[]{new PropertySpellChecker("name"), new AvailabilityChecker(), new PortTypeChecker()};
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
            for (InPort p : fGraphModel.getOuterInPorts()) {
                if (p.getName().equals(value)) {
                    return "Name already " + value + " already exists.";
                }
            }
            for (OutPort p : fGraphModel.getOuterOutPorts()) {
                if (p.getName().equals(value)) {
                    return "Name already " + value + " already exists.";
                }
            }
            return null;
        }
    }

    private class PortTypeChecker extends PropertyValidator<String> {

        public PortTypeChecker() {
            super("name");
        }

        @Override
        public String validateValue(String value) {
            if (portType == null) return null;
            if (!(value != null && value.startsWith(portType) && value.length() > portType.length()))
                return "Not valid Name";
            return null;
        }
    }
}
