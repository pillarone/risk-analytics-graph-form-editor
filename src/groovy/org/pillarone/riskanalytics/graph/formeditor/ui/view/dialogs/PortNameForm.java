package org.pillarone.riskanalytics.graph.formeditor.ui.view.dialogs;

import org.pillarone.riskanalytics.graph.formeditor.ui.model.PortNameFormModel;

public class PortNameForm extends AbstractRegistryFormBuilder<PortNameFormModel> {

    public PortNameForm(PortNameFormModel bean) {
        super(bean);
    }

    @Override
    protected void initForm() {
        setColumnWeights(0f, 0f, 1f);
        addTextField("name").columns(15);
    }
}