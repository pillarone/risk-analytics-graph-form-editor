package org.pillarone.riskanalytics.graph.formeditor.ui.view;


import com.canoo.ulc.graph.IGraphSelectionListener;
import com.canoo.ulc.graph.ULCGraph;
import com.canoo.ulc.graph.ULCGraphComponent;
import com.canoo.ulc.graph.dnd.GraphTransferHandler;
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
import com.ulcjava.base.application.event.ActionEvent;
import com.ulcjava.base.application.event.IActionListener;
import com.ulcjava.base.application.event.IWindowListener;
import com.ulcjava.base.application.event.WindowEvent;
import com.ulcjava.base.application.util.Dimension;
import com.ulcjava.base.application.util.Point;
import com.ulcjava.base.application.util.Rectangle;
import org.codehaus.groovy.grails.commons.ApplicationHolder;
import org.jetbrains.annotations.NotNull;
import org.pillarone.riskanalytics.graph.core.graph.model.*;
import org.pillarone.riskanalytics.graph.core.layout.ComponentLayout;
import org.pillarone.riskanalytics.graph.core.layout.GraphLayoutService;
import org.pillarone.riskanalytics.graph.core.palette.model.ComponentDefinition;
import org.pillarone.riskanalytics.graph.core.palette.service.PaletteService;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.NodeNameFormModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.NameBean;
import org.pillarone.riskanalytics.graph.formeditor.util.GroovyUtils;

import java.lang.reflect.Field;
import java.util.*;

public class SingleModelVisualView extends AbstractBean implements GraphModelViewable {
    private AbstractGraphModel fGraphModel;

    Map<String, ComponentNode> fNodesMap;
    Map<Vertex, ComponentNode> fNodesToBeAdded;
    Map<String, Connection> fConnectionsMap;
    List<Connection> fConnectionsToBeAdded;

    Vertex fCurrentVertex;
    ComponentDefinition fCurrentComponentDefinition;
    Edge fCurrentEdge;
    ShapeTemplate fComposedComponentShape;

    private ULCGraph fULCGraph;
    private ULCGraphComponent fULCGraphComponent;

    private ULCBoxPane fMainView;

    private GraphLayoutService fLayoutService;

    private ApplicationContext fApplicationContext;

    public SingleModelVisualView(ApplicationContext ctx) {
        super();
        fApplicationContext = ctx;
        createView();
    }

    protected void createView() {
        fMainView = new ULCBoxPane(1, 1, 2, 2);
        ULCBoxPane content = new ULCBoxPane(2, 1);
        fMainView.add(ULCBoxPane.BOX_EXPAND_EXPAND, content);
        fULCGraph = new ULCGraph();
        fULCGraph.addGraphListener(new ULCGraphListener());
        fULCGraphComponent = new ULCGraphComponent(fULCGraph);
        fULCGraphComponent.setTransferHandler(new VertexDropHandler(fULCGraphComponent));
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
        updateULCGraph();
    }

    public void injectGraphModel(AbstractGraphModel model) {
        fGraphModel = model;
        fGraphModel.addGraphModelChangeListener(new GraphModelListener());

        fCurrentComponentDefinition = null;
        fCurrentVertex = null;

        if (fGraphModel instanceof ComposedComponentGraphModel) {
            addComposedComponentBoundingBox((ComposedComponentGraphModel) model);
        }

        fNodesMap = new LinkedHashMap<String, ComponentNode>();
        fNodesToBeAdded = new LinkedHashMap<Vertex, ComponentNode>();
        for (ComponentNode node : model.getAllComponentNodes()) {
            Vertex vertex = createVertex(node);
            fNodesToBeAdded.put(vertex, node);
        }

        fConnectionsMap = new LinkedHashMap<String, Connection>();
        fConnectionsToBeAdded = new ArrayList<Connection>();
        for (Connection c : model.getAllConnections()) {
            fConnectionsToBeAdded.add(c);
        }

        // this can be created only now - since now we know whether we have a Model or a ComposedComponent
        fULCGraphComponent.setComponentPopupMenu(createPopupMenu());
    }

    private void updateULCGraph() {
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
                String id = "conn" + c.getFrom().getComponentNode().getName() + "_" + c.getTo().getComponentNode().getName() + "_" + System.currentTimeMillis();
                Edge e = new Edge(id, outPort, inPort);
                fULCGraph.addEdge(e);
                fConnectionsMap.put(e.getId(), c);
            } catch (Exception ex) {
                System.out.println("Problem occurred while adding edge to ULC graph: \n" + ex.getMessage());
            }
        }
        fConnectionsToBeAdded = new ArrayList<Connection>();
        fULCGraph.upload();
        fULCGraphComponent.layout();
    }

    private void addComposedComponentBoundingBox(ComposedComponentGraphModel model) {
        fComposedComponentShape = new ShapeTemplate(ShapeTemplate.ShapeType.Rectangle, model.getPackageName() + "." + model.getName(), model.getName());

        Vertex vertex = new Vertex();
        vertex.setTitle(fComposedComponentShape.getDisplayName());
        vertex.setId(fComposedComponentShape.getName() + "_" + System.currentTimeMillis());
        vertex.setTemplateId(fComposedComponentShape.getName());
        vertex.setStyle("swimlane");

        int width = ClientContext.getScreenWidth() / 2;
        int height = ClientContext.getScreenHeight() / 2;
        Dimension dim = new Dimension(width - 30, height - 30);
        Point point = new Point(15, 15);
        vertex.setRectangle(new Rectangle(point, dim));
        try {
            fULCGraph.addVertex(vertex);
            //fULCGraphComponent.shrinkVertex(vertex);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        fULCGraphComponent.layout();
    }

    private Vertex createVertex(ComponentNode node) {
        Vertex vertex = new Vertex();
        vertex.setTitle(node.getName());
        vertex.setId(node.getName() + "_" + System.currentTimeMillis());
        vertex.setTemplateId(node.getType().getTypeClass().getName());
        vertex.setStyle("swimlane");
        addPorts(vertex, node.getType(), node.getName());
        return vertex;
    }

    private void addPorts(Vertex vertex, ComponentDefinition componentDefinition, String nodeName) {

        Map<Field, Class> fieldClassMap = GroovyUtils.obtainPorts(componentDefinition, "in");
        for (Map.Entry<Field, Class> entry : fieldClassMap.entrySet()) {
            final String portName = entry.getKey().getName();
            final String className = entry.getValue().getName();
            final Port port = new Port(nodeName + "_" + portName + "_" + System.currentTimeMillis(), PortType.IN, className, portName);
            port.addConstraint(new PortConstraint(className, 0, 100));
            vertex.addPort(port);
        }
        fieldClassMap = GroovyUtils.obtainPorts(componentDefinition, "out");
        for (Map.Entry<Field, Class> entry : fieldClassMap.entrySet()) {
            final String portName = entry.getKey().getName();
            final String className = entry.getValue().getName();
            final Port port = new Port(nodeName + "_" + portName + "_" + System.currentTimeMillis(), PortType.OUT, className, portName);
            port.addConstraint(new PortConstraint(className, 0, 100));
            vertex.addPort(port);
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
        return node.getPort(ulcPort.getType()+ulcPort.getTitle());
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
        ComponentNode node = p.getComponentNode();
        Vertex v = null;
        Iterator<Map.Entry<String, ComponentNode>> it = fNodesMap.entrySet().iterator();
        while (v == null && it.hasNext()) {
            Map.Entry<String, ComponentNode> e = it.next();
            if (e.getValue() == node) {
                v = fULCGraph.getVertex(e.getKey());
            }
        }
        if (v != null) {
            for (Port port : v.getPorts()) {
                if (port.getTitle().equals(p.getName())) {
                    return port;
                }
            }
        }
        return null;
    }

    private void saveLayout(long uid, String name) {
        String graphName = fGraphModel.getPackageName() + "." + fGraphModel.getName();
        if (getPersistenceService().findLayout(uid, name, graphName)) {
            //Dialog to verify if overwrite existing save
            return;
        }
        List<ComponentLayout> components = new ArrayList<ComponentLayout>();
        Map<ComponentNode, String> inverse = invertMap(fNodesMap);
        for (Map.Entry<String, List<ComponentNode>> entry : getComponentPaths().entrySet()) {
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

    private GraphLayoutService getPersistenceService() {
        if (fLayoutService == null) {
            org.springframework.context.ApplicationContext ctx = ApplicationHolder.getApplication().getMainContext();
            fLayoutService = ctx.getBean(GraphLayoutService.class);
        }

        return fLayoutService;
    }


    private void loadLayout(long uid, String name) {
        String graphName = fGraphModel.getPackageName() + "." + fGraphModel.getName();
        Map<ComponentNode, String> inverse = invertMap(fNodesMap);
        Map<String, List<ComponentNode>> paths = getComponentPaths();
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

    //invert 1:1 map
    private Map<ComponentNode, String> invertMap(Map<String, ComponentNode> map) {
        HashMap<ComponentNode, String> inverse = new HashMap<ComponentNode, String>();
        for (Map.Entry<String, ComponentNode> entry : map.entrySet()) {
            inverse.put(entry.getValue(), entry.getKey());
        }
        return inverse;
    }


    private HashMap<String, List<ComponentNode>> getComponentPaths() {
        HashMap<String, List<ComponentNode>> paths = new HashMap<String, List<ComponentNode>>();
        for (Map.Entry<String, ComponentNode> entry : fNodesMap.entrySet()) {
            List<ComponentNode> tmpList;
            if ((tmpList = paths.get(entry.getValue().getName())) == null) {
                tmpList = new ArrayList<ComponentNode>();
                paths.put(entry.getValue().getName(), tmpList);
            }
            tmpList.add(entry.getValue());
        }
        return paths;
    }

    /**
     * Reacts to changes in the ULC graph - updates the graph model
     */
    private class ULCGraphListener implements IGraphListener {

        public void vertexAdded(@NotNull Vertex vertex) {
            if (vertex.getId() == null) {
                vertex.setId("noname_" + System.currentTimeMillis() + Math.random());
            }
            fULCGraphComponent.shrinkVertex(vertex);
        }

        public void vertexRemoved(@NotNull Vertex vertex) {
            if (fNodesMap.containsKey(vertex.getId())) {
                ComponentNode node = fNodesMap.get(vertex.getId());
                fGraphModel.removeComponentNode(node);
            }
        }

        public void edgeAdded(@NotNull Edge edge) {
            fCurrentEdge = edge;
            org.pillarone.riskanalytics.graph.core.graph.model.Port outPort = getGraphPort((Port) edge.getSource());
            org.pillarone.riskanalytics.graph.core.graph.model.Port inPort = getGraphPort((Port) edge.getTarget());
            if (edge.getId() == null) {
                edge.setId("conn_" + outPort.getComponentNode().getName() + "_" + inPort.getComponentNode().getName() + "_" + System.currentTimeMillis());
            }
            if (outPort != null && inPort != null) {
                fGraphModel.createConnection(outPort, inPort);
            } else {
                fCurrentEdge = null;
                System.out.println("Ports could not be properly identified - no connection added to the graph model.");
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
            if (fCurrentVertex != null) {
                fCurrentVertex.setId(node.getName() + "_" + System.currentTimeMillis());
                fCurrentVertex.setTitle(node.getName());
                fNodesMap.put(fCurrentVertex.getId(), node);
                try {
                    fULCGraph.addVertex(fCurrentVertex);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                fCurrentVertex = null;
            } else {
                Vertex vertex = createVertex(node);
                fNodesToBeAdded.put(vertex, node);
                fCurrentVertex = null;
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
            if (p instanceof org.pillarone.riskanalytics.graph.core.graph.model.InPort) {
                PortTemplate portTemplate = new PortTemplate(PortType.IN, p.getPacketType().getClass().getName(), p.getName(), Arrays.asList(new PortConstraint(p.getPacketType().getClass().getName(), 0, 100)));
                fComposedComponentShape.addPortTemplate(portTemplate);
            } else {
                PortTemplate portTemplate = new PortTemplate(PortType.OUT, p.getPacketType().getClass().getName(), p.getName(), Arrays.asList(new PortConstraint(p.getPacketType().getClass().getName(), 0, 100)));
                fComposedComponentShape.addPortTemplate(portTemplate);
            }
        }

        public void outerPortRemoved(org.pillarone.riskanalytics.graph.core.graph.model.Port p) {
            //nothing to do
        }

        public void nodesSelected(List<ComponentNode> nodes) {
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
            if (fCurrentEdge != null) {
                fConnectionsMap.put(fCurrentEdge.getId(), c);
                fCurrentEdge = null;
            } else {
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

    private class NodeNameDialog extends ULCDialog {
        private BeanFormDialog<NodeNameFormModel> fBeanForm;
        private final AbstractGraphModel fGraphModel;

        NodeNameDialog(ULCWindow parent, AbstractGraphModel model) {
            super(parent);
            boolean metalLookAndFeel = "Metal".equals(ClientContext.getLookAndFeelName());
            if (!metalLookAndFeel && ClientContext.getLookAndFeelSupportsWindowDecorations()) {
                setUndecorated(true);
                setWindowDecorationStyle(ULCDialog.PLAIN_DIALOG);
            }
            fGraphModel = model;
            createBeanView();
            setTitle("Component Node Name");
            setLocationRelativeTo(parent);
        }

        @SuppressWarnings("serial")
        private void createBeanView() {
            NodeNameFormModel model = new NodeNameFormModel(new NameBean(), fGraphModel);
            NodeNameForm form = new NodeNameForm(model);
            fBeanForm = new BeanFormDialog<NodeNameFormModel>(form);
            add(fBeanForm.getContentPane());
            fBeanForm.addSaveActionListener(new IActionListener() {
                public void actionPerformed(ActionEvent event) {
                    NameBean bean = fBeanForm.getModel().getBean();
                    String nodeName = bean.getName();
                    setVisible(false);
                    fBeanForm.reset();
                    if (fCurrentComponentDefinition != null) {
                        fGraphModel.createComponentNode(fCurrentComponentDefinition, nodeName);
                    }
                }
            });

            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            addWindowListener(new IWindowListener() {
                public void windowClosing(WindowEvent event) {
                    fBeanForm.interceptIfDirty(new Runnable() {
                        public void run() {
                            setVisible(false);
                        }
                    });
                }
            });
            pack();
        }

        public BeanFormDialog<NodeNameFormModel> getBeanForm() {
            return fBeanForm;
        }
    }

    private class VertexDropHandler extends GraphTransferHandler {

        private VertexDropHandler(ULCGraphComponent inGraphComponent) {
            super(inGraphComponent);
        }

        @Override
        public boolean importData(final ULCComponent inTargetComponent, final Transferable inTransferable) {
            final Object data = inTransferable.getTransferData(DataFlavor.DROP_FLAVOR);
            if (data instanceof Vertex) {
                final Vertex vertex = (Vertex) data;
                fCurrentComponentDefinition = PaletteService.getInstance().getComponentDefinition(vertex.getTemplateId());
                NodeNameDialog nodeNameDialog = new NodeNameDialog(UlcUtilities.getWindowAncestor(fULCGraphComponent), fGraphModel);
                nodeNameDialog.setModal(true);
                nodeNameDialog.setVisible(true);
                fCurrentVertex = vertex;
                return true;
            }
            return false;
        }
    }
}
