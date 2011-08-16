package org.pillarone.riskanalytics.graph.formeditor.ui.view;


import com.canoo.ulc.graph.ULCGraph;
import com.canoo.ulc.graph.ULCGraphComponent;
import com.canoo.ulc.graph.dnd.GraphTransferData;
import com.canoo.ulc.graph.dnd.GraphTransferHandler;
import com.canoo.ulc.graph.event.DuplicateIdException;
import com.canoo.ulc.graph.event.IGraphListener;
import com.canoo.ulc.graph.model.Edge;
import com.canoo.ulc.graph.model.Port;
import com.canoo.ulc.graph.model.Vertex;
import com.canoo.ulc.graph.shared.PortConstraint;
import com.canoo.ulc.graph.shared.PortTemplate;
import com.canoo.ulc.graph.shared.PortType;
import com.canoo.ulc.graph.shared.ShapeTemplate;
import com.ulcjava.applicationframework.application.AbstractBean;
import com.ulcjava.applicationframework.application.Action;
import com.ulcjava.applicationframework.application.ApplicationActionMap;
import com.ulcjava.applicationframework.application.ApplicationContext;
import com.ulcjava.applicationframework.application.form.BeanFormDialog;
import com.ulcjava.base.application.*;
import com.ulcjava.base.application.dnd.DataFlavor;
import com.ulcjava.base.application.dnd.Transferable;
import com.ulcjava.base.application.event.*;
import com.ulcjava.base.application.util.Dimension;
import com.ulcjava.base.application.util.KeyStroke;
import com.ulcjava.base.application.util.Point;
import com.ulcjava.base.application.util.Rectangle;
import org.codehaus.groovy.grails.commons.ApplicationHolder;
import org.jetbrains.annotations.NotNull;
import org.pillarone.riskanalytics.graph.core.graph.model.*;
import org.pillarone.riskanalytics.graph.core.graph.model.filters.IComponentNodeFilter;
import org.pillarone.riskanalytics.graph.core.graph.util.UIUtils;
import org.pillarone.riskanalytics.graph.core.layout.ComponentLayout;
import org.pillarone.riskanalytics.graph.core.layout.GraphLayoutService;
import org.pillarone.riskanalytics.graph.core.palette.model.ComponentDefinition;
import org.pillarone.riskanalytics.graph.core.palette.service.PaletteService;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.NodeNameFormModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.NameBean;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.NodeBean;
import org.pillarone.riskanalytics.graph.formeditor.util.GraphModelUtilities;
import org.pillarone.riskanalytics.graph.formeditor.util.GroovyUtils;
import org.pillarone.riskanalytics.graph.formeditor.util.VisualSceneUtilities;

import java.lang.reflect.Field;
import java.util.*;

public class SingleModelVisualView extends AbstractBean implements GraphModelViewable, ISelectionListener {
    private ApplicationContext fApplicationContext;

    private AbstractGraphModel fGraphModel;
    Map<String, ComponentNode> fNodesMap;
    Map<Vertex, ComponentNode> fNodesToBeAdded;
    Map<String, Connection> fConnectionsMap;
    List<Connection> fConnectionsToBeAdded;

    List<ISelectionListener> fSelectionListeners;

    private ULCGraph fULCGraph;
    private ULCGraphComponent fULCGraphComponent;
    Vertex fRootVertex;
    Point fCurrentPosition;

    private ULCBoxPane fMainView;

    private GraphLayoutService fLayoutService;

    public SingleModelVisualView(ApplicationContext ctx, boolean isModel) {
        super();
        fApplicationContext = ctx;
        createView(isModel);
        fULCGraphComponent.setTransferHandler(new VertexDropHandler(fULCGraphComponent));
        fSelectionListeners = new ArrayList<ISelectionListener>();
    }

    protected void createView(boolean isModel) {
        fMainView = new ULCBoxPane(1, 1, 2, 2);
        ULCBoxPane content = new ULCBoxPane(2, 1);
        fMainView.add(ULCBoxPane.BOX_EXPAND_EXPAND, content);
        if (isModel) {
            fULCGraph = new ULCGraph();
        } else {
            fRootVertex = new Vertex("root" + System.currentTimeMillis()+Math.random());
            fRootVertex.setRectangle(new Rectangle(5, 5, 800, 500));
            try {
                fULCGraph = new ULCGraph(fRootVertex);
            } catch(DuplicateIdException ex) {

            }
        }
        fULCGraph.addGraphListener(new ULCGraphListener());
        fULCGraphComponent = new ULCGraphComponent(fULCGraph);
        content.add(ULCBoxPane.BOX_EXPAND_EXPAND, fULCGraphComponent);
        fULCGraph.upload();
        fULCGraphComponent.upload();
    }

    public ULCBoxPane getView() {
        return fMainView;
    }

    public ULCGraph getULCGraph() {
        return fULCGraph;
    }

    public ULCGraphComponent getULCGraphComponent() {
        return fULCGraphComponent;
    }

    public void setVisible(boolean visible) {
        fMainView.setVisible(visible);
        if (visible && fGraphModel!=null) {
            updateULCGraph();
        }
    }

    public void injectGraphModel(AbstractGraphModel model) {
        fGraphModel = model;
        fGraphModel.addGraphModelChangeListener(new GraphModelListener());

        fNodesMap = new LinkedHashMap<String, ComponentNode>();
        fNodesToBeAdded = new LinkedHashMap<Vertex, ComponentNode>();
        for (ComponentNode node : model.getAllComponentNodes()) {
            Vertex vertex = VisualSceneUtilities.createVertex(null, node.getType().getTypeClass().getName());
            vertex.setTitle(node.getName());
            fNodesToBeAdded.put(vertex, node);
        }

        fConnectionsMap = new LinkedHashMap<String, Connection>();
        fConnectionsToBeAdded = new ArrayList<Connection>();
        for (Connection c : model.getAllConnections()) {
            fConnectionsToBeAdded.add(c);
        }

        // this can be created only now - since now we know whether we have a Model or a ComposedComponent
        fULCGraphComponent.setComponentPopupMenu(createPopupMenu());

        if (fRootVertex != null) {
            fRootVertex.setTitle(fGraphModel.getName());
        }

        if (fMainView.isVisible()) {
            updateULCGraph();
        }
    }

    private void updateULCGraph() {
        boolean newLayout = fNodesToBeAdded.size()>0;
        for (Vertex v : fNodesToBeAdded.keySet()) {
            try {
                fULCGraph.addVertex(v);
                fULCGraphComponent.shrinkVertex(v);
                ComponentNode node = fNodesToBeAdded.get(v);
                fNodesMap.put(v.getId(), node);
            } catch (Exception ex) {
                System.out.println("Problem occurred while adding vertex to ULC graph: \n" + ex.getMessage());
            }
        }
        fNodesToBeAdded = new LinkedHashMap<Vertex, ComponentNode>();

        // construct and add edges associated with the connections registered for inclusion
        // note that the edges can be created only after all the vertices are available.
        for (Connection c : fConnectionsToBeAdded) {
            try {
                Port outPort = getULCPort(c.getFrom());
                Port inPort = getULCPort(c.getTo());
                String id = "conn_" + System.currentTimeMillis() + "_" + Math.random();
                Edge e = new Edge(id, outPort, inPort);
                fULCGraph.addEdge(e);
                fConnectionsMap.put(e.getId(), c);
            } catch (Exception ex) {
                System.out.println("Problem occurred while adding edge to ULC graph: \n" + ex.getMessage());
            }
        }
        fConnectionsToBeAdded = new ArrayList<Connection>();

        if (newLayout) {
            fULCGraphComponent.layout();
        }
    }

    @Action
    public void replicatePortAction() {
        ComposedComponentGraphModel ccGraphModel = (ComposedComponentGraphModel) fGraphModel;
        Set<Port> selectedPorts = fULCGraph.getSelectionModel().getSelectedPorts();
        for (Port port : selectedPorts) {
            org.pillarone.riskanalytics.graph.core.graph.model.Port graphPort = getGraphPort(port);
            if (graphPort != null) {
                if (graphPort instanceof InPort) {
                    InPort outerPort = ccGraphModel.createOuterInPort(graphPort.getPacketType(), graphPort.getName()); // TODO --> check if name is already occupied, ask for name in dialog
                    ccGraphModel.createConnection(outerPort, graphPort);
                } else {
                    OutPort outerPort = ccGraphModel.createOuterOutPort(graphPort.getPacketType(), graphPort.getName()); // TODO --> check if name is already occupied, ask for name in dialog
                    ccGraphModel.createConnection(graphPort, outerPort);
                }
            } else {
                ULCAlert alert = new ULCAlert("Port not replicated.",
                        "Port " + port.getTitle() + " cannot be replicated.", "ok");
                alert.show();
            }
        }
    }

    private ULCPopupMenu createPopupMenu() {
        ULCPopupMenu popupMenu = new ULCPopupMenu();
        ApplicationActionMap actionMap = fApplicationContext.getActionMap(this);
        if (fGraphModel instanceof ComposedComponentGraphModel) {
            ULCMenuItem replicatePortItem = new ULCMenuItem("replicate port");
            replicatePortItem.addActionListener(actionMap.get("replicatePortAction"));
            popupMenu.add(replicatePortItem);
        }
        return popupMenu;
    }

    private org.pillarone.riskanalytics.graph.core.graph.model.Port getGraphPort(Port ulcPort) {
        Vertex vertex = getParentVertex(ulcPort);
        if (vertex == null) {
            System.out.println("Parent vertex not found for port " + ulcPort.getId());
            return null;
        }
        ComponentNode node = fNodesMap.get(vertex.getId());
        if (node == null) {
            System.out.println("No node to given vertex found: " + vertex.getTitle());
            return null;
        }
        String name = ulcPort.getType() + ulcPort.getTitle();
        name = name.replaceAll(" ", "");
        return node.getPort(name);
    }

    private Vertex getParentVertex(Port p) {
        for (String vertexId : fNodesMap.keySet()) {
            Vertex vertex = fULCGraph.getVertex(vertexId);
            if (vertex != null && vertex.getPorts().contains(p)) {
                return vertex;
            }
        }
        return null;
    }

    private Port getULCPort(org.pillarone.riskanalytics.graph.core.graph.model.Port p) {
        Vertex v = null;
        if (!p.isComposedComponentOuterPort()) {
            ComponentNode node = p.getComponentNode();
            Iterator<Map.Entry<String, ComponentNode>> it = fNodesMap.entrySet().iterator();
            while (v == null && it.hasNext()) {
                Map.Entry<String, ComponentNode> e = it.next();
                if (e.getValue() == node) {
                    v = fULCGraph.getVertex(e.getKey());
                }
            }
        } else {
            v = fRootVertex;
        }
        if (v != null) {
            for (Port port : v.getPorts()) {
                if (VisualSceneUtilities.isConsistentPort(port, p)) {
                    return port;
                }
            }
        }
        return null;
    }

    private GraphLayoutService getPersistenceService() {
        if (fLayoutService == null) {
            org.springframework.context.ApplicationContext ctx = ApplicationHolder.getApplication().getMainContext();
            fLayoutService = ctx.getBean(GraphLayoutService.class);
        }

        return fLayoutService;
    }

    private void saveLayout(long uid, String name) {
        String graphName = fGraphModel.getPackageName() + "." + fGraphModel.getName();
        if (getPersistenceService().findLayout(uid, name, graphName)) {
            //Dialog to verify if overwrite existing save
            return;
        }
        List<ComponentLayout> components = new ArrayList<ComponentLayout>();
        Map<ComponentNode, String> inverse = GraphModelUtilities.invertMap(fNodesMap);
        for (Map.Entry<String, List<ComponentNode>> entry : GraphModelUtilities.getComponentPaths(fNodesMap).entrySet()) {
            for (ComponentNode n : entry.getValue()) {
                Vertex v = fULCGraph.getVertex(inverse.get(n));
                if (v != null) {
                    ComponentLayout cl = new ComponentLayout();
                    cl.setName(entry.getKey());
                    cl.setType(n.getType().getTypeClass());
                    cl.setX(0);
                    cl.setY(0);
                    cl.setH(0);
                    cl.setW(0);
                    cl.setUnfolded(!v.isCollapsed());
                    components.add(cl);
                }
            }
        }
        getPersistenceService().saveLayout(uid, name, graphName, components);
    }

    private void loadLayout(long uid, String name) {
        String graphName = fGraphModel.getPackageName() + "." + fGraphModel.getName();
        Map<ComponentNode, String> inverse = GraphModelUtilities.invertMap(fNodesMap);
        Map<String, List<ComponentNode>> paths = GraphModelUtilities.getComponentPaths(fNodesMap);
        Set<ComponentLayout> components = getPersistenceService().loadLayout(uid, name, graphName);
        for (ComponentLayout cl : components) {
            List<ComponentNode> cList = paths.get(cl.getName());
            if (cList != null) {
                ComponentNode match = null;
                //match=find{it.typeClass.equals(cl.getTyp)}
                for (ComponentNode n : cList) {
                    if (n.getType().getTypeClass().equals(cl.getType())) {
                        match = n;
                        break;
                    }
                }
                if (match != null) {
                    Vertex v = fULCGraph.getVertex(inverse.get(match));
                    if (v != null) {
                        v.setRectangle(new Rectangle(cl.getX(), cl.getY(), cl.getW(), cl.getH()));
                        v.setCollapsed(!cl.getUnfolded());
                    }
                }
            }
        }
    }

    public void addSelectionListener(ISelectionListener selectionListener) {
        fSelectionListeners.add(selectionListener);
    }

    public void removeSelectionListener(ISelectionListener selectionListener) {
        if (fSelectionListeners.contains(selectionListener)) {
            fSelectionListeners.remove(selectionListener);
        }
    }

    public void applyFilter(IComponentNodeFilter filter) {

    }

    public void setSelectedComponents(List<ComponentNode> selection) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setSelectedConnections(List<Connection> selection) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Reacts to changes in the ULC graph - updates the graph model
     */
    private class ULCGraphListener implements IGraphListener {

        public void vertexAdded(@NotNull Vertex vertex) {
            if (vertex.getId() == null) {
                // in case the id is null the vertex has been created on client side
                String compDef = vertex.getTemplateId();
                String name = vertex.getTitle();
                NodeEditDialog nodeEditDialog = new NodeEditDialog(UlcUtilities.getWindowAncestor(fULCGraphComponent), fGraphModel);
                nodeEditDialog.setModal(true);
                nodeEditDialog.setVisible(true);
                NodeBean bean = nodeEditDialog.getBeanForm().getModel().getBean();
                bean.reset();
                bean.setComponentType(compDef);
                bean.setName(name);
                nodeEditDialog.getBeanForm().getModel().setEditedNode(null);
                nodeEditDialog.setEditedNode(null);
                fCurrentPosition = vertex.getRectangle().getLocation();
                vertex.setId("noname_" + System.currentTimeMillis() + Math.random());
                fULCGraph.removeVertex(vertex);
            } else {
                fULCGraphComponent.shrinkVertex(vertex);
            }
        }

        public void vertexRemoved(@NotNull Vertex vertex) {
            if (fNodesMap.containsKey(vertex.getId())) {
                ComponentNode node = fNodesMap.get(vertex.getId());
                fGraphModel.removeComponentNode(node);
            }
        }

        public void edgeAdded(@NotNull Edge edge) {
            if (edge.getId()==null) {
                edge.setId("conn_" + System.currentTimeMillis() + "_" + Math.random());
            }
            org.pillarone.riskanalytics.graph.core.graph.model.Port outPort = getGraphPort(fULCGraph.getPort(edge.getSourceId()));
            org.pillarone.riskanalytics.graph.core.graph.model.Port inPort = getGraphPort(fULCGraph.getPort(edge.getTargetId()));
            if (outPort != null && inPort != null) {
                Connection c = fGraphModel.createConnection(outPort, inPort);
                fConnectionsMap.put(edge.getId(), c);
            } else {
                System.out.println("Ports could not be properly identified - no connection added to the graph model.");
                fULCGraph.removeEdge(edge);
            }
        }

        public void edgeRemoved(@NotNull Edge edge) {
            if (fConnectionsMap.containsKey(edge.getId())) {
                Connection conn = fConnectionsMap.get(edge.getId());
                fConnectionsMap.remove(edge.getId());
                fGraphModel.removeConnection(conn);
            }
        }
    }

    private class GraphModelListener implements IGraphModelChangeListener {

        public void nodeAdded(ComponentNode node) {
            Vertex vertex = VisualSceneUtilities.createVertex(fCurrentPosition, node.getType().getTypeClass().getName());
            vertex.setTitle(node.getName());
            if (fMainView.isVisible()) {
                fNodesMap.put(vertex.getId(), node);
                try {
                    fULCGraph.addVertex(vertex);
                    fULCGraphComponent.shrinkVertex(vertex);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                fNodesToBeAdded.put(vertex, node);
            }
        }

        public void nodeRemoved(ComponentNode node) {
            if (fNodesToBeAdded.containsValue(node)) {
                Vertex v = null;
                Iterator<Map.Entry<Vertex, ComponentNode>> it = fNodesToBeAdded.entrySet().iterator();
                while (it.hasNext() && v == null) {
                    Map.Entry<Vertex, ComponentNode> entry = it.next();
                    if (entry.getValue().equals(node)) {
                        v = entry.getKey();
                    }
                }
                if (v != null) {
                    fNodesToBeAdded.remove(v);
                }
            } else if (fNodesMap.containsValue(node)) {
                Vertex v = null;
                Iterator<Map.Entry<String, ComponentNode>> it = fNodesMap.entrySet().iterator();
                while (it.hasNext() && v == null) {
                    Map.Entry<String, ComponentNode> entry = it.next();
                    if (entry.getValue().equals(node)) {
                        v = fULCGraph.getVertex(entry.getKey());
                    }
                }
                if (v != null) {
                    fNodesMap.remove(v.getId());
                    fULCGraph.removeVertex(v);
                }
                fULCGraph.upload();
            }
        }

        public void outerPortAdded(org.pillarone.riskanalytics.graph.core.graph.model.Port p) {
            String id = "in_port_" + new Date().getTime() + "_"+Math.random();
            Port port;
            if (p instanceof org.pillarone.riskanalytics.graph.core.graph.model.InPort) {
                port = new Port(id, PortType.IN, p.getPacketType().getName(), UIUtils.formatDisplayName(p.getName()));
            } else {
                port = new Port(id, PortType.OUT, p.getPacketType().getName(), UIUtils.formatDisplayName(p.getName()));
            }
            port.addConstraint(new PortConstraint(p.getPacketType().getName(), 0, 100)); // TODO - set the correct constraint here
            fRootVertex.addPort(port);
            fULCGraph.updateElement(fRootVertex);
        }

        public void outerPortRemoved(org.pillarone.riskanalytics.graph.core.graph.model.Port p) {
            Port ulcPort = null;
            Iterator<Port> it = fRootVertex.getPorts().iterator();
            while (ulcPort==null && it.hasNext()) {
                Port p0 = it.next();
                String name = p0.getType().toString()+p0.getTitle();
                name = name.replaceAll(" ","");
                if (p.getName().equalsIgnoreCase(name)) {
                    ulcPort = p0;
                }
            }
            if (ulcPort != null) {
                
            }
        }

        public void nodesSelected(List<ComponentNode> nodes) {
            //nothing to do
        }

        public void connectionsSelected(List<Connection> connections) {
            //nothing to do
        }

        public void selectionCleared() {
            //nothing to do
        }

        public void filtersApplied() {
            // TODO - once the selection possibilities are available
        }

        public void nodePropertyChanged(ComponentNode node, String propertyName, Object oldValue, Object newValue) {
            if (!fNodesToBeAdded.values().contains(node)) {
                for (Map.Entry<String, ComponentNode> e : fNodesMap.entrySet()) {
                    if (e.getValue() == node) {
                        Vertex v = fULCGraph.getVertex(e.getKey());
                        if (v != null) {
                            v.setTitle(node.getName());
                            fULCGraph.upload();
                            return;
                        }
                    }
                }
            } else {

            }
        }

        public void connectionAdded(Connection c) {
            if (!fMainView.isVisible()) {
                fConnectionsToBeAdded.add(c);
            }
        }

        public void connectionRemoved(Connection c) {
            if (fConnectionsToBeAdded.contains(c)) {
                fConnectionsToBeAdded.remove(c);
            } else if (fConnectionsMap.containsValue(c)) {
                Edge e = null;
                Iterator<Map.Entry<String, Connection>> it = fConnectionsMap.entrySet().iterator();
                while (it.hasNext() && e == null) {
                    Map.Entry<String, Connection> entry = it.next();
                    if (entry.getValue() == c) {
                        e = fULCGraph.getEdge(entry.getKey());
                    }
                }
                if (e != null) {
                    fConnectionsMap.remove(e.getId());
                    fULCGraph.removeEdge(e);
                }
                fULCGraph.upload();
            }
        }
    }

    private class VertexDropHandler extends GraphTransferHandler {

        private VertexDropHandler(ULCGraphComponent inGraphComponent) {
            super(inGraphComponent);
        }

        @Override
        public boolean importData(final ULCComponent inTargetComponent, final Transferable inTransferable) {
            final Object data = inTransferable.getTransferData(DataFlavor.DROP_FLAVOR);
            if (data instanceof GraphTransferData) {
                final GraphTransferData transferData = (GraphTransferData) data;

                String compDef = null;

                Vertex vertex = (transferData).getTransferredVertex();
                // try first whether the dragged object is already a vertex (then it comes from a palette)
                if (vertex != null) {
                    compDef = vertex.getTemplateId();
                    fCurrentPosition = vertex.getRectangle().getLocation();
                } else {
                // if not returned yet it has not been a vertex
                // then it typically comes from a TypeTreeNode
                // try to collect the information needed from there
                    final Point mouseLocation = transferData.getMouseLocation();
                    compDef = transferData.getTransferString();
                    fCurrentPosition = mouseLocation;
                }

                NodeEditDialog nodeEditDialog = new NodeEditDialog(UlcUtilities.getWindowAncestor(fULCGraphComponent), fGraphModel);
                nodeEditDialog.setModal(true);
                nodeEditDialog.setVisible(true);
                NodeBean bean = nodeEditDialog.getBeanForm().getModel().getBean();
                bean.reset();
                bean.setComponentType(compDef);
                nodeEditDialog.getBeanForm().getModel().setEditedNode(null);
                nodeEditDialog.setEditedNode(null);
                return true;
            }
            return false;
        }
    }
}
