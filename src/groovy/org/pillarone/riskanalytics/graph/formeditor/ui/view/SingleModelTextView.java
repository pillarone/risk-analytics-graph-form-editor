package org.pillarone.riskanalytics.graph.formeditor.ui.view;


import com.ulcjava.applicationframework.application.AbstractBean;
import com.ulcjava.applicationframework.application.ApplicationContext;
import com.ulcjava.base.application.ULCAlert;
import com.ulcjava.base.application.ULCBoxPane;
import com.ulcjava.base.application.ULCScrollPane;
import com.ulcjava.base.application.ULCTextArea;
import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.model.ComponentNode;
import org.pillarone.riskanalytics.graph.core.graph.model.Connection;
import org.pillarone.riskanalytics.graph.core.graph.model.ModelGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.model.filters.IComponentNodeFilter;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.treetable.NodeNameFilter;
import org.pillarone.riskanalytics.graph.formeditor.util.GraphModelUtilities;

import java.util.List;

public class SingleModelTextView extends AbstractBean implements GraphModelViewable, ISelectionListener {
    private ApplicationContext fApplicationContext;

    private AbstractGraphModel fGraphModel;
    private boolean fIsModel;

    private ULCBoxPane fMainView;
    private ULCTextArea fTextArea;

    public SingleModelTextView(ApplicationContext ctx) {
        super();
        fApplicationContext = ctx;
        createView();
    }

    protected void createView() {
        fTextArea = new ULCTextArea();
        fTextArea.setEditable(false);
        ULCScrollPane scrollPane = new ULCScrollPane(fTextArea);
        fMainView = new ULCBoxPane(1, 1, 2, 2);
        fMainView.set(0, 0, ULCBoxPane.BOX_EXPAND_EXPAND, scrollPane);
    }

    private String getGroovyCode() {
        try {
            return GraphModelUtilities.getGroovyModelCode(fGraphModel);
        } catch (Exception ex) {
            ULCAlert alert = new ULCAlert("Groovy code not generated .", "Exception occurred: " + ex.getMessage(), "ok");
            alert.show();
            return null;
        }
    }

    public ULCBoxPane getView() {
        return fMainView;
    }

    public void setVisible(boolean visible) {
        fMainView.setVisible(visible);
    }

    public void injectGraphModel(AbstractGraphModel model) {
        fGraphModel = model;
        fIsModel = model instanceof ModelGraphModel;
        fTextArea.setText(getGroovyCode());
    }

    /////////////////////////////////////////
    // Implementation of ISelectionListener
    /////////////////////////////////////////

    public void applyFilter(IComponentNodeFilter filter) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void applyFilter(NodeNameFilter filter) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setSelectedComponents(List<ComponentNode> selection) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setSelectedConnections(List<Connection> selection) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void clearSelection() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
