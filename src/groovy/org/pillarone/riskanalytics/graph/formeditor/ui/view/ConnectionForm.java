package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.applicationframework.application.form.AbstractFormBuilder;
import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.AvailablePortsComboBoxModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.ConnectionFormModel;

public class ConnectionForm extends AbstractFormBuilder<ConnectionFormModel> {
    
	AbstractGraphModel fGraphModel;
	
	
    public ConnectionForm(ConnectionFormModel formModel, AbstractGraphModel model) {
        super(formModel);
        fGraphModel = model;
    }
    
    public void refresh() {
    	
    }
    
    @Override
    protected void initForm() {
    	setColumnWeights(0f, 0f, 1f);
    	addComboBox("from", new AvailablePortsComboBoxModel(fGraphModel, false));
    	addComboBox("to", new AvailablePortsComboBoxModel(fGraphModel, true));
    }
}