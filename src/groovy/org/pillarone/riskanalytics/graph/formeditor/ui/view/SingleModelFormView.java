package org.pillarone.riskanalytics.graph.formeditor.ui.view;


import com.ulcjava.applicationframework.application.AbstractBean;
import com.ulcjava.applicationframework.application.ApplicationContext;
import com.ulcjava.base.application.BorderFactory;
import com.ulcjava.base.application.ULCBoxPane;
import com.ulcjava.base.application.ULCScrollPane;
import com.ulcjava.base.application.ULCSplitPane;
import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.model.ComponentNode;
import org.pillarone.riskanalytics.graph.core.graph.model.Connection;
import org.pillarone.riskanalytics.graph.core.graph.model.filters.IComponentNodeFilter;

import java.util.ArrayList;
import java.util.List;

public class SingleModelFormView extends AbstractBean implements GraphModelViewable, ISelectionListener {
    private ApplicationContext fApplicationContext;

    private AbstractGraphModel fGraphModel;

    private ULCBoxPane fMainView;
    private ULCScrollPane fNodesPane;
    private ComponentNodesTable fNodesTable;

    private ULCScrollPane fConnectionsPane;
    private ConnectionsTable fConnectionsTable;


    public SingleModelFormView(ApplicationContext ctx) {
        super();
        fApplicationContext = ctx;
        createView();
    }

    public void createView() {
        fMainView = new ULCBoxPane(true, 1);

        fNodesPane = new ULCScrollPane();
        ULCBoxPane nodesPane = new ULCBoxPane(true);
        nodesPane.setBorder(BorderFactory.createTitledBorder("Components"));
        nodesPane.add(ULCBoxPane.BOX_EXPAND_EXPAND, fNodesPane);

        fConnectionsPane = new ULCScrollPane();
        ULCBoxPane connPane = new ULCBoxPane(true);
        connPane.setBorder(BorderFactory.createTitledBorder("Connections"));
        connPane.add(ULCBoxPane.BOX_EXPAND_EXPAND, fConnectionsPane);

        ULCSplitPane splitPane = new ULCSplitPane(ULCSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(nodesPane);
        splitPane.setRightComponent(connPane);
        splitPane.setDividerLocation(0.5);
        splitPane.setDividerSize(10);
        //splitPane.setDividerLocationAnimationEnabled(true);
        fMainView.add(ULCBoxPane.BOX_EXPAND_EXPAND, splitPane);
    }

    public ULCBoxPane getView() {
        return fMainView;
    }

    public void setVisible(boolean visible) {
        fMainView.setVisible(visible);
    }

    public void injectGraphModel(AbstractGraphModel model) {
        fGraphModel = model;

        fNodesTable = new ComponentNodesTable(fApplicationContext, fGraphModel);
        fNodesTable.setVisible(true);
        fNodesTable.addSelectionListener(fConnectionsTable);
        fNodesPane.setViewPortView(fNodesTable);

        fConnectionsTable = new ConnectionsTable(fApplicationContext, fGraphModel);
        fConnectionsTable.addSelectionListener(fNodesTable);
        fConnectionsPane.setViewPortView(fConnectionsTable);
    }

    public void addSelectionListener(ISelectionListener selectionListener) {
        fNodesTable.addSelectionListener(selectionListener);
        fConnectionsTable.addSelectionListener(selectionListener);
    }

    public void removeSelectionListener(ISelectionListener selectionListener) {
        fNodesTable.removeSelectionListener(selectionListener);
        fConnectionsTable.removeSelectionListener(selectionListener);
    }

    public void newNodeAction(String componentType) {
        fNodesTable.newNodeAction(componentType);
    }

    List<IVertexHelpListener> vertexHelpListeners = new ArrayList<IVertexHelpListener>();

    public void selectionChanged(String propertyKey) {
        for (IVertexHelpListener helpListener : vertexHelpListeners) {
            helpListener.updateView(propertyKey);
        }
    }

    public void addVertexHelpListener(IVertexHelpListener listener) {
        vertexHelpListeners.add(listener);
    }

    public void removeVertexHelpListener(IVertexHelpListener listener) {
        vertexHelpListeners.remove(listener);
    }

    public void applyFilter(IComponentNodeFilter filter) {
        fNodesTable.applyFilter(filter);
        fConnectionsTable.applyFilter(filter);
    }

    public void setSelectedComponents(List<ComponentNode> selection) {
        fNodesTable.setSelectedComponents(selection);
    }

    public void setSelectedConnections(List<Connection> selection) {
        fConnectionsTable.setSelectedConnections(selection);
    }
}
