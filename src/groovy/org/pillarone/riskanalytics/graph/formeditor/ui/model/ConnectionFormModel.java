package org.pillarone.riskanalytics.graph.formeditor.ui.model;

import com.ulcjava.applicationframework.application.form.model.FormModel;
import com.ulcjava.applicationframework.application.form.model.IValidator;
import com.ulcjava.base.shared.ErrorObject;
import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.model.Port;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.ConnectionBean;
import org.pillarone.riskanalytics.graph.formeditor.ui.view.ConnectionForm;
import org.pillarone.riskanalytics.graph.formeditor.util.GraphModelUtilities;

/**
 * Model underlying the input form ({@link ConnectionForm}) for connections.
 * Is based on the generic {@link FormModel} specified for the {@link org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.ConnectionBean}
 * introduced particularly for that purpose.
 * 
 * @author martin.melchior
 */
public class ConnectionFormModel extends FormModel<ConnectionBean> {
    
	private AbstractGraphModel fGraphModel; // TODO: needed for validation
    
    public ConnectionFormModel(ConnectionBean bean, AbstractGraphModel graphModel) {
        super(bean);
        fGraphModel = graphModel;
    }

    @Override
    protected IValidator[] createValidators() {
        return new IValidator[]{new PackageTypeValidator()};
    }

    private class PackageTypeValidator implements IValidator {
		public void validate(FormModel<?> formModel) {
			ConnectionBean bean = (ConnectionBean) formModel.getBean();
			String from = bean.getFrom();
			String to = bean.getTo();
			if (from != null && to != null) {
				Port pFrom = GraphModelUtilities.getPortFromName(from, fGraphModel);
				Port pTo = GraphModelUtilities.getPortFromName(to, fGraphModel);
				if (pFrom == null || pTo == null 
						|| !pTo.getPacketType().isAssignableFrom(pFrom.getPacketType())) {
					ErrorObject error = new ErrorObject("Packet types do not match!", null);
					formModel.setError("to", error);
				}
			}
		}
    }
}
