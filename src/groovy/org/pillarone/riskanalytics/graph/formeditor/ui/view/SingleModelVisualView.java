package org.pillarone.riskanalytics.graph.formeditor.ui.view;


import com.ulcjava.applicationframework.application.AbstractBean;
import com.ulcjava.applicationframework.application.ApplicationContext;
import com.ulcjava.base.application.*;
import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.model.ModelGraphModel;
import org.pillarone.riskanalytics.graph.formeditor.util.GraphModelUtilities;

public class SingleModelVisualView extends AbstractBean implements GraphModelViewable {
    private ApplicationContext fApplicationContext;

    private AbstractGraphModel fGraphModel;
    private boolean fIsModel;

    private ULCBoxPane fMainView;

    public SingleModelVisualView(ApplicationContext ctx, AbstractGraphModel model) {
        super();
        fMainView = new ULCBoxPane(1, 1, 2, 2);
        ULCBoxPane content = new ULCBoxPane(true);
        fMainView.add(ULCBoxPane.BOX_EXPAND_EXPAND, content);
        fApplicationContext = ctx;
        injectGraphModel(model);
    }

    public ULCBoxPane getView() {
        return fMainView;
    }

    public void setVisible(boolean visible) {
        if (fMainView != null) {
            fMainView.setVisible(visible);
        }
    }

    public void injectGraphModel(AbstractGraphModel model) {
    }
}
