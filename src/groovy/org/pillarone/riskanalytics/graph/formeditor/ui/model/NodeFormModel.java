package org.pillarone.riskanalytics.graph.formeditor.ui.model;

import com.ulcjava.applicationframework.application.form.model.FormModel;
import com.ulcjava.applicationframework.application.form.model.IValidator;
import com.ulcjava.applicationframework.application.form.model.PropertyValidator;
import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.model.ComponentNode;
import org.pillarone.riskanalytics.graph.core.graph.model.ComposedComponentGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.util.UIUtils;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.NodeBean;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.checkers.PropertySpellChecker;

/**
 * Model underlying the input form ({@link org.pillarone.riskanalytics.graph.formeditor.ui.view.NodeForm}) for nodes.
 * Is based on the generic {@link FormModel} specified for the {@link NodeBean}
 * introduced particularly for that purpose.
 *
 * @author martin.melchior
 */
public class NodeFormModel extends FormModel<NodeBean> {

    private AbstractGraphModel fGraphModel;
    private ComponentNode fEditedNode;

    public NodeFormModel(NodeBean bean, AbstractGraphModel graphModel) {
        super(bean);
        fGraphModel = graphModel;
        fEditedNode = null;
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
     * Give the form model a reference to the component node the bean (being edited in this form) is associated with.
     * This allows to identify and validate changes being done to the comment node.
     * Currently, it is just used for validating the input in the {@link AvailabilityChecker}.
     * A null value should be set before using the form in a new node action - the component node to be modified
     * should be set here when using the form in a modify action.
     *
     * @param editedNode
     */
    public void setEditedNode(ComponentNode editedNode) {
        fEditedNode = editedNode;
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
         * Distinguishes between "new node" and "modify" use scenarios:
         * When modifying a given node by changing the type the name may be the same and
         * hence can already occur in the given graph model.
         */
        public String validateValue(String value) {
            String technicalName = UIUtils.formatTechnicalName(value, ComponentNode.class, fGraphModel instanceof ComposedComponentGraphModel);
            if (fEditedNode == null) {
                for (ComponentNode n : fGraphModel.getAllComponentNodes()) {
                    if (n.getName().equals(technicalName)) {
                        return "Name already exists.";
                    }
                }
            } else {
                for (ComponentNode n : fGraphModel.getAllComponentNodes()) {
                    if (n != fEditedNode && n.getName().equals(technicalName)) {
                        return "Name already exists.";
                    }
                }
            }
            return null;
        }
    }
}
