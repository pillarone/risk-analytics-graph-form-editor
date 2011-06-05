package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.applicationframework.application.form.AbstractFormBuilder;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.NodeNameFormModel;

public class NodeNameForm extends AbstractFormBuilder<NodeNameFormModel> {

    public NodeNameForm(NodeNameFormModel bean) {
        super(bean);
    }

    @Override
    protected void initForm() {
        setColumnWeights(0f, 0f, 1f);
        addTextField("name").columns(15);
    }
}