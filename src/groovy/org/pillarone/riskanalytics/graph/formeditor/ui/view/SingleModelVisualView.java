package org.pillarone.riskanalytics.graph.formeditor.ui.view;


import com.canoo.ulc.graph.IGraphSelectionListener;
import com.canoo.ulc.graph.ULCGraph;
import com.canoo.ulc.graph.ULCGraphComponent;
import com.canoo.ulc.graph.ULCGraphOutline;
import com.canoo.ulc.graph.dnd.GraphTransferData;
import com.canoo.ulc.graph.dnd.GraphTransferHandler;
import com.canoo.ulc.graph.event.DuplicateIdException;
import com.canoo.ulc.graph.event.IGraphComponentListener;
import com.canoo.ulc.graph.event.IGraphListener;
import com.canoo.ulc.graph.model.Edge;
import com.canoo.ulc.graph.model.Port;
import com.canoo.ulc.graph.model.Vertex;
import com.canoo.ulc.graph.shared.CollapseState;
import com.canoo.ulc.graph.shared.PortConstraint;
import com.canoo.ulc.graph.shared.PortType;
import com.canoo.ulc.graph.shared.StyleType;
import com.ulcjava.applicationframework.application.AbstractBean;
import com.ulcjava.applicationframework.application.Action;
import com.ulcjava.applicationframework.application.ApplicationActionMap;
import com.ulcjava.applicationframework.application.ApplicationContext;
import com.ulcjava.base.application.*;
import com.ulcjava.base.application.dnd.DataFlavor;
import com.ulcjava.base.application.dnd.Transferable;
import com.ulcjava.base.application.util.Point;
import com.ulcjava.base.application.util.Rectangle;
import org.codehaus.groovy.grails.commons.ApplicationHolder;
import org.jetbrains.annotations.NotNull;
import org.pillarone.riskanalytics.graph.core.graph.model.*;
import org.pillarone.riskanalytics.graph.core.graph.model.filters.IComponentNodeFilter;
import org.pillarone.riskanalytics.graph.core.graph.util.IntegerRange;
import org.pillarone.riskanalytics.graph.core.graph.util.UIUtils;
import org.pillarone.riskanalytics.graph.core.layout.ComponentLayout;
import org.pillarone.riskanalytics.graph.core.layout.GraphLayoutService;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.NameBean;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.NodeBean;
import org.pillarone.riskanalytics.graph.formeditor.util.GraphModelUtilities;
import org.pillarone.riskanalytics.graph.formeditor.util.VisualSceneUtilities;

import java.util.*;

public class SingleModelVisualView extends AbstractBean implements GraphModelViewable, ISelectionListener {
    private ApplicationContext fApplicationContext;

    private AbstractGraphModel fGraphModel;
    Map<String, ComponentNode> fNodesMap;
    Map<Vertex, ComponentNode> fNodesToBeAdded;
    Map<String, Connection> fConnectionsMap;
    List<Connection> fConnectionsToBeAdded;
    IWatchList fWatchList;

    List<ISelectionListener> fSelectionListeners;
    IGraphSelectionListener fGraphSelectionListener;

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
        if (isModel) {
            fULCGraph = new ULCGraph();
        } else {
            fRootVertex = new Vertex("root" + System.currentTimeMillis() + Math.random());
            fRootVertex.setRectangle(new Rectangle(5, 5, 1200, 500));
            try {
                fULCGraph = new ULCGraph(fRootVertex);
            } catch (DuplicateIdException ex) {

            }
        }
        fULCGraph.addGraphListener(new ULCGraphListener());

        fGraphSelectionListener = new IGraphSelectionListener() {
            public void selectionChanged() {
                Set<Vertex> selectedVertices = fULCGraph.getSelectionModel().getSelectedVertices();
                List<ComponentNode> selectedNodes = new ArrayList<ComponentNode>();
                for (Vertex v : selectedVertices) {
                    selectedNodes.add(fNodesMap.get(v.getId()));
                }
                Set<Edge> selectedEdges = fULCGraph.getSelectionModel().getSelectedEdges();
                List<Connection> selectedConnections = new ArrayList<Connection>();
                for (Edge e : selectedEdges) {
                    selectedConnections.add(fConnectionsMap.get(e.getId()));
                }
                for (ISelectionListener listener : fSelectionListeners) {
                    listener.setSelectedComponents(selectedNodes);
                    listener.setSelectedConnections(selectedConnections);
                }
            }
        };
        fULCGraph.getSelectionModel().addGraphSelectionListener(fGraphSelectionListener);

        fULCGraphComponent = new ULCGraphComponent(fULCGraph);
        fULCGraphComponent.addListener(new IGraphComponentListener() {
            public void doubleClickOnElement(com.canoo.ulc.graph.model.GraphElement inElement) {
                if (inElement instanceof Vertex && fNodesMap.containsKey(inElement.getId())) {
                    ComponentNode node = fNodesMap.get(inElement.getId());
                    modifyNodeAction(node);
                }
            }

            public void fetchNestedElements(Vertex vertex) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        // ULCBoxPane content = new ULCBoxPane(2, 1);
        // content.add(ULCBoxPane.BOX_EXPAND_EXPAND, fULCGraphComponent);

        ULCDesktopPane content = new ULCDesktopPane();
        ULCInternalFrame graphFrame = new ULCInternalFrame();
        graphFrame.add(fULCGraphComponent);
        graphFrame.setMaximum(true);
        graphFrame.setWindowDecorationStyle(ULCRootPane.NONE);
        graphFrame.setBorder(BorderFactory.createEmptyBorder());
        graphFrame.putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);
        graphFrame.setVisible(true);

        ULCGraphOutline satelliteView = new ULCGraphOutline(fULCGraphComponent);
        ULCInternalFrame satelliteFrame = new ULCInternalFrame();
        satelliteFrame.add(satelliteView);
        satelliteFrame.setResizable(true);
        satelliteFrame.setBounds(1205, 375, 200, 120);
        satelliteFrame.setWindowDecorationStyle(ULCRootPane.NONE);
        satelliteFrame.putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);
        //satelliteFrame.setBorder(BorderFactory.createEmptyBorder());
        satelliteFrame.setVisible(true);

        content.add(graphFrame);
        content.setLayer(graphFrame, 0);
        content.add(satelliteFrame);
        content.setLayer(satelliteFrame, 1);

        fMainView = new ULCBoxPane(1, 1, 2, 2);
        fMainView.add(ULCBoxPane.BOX_EXPAND_EXPAND, content);

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
        if (visible && fGraphModel != null) {
            updateULCGraph();
        }
    }

    public void injectGraphModel(AbstractGraphModel model) {
        fGraphModel = model;
        fGraphModel.addGraphModelChangeListener(new GraphModelListener());

        // in case we have a composed component (and a root vertex): set title and add existing outer ports
        if (model instanceof ComposedComponentGraphModel && fRootVertex != null) {
            // title
            fRootVertex.setTitle(fGraphModel.getName());

            // outer ports
            ComposedComponentGraphModel ccModel = (ComposedComponentGraphModel) model;
            for (InPort graphModelPort : ccModel.getOuterInPorts()) {
                addOuterPort(graphModelPort);
            }
            for (OutPort graphModelPort : ccModel.getOuterOutPorts()) {
                addOuterPort(graphModelPort);
            }
        }

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

        if (fMainView.isVisible()) {
            updateULCGraph();
        }

        // this can be created only now - since now we know whether we have a Model or a ComposedComponent
        fULCGraphComponent.setComponentPopupMenu(createPopupMenu());

    }

    private void updateULCGraph() {
        int nodesToAdd = fNodesToBeAdded.size();
        int connectionsToAdd = fConnectionsToBeAdded.size();
        if (nodesToAdd == 0 && connectionsToAdd == 0) {
            return;
        }

        Vertex[] vertices = new Vertex[nodesToAdd];
        int i = 0;
        for (Vertex v : fNodesToBeAdded.keySet()) {
            ComponentNode node = fNodesToBeAdded.get(v);
            fNodesMap.put(v.getId(), node);
            vertices[i++] = v;
        }
        try {
            fULCGraph.addVertex(vertices);
            for (Vertex v : vertices) {
                fULCGraphComponent.autoAdjustVertexSize(v);
            }
        } catch (Exception ex) {
            System.out.println("Problem occurred while adding a vertex to ULC graph: \n" + ex.getMessage());
        }
        fNodesToBeAdded = new LinkedHashMap<Vertex, ComponentNode>();

        // construct and add edges associated with the connections registered for inclusion
        // note that the edges can be created only after all the vertices are available.
        Edge[] edges = new Edge[connectionsToAdd];
        i = 0;
        for (Connection c : fConnectionsToBeAdded) {
            Port outPort = getULCPort(c.getFrom());
            Port inPort = getULCPort(c.getTo());
            String id = "conn_" + System.currentTimeMillis() + "_" + Math.random();
            Edge e = new Edge(id, outPort, inPort);
            fConnectionsMap.put(e.getId(), c);
            edges[i++] = e;
        }
        try {
            fULCGraph.addEdge(edges);
        } catch (Exception ex) {
            System.out.println("Problem occurred while adding an edge to ULC graph: \n" + ex.getMessage());
        }
        fConnectionsToBeAdded = new ArrayList<Connection>();

        fULCGraphComponent.layout();
    }

    public void setWatchList(IWatchList watchList) {
        fWatchList = watchList;
    }


    @Action
    public void newNodeAction() {
        NodeEditDialog dialog = new NodeEditDialog(UlcUtilities.getWindowAncestor(fULCGraphComponent), fGraphModel);
        dialog.setModal(true);
        dialog.setVisible(true);
    }

    @Action
    public void modifyNodeAction(ComponentNode node) {
        if (node != null) {
            NodeEditDialog dialog = new NodeEditDialog(UlcUtilities.getWindowAncestor(fULCGraphComponent), fGraphModel);
            dialog.setModal(true);
            dialog.setVisible(true);
            NodeBean bean = dialog.getBeanForm().getModel().getBean();
            bean.setName(node.getName());
            bean.setComponentType(node.getType().getTypeClass().getName());
            bean.setComment(node.getComment());
            if (fGraphModel instanceof ModelGraphModel) {
                bean.setStarter(((ModelGraphModel) fGraphModel).getStartComponents().contains(node));
            }
            dialog.getBeanForm().getModel().setEditedNode(node);
            dialog.setEditedNode(node);
        }
    }

    @Action
    public void clearSelectionAction() {
        clearSelection();
        for (ISelectionListener listener : fSelectionListeners) {
            listener.clearSelection();
        }
    }

    @Action
    public void replicatePortAction() {
        ComposedComponentGraphModel ccGraphModel = (ComposedComponentGraphModel) fGraphModel;
        Set<Port> selectedPorts = fULCGraph.getSelectionModel().getSelectedPorts();
        for (Port port : selectedPorts) {
            org.pillarone.riskanalytics.graph.core.graph.model.Port graphPort = getGraphPort(port);
            if (graphPort == null) {
                ULCAlert alert = new ULCAlert("Port not replicated.",
                        "Port " + port.getTitle() + " cannot be replicated.", "ok");
                alert.show();
                return;
            }
            if (ccGraphModel.isReplicated(graphPort)) {
                ULCAlert alert = new ULCAlert(UlcUtilities.getWindowAncestor(fULCGraphComponent), "Port already replicated.",
                        "Port is already replicated.", "ok");
                alert.show();
            } else {
                PortNameDialog dialog = new PortNameDialog(UlcUtilities.getWindowAncestor(fULCGraphComponent), ccGraphModel, graphPort);
                dialog.setModal(true);
                NameBean bean = dialog.getBeanForm().getModel().getBean();
                bean.setName(graphPort.getPrefix());
                dialog.setVisible(true);
            }
        }
    }

    @Action
    public void removePortAction() {
        ComposedComponentGraphModel ccGraphModel = (ComposedComponentGraphModel) fGraphModel;
        Set<Port> selectedPorts = fULCGraph.getSelectionModel().getSelectedPorts();
        List<org.pillarone.riskanalytics.graph.core.graph.model.Port> selectedOuterPorts = new ArrayList<org.pillarone.riskanalytics.graph.core.graph.model.Port>();
        for (Port port : selectedPorts) {
            org.pillarone.riskanalytics.graph.core.graph.model.Port graphPort = getGraphPort(port);
            if (graphPort != null && graphPort.isComposedComponentOuterPort()) {
                selectedOuterPorts.add(graphPort);
            }
        }
        if (selectedOuterPorts.size() > 0) {
            for (org.pillarone.riskanalytics.graph.core.graph.model.Port p : selectedOuterPorts) {
                ccGraphModel.removeOuterPort(p);
            }
        } else {
            ULCAlert alert = new ULCAlert("Port not removed.", "Inner ports cannot be removed.", "ok");
            alert.show();
        }
    }

    @Action
    public void addSelectedToWatches() {
        Set<Port> selectedPorts = fULCGraph.getSelectionModel().getSelectedPorts();
        for (Port port : selectedPorts) {
            org.pillarone.riskanalytics.graph.core.graph.model.Port graphPort = getGraphPort(port);
            if (graphPort == null) {
                ULCAlert alert = new ULCAlert("No watches added.",
                        "Port " + port.getTitle() + " cannot be identified.", "ok");
                alert.show();
                return;
            }
            String path = GraphModelUtilities.getPath(graphPort, fGraphModel);
            fWatchList.addWatch(path);
        }
    }

    private ULCPopupMenu createPopupMenu() {
        ULCPopupMenu popupMenu = new ULCPopupMenu();
        ApplicationActionMap actionMap = fApplicationContext.getActionMap(this);

        ULCMenuItem addNodeItem = new ULCMenuItem("add node");
        addNodeItem.addActionListener(actionMap.get("addNodeAction"));
        popupMenu.add(addNodeItem);

        if (fGraphModel instanceof ComposedComponentGraphModel) {
            ULCMenuItem replicatePortItem = new ULCMenuItem("replicate port");
            replicatePortItem.addActionListener(actionMap.get("replicatePortAction"));
            popupMenu.add(replicatePortItem);

            ULCMenuItem removeReplicatedPortItem = new ULCMenuItem("remove port");
            removeReplicatedPortItem.addActionListener(actionMap.get("removePortAction"));
            popupMenu.add(removeReplicatedPortItem);
        } else {
            ULCMenuItem addToWatchesItem = new ULCMenuItem("add port to watches");
            addToWatchesItem.addActionListener(actionMap.get("addSelectedToWatches"));
            popupMenu.add(addToWatchesItem);
        }


        popupMenu.addSeparator();

        ULCMenuItem clearSelectionsItem = new ULCMenuItem("clear all selections");
        clearSelectionsItem.addActionListener(actionMap.get("clearSelectionsAction"));
        popupMenu.add(clearSelectionsItem);


        return popupMenu;
    }

    private org.pillarone.riskanalytics.graph.core.graph.model.Port getGraphPort(Port ulcPort) {
        if (ulcPort.getType().equals(PortType.IN) || ulcPort.getType().equals(PortType.OUT)) {
            Vertex vertex = getParentVertex(ulcPort);
            ComponentNode node = fNodesMap.get(vertex.getId());
            if (node == null) {
                System.out.println("No node to given vertex found: " + vertex.getTitle());
                return null;
            }
            String name = (ulcPort.getType().equals(PortType.IN) ? "in" : "out") + ulcPort.getTitle();
            name = name.replaceAll(" ", "");
            return node.getPort(name);
        } else {
            ComposedComponentGraphModel ccModel = (ComposedComponentGraphModel) fGraphModel;
            if (ulcPort.getType().equals(PortType.REPLICATE_IN)) {
                String name = "in" + ulcPort.getTitle();
                name = name.replaceAll(" ", "");
                for (InPort graphPort : ccModel.getOuterInPorts()) {
                    if (graphPort.getName().equalsIgnoreCase(name)) {
                        return graphPort;
                    }
                }
            } else if (ulcPort.getType().equals(PortType.REPLICATE_OUT)) {
                String name = "out" + ulcPort.getTitle();
                for (OutPort graphPort : ccModel.getOuterOutPorts()) {
                    if (graphPort.getName().equalsIgnoreCase(name)) {
                        return graphPort;
                    }
                }
            }
        }
        System.out.println("Port could not be identified: " + ulcPort.getTitle());
        return null;
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
                    cl.setUnfolded(v.getCollapseState() == CollapseState.EXPANDED); // TODO: adjust to allow for three different states
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
                        v.setCollapseState(cl.getUnfolded() ? CollapseState.EXPANDED : CollapseState.COLLAPSED); // TODO: adjust to allow for three different states
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

    /////////////////////////////////////////
    // Implementation of ISelectionListener
    /////////////////////////////////////////

    public void applyFilter(IComponentNodeFilter filter) {
        for (Map.Entry<String, ComponentNode> entry : fNodesMap.entrySet()) {
            Vertex v = fULCGraph.getVertex(entry.getKey());
            if (filter.isSelected(entry.getValue())) {
                v.setStyle(StyleType.fillColor, "yellow");
            } else {
                v.setStyle(StyleType.fillColor, "white");
            }
            fULCGraph.updateElement(v);
            fULCGraphComponent.refresh();
        }
    }

    public void setSelectedComponents(List<ComponentNode> selection) {
        fULCGraph.getSelectionModel().removeGraphSelectionListener(fGraphSelectionListener);
        clearVertexSelection();
        List<Vertex> verticesToSelect = getVertices(selection);
        fULCGraph.getSelectionModel().selectElements(verticesToSelect);
        fULCGraph.getSelectionModel().addGraphSelectionListener(fGraphSelectionListener);
    }

    public void setSelectedConnections(List<Connection> selection) {
        fULCGraph.getSelectionModel().removeGraphSelectionListener(fGraphSelectionListener);
        clearEdgeSelection();
        List<Edge> edgesToSelect = getEdges(selection);
        fULCGraph.getSelectionModel().selectElements(edgesToSelect);
        fULCGraph.getSelectionModel().addGraphSelectionListener(fGraphSelectionListener);
    }

    public void clearSelection() {
        fULCGraph.getSelectionModel().removeGraphSelectionListener(fGraphSelectionListener);
        clearVertexSelection();
        clearEdgeSelection();
        clearPortSelection();
        fULCGraph.getSelectionModel().addGraphSelectionListener(fGraphSelectionListener);
    }


    ////////////////////////////////////
    // Convience methods
    ////////////////////////////////////
    private void clearVertexSelection() {
        List<Vertex> selectedV = new ArrayList<Vertex>();
        selectedV.addAll(fULCGraph.getSelectionModel().getSelectedVertices());
        for (Vertex v : selectedV) {
            fULCGraph.selectionRemoved(v);
        }
    }

    private void clearEdgeSelection() {
        List<Port> selectedP = new ArrayList<Port>();
        selectedP.addAll(fULCGraph.getSelectionModel().getSelectedPorts());
        for (Port p : selectedP) {
            fULCGraph.selectionRemoved(p);
        }
    }

    private void clearPortSelection() {
        List<Edge> selectedE = new ArrayList<Edge>();
        selectedE.addAll(fULCGraph.getSelectionModel().getSelectedEdges());
        for (Edge e : selectedE) {
            fULCGraph.selectionRemoved(e);
        }
    }

    private List<Vertex> getVertices(List<ComponentNode> nodes) {
        List<Vertex> vertices = new ArrayList<Vertex>();
        if (nodes != null) {
            for (Map.Entry<String, ComponentNode> entry : fNodesMap.entrySet()) {
                if (nodes.contains(entry.getValue())) {
                    Vertex v = fULCGraph.getVertex(entry.getKey());
                    vertices.add(v);
                }
            }
            for (Map.Entry<Vertex, ComponentNode> entry : fNodesToBeAdded.entrySet()) {
                if (nodes.contains(entry.getValue())) {
                    vertices.add(entry.getKey());
                }
            }
        }
        return vertices;
    }

    private Vertex getVertex(ComponentNode node) {
        Iterator<Map.Entry<String, ComponentNode>> it1 = fNodesMap.entrySet().iterator();
        while (it1.hasNext()) {
            Map.Entry<String, ComponentNode> entry = it1.next();
            if (node == entry.getValue()) {
                return fULCGraph.getVertex(entry.getKey());
            }
        }
        Iterator<Map.Entry<Vertex, ComponentNode>> it2 = fNodesToBeAdded.entrySet().iterator();
        while (it2.hasNext()) {
            Map.Entry<Vertex, ComponentNode> entry = it2.next();
            if (node == entry.getValue()) {
                return entry.getKey();
            }
        }
        return null;
    }


    private List<Edge> getEdges(List<Connection> connections) {
        List<Edge> edges = new ArrayList<Edge>();
        if (connections != null) {
            for (Map.Entry<String, Connection> entry : fConnectionsMap.entrySet()) {
                if (connections.contains(entry.getValue())) {
                    Edge v = fULCGraph.getEdge(entry.getKey());
                    edges.add(v);
                }
            }
        }
        return edges;
    }

    private void addOuterPort(org.pillarone.riskanalytics.graph.core.graph.model.Port p) {
        String id = "port_" + new Date().getTime() + "_" + Math.random();
        Port port;
        if (p instanceof org.pillarone.riskanalytics.graph.core.graph.model.InPort) {
            port = new Port(id, PortType.REPLICATE_IN, p.getPacketType().getName(), UIUtils.formatDisplayName(p.getName()));
            IntegerRange range = p.getConnectionCardinality();
            port.addConstraint(new PortConstraint(p.getPacketType().getName(), range.getFrom(), range.getTo()));
        } else {
            port = new Port(id, PortType.REPLICATE_OUT, p.getPacketType().getName(), UIUtils.formatDisplayName(p.getName()));
            port.addConstraint(new PortConstraint(p.getPacketType().getName(), 0, Integer.MAX_VALUE));
        }
        fRootVertex.addPort(port);
    }

    private void removeOuterPort(org.pillarone.riskanalytics.graph.core.graph.model.Port p) {
        Port ulcPort = null;
        Iterator<Port> it = fRootVertex.getPorts().iterator();
        while (ulcPort == null && it.hasNext()) {
            Port p0 = it.next();
            String name = (p0.getType().equals(PortType.REPLICATE_IN) ? "in" : "out") + p0.getTitle();
            name = name.replaceAll(" ", "");
            if (p.getName().equalsIgnoreCase(name)) {
                ulcPort = p0;
            }
        }
        if (ulcPort != null) {
            fRootVertex.removePort(ulcPort);
        }
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
                fULCGraphComponent.autoAdjustVertexSize(vertex);
            }
        }

        public void vertexRemoved(@NotNull Vertex vertex) {
            if (fNodesMap.containsKey(vertex.getId())) {
                ComponentNode node = fNodesMap.get(vertex.getId());
                fGraphModel.removeComponentNode(node);
            }
        }

        public void edgeAdded(@NotNull Edge edge) {
            if (edge.getId() == null) {
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
                    fULCGraphComponent.autoAdjustVertexSize(vertex);
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
            addOuterPort(p);
            fULCGraph.updateElement(fRootVertex);
            fULCGraphComponent.layout();
        }

        public void outerPortRemoved(org.pillarone.riskanalytics.graph.core.graph.model.Port p) {
            removeOuterPort(p);
            fULCGraph.updateElement(fRootVertex);
            fULCGraphComponent.layout();
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
            Vertex vertex = getVertex(node);
            if (vertex != null) {
                if (propertyName.equals("name")) {
                    vertex.setTitle(node.getName());
                }
                if (fNodesMap.keySet().contains(vertex.getId())) {
                    fULCGraph.upload();
                }
            }
        }

        public void connectionAdded(Connection c) {
            if (!fMainView.isVisible()) {
                fConnectionsToBeAdded.add(c);
            } else if (c.isReplicatingConnection()) {
                Port outPort = getULCPort(c.getFrom());
                Port inPort = getULCPort(c.getTo());
                String id = "conn_" + System.currentTimeMillis() + "_" + Math.random();
                Edge e = new Edge(id, outPort, inPort);
                fConnectionsMap.put(e.getId(), c);
                try {
                    fULCGraph.addEdge(e);
                } catch (Exception ex) {

                }
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
