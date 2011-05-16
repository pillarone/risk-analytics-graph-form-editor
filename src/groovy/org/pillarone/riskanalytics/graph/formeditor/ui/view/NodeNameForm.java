package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.applicationframework.application.form.AbstractFormBuilder;
import com.ulcjava.base.application.DefaultComboBoxModel;
import com.ulcjava.base.application.IComboBoxModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.NodeFormModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.NodeNameFormModel;
import org.pillarone.riskanalytics.graph.formeditor.util.PaletteUtilities;

import java.util.List;

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