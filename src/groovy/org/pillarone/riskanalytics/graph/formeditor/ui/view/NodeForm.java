package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.applicationframework.application.form.AbstractFormBuilder;
import com.ulcjava.base.application.DefaultComboBoxModel;
import com.ulcjava.base.application.IComboBoxModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.NodeFormModel;
import org.pillarone.riskanalytics.graph.formeditor.util.PaletteUtilities;

import java.util.List;

public class NodeForm extends AbstractRegistryFormBuilder<NodeFormModel> {
    private boolean fIsModel;

    public NodeForm(NodeFormModel bean, boolean isModel) {
        super(bean);
        fIsModel = isModel;
    }

    @Override
    protected void initForm() {
        setColumnWeights(0f, 0f, 1f);
        addTextField("name").columns(15);
        List<String> typeNames = PaletteUtilities.getAvailableComponentNames(true);
        IComboBoxModel comboBoxModel = new DefaultComboBoxModel(typeNames);
        comboBoxModel.setSelectedItem(typeNames != null && typeNames.size() > 0 ? typeNames.get(0) : null);
        addComboBox("componentType", comboBoxModel);
        addTextField("comment").columns(15);
    }
}