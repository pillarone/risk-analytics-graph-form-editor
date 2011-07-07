package org.pillarone.riskanalytics.graph.formeditor.ui.view;


import com.canoo.ulc.graph.ULCGraph;
import com.canoo.ulc.graph.ULCGraphComponent;
import com.canoo.ulc.graph.dnd.GraphTransferHandler;
import com.canoo.ulc.graph.event.IGraphListener;
import com.canoo.ulc.graph.model.Edge;
import com.canoo.ulc.graph.model.Port;
import com.canoo.ulc.graph.model.Vertex;
import com.canoo.ulc.graph.shared.PortConstraint;
import com.canoo.ulc.graph.shared.PortType;
import com.ulcjava.applicationframework.application.AbstractBean;
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
import org.jetbrains.annotations.NotNull;
import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.model.ComponentNode;
import org.pillarone.riskanalytics.graph.core.graph.model.Connection;
import org.pillarone.riskanalytics.graph.core.graph.model.IGraphModelChangeListener;
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
    Vertex fCurrentVertex;
    ComponentDefinition fCurrentComponentDefinition;
    Map<String, Connection> fConnectionsMap;
    List<Connection> fConnectionsToBeAdded;
    Edge fCurrentEdge;

    private ULCGraph fULCGraph;
    private ULCGraphComponent fULCGraphComponent;
    private LayOuter fLayOuter;

    private ULCBoxPane fMainView;
    private ULCBoxPane fContent;

    public SingleModelVisualView(ApplicationContext ctx, AbstractGraphModel model) {
        super();
        fMainView = new ULCBoxPane(1, 1, 2, 2);
        fContent = new ULCBoxPane(2, 1);
        fMainView.add(ULCBoxPane.BOX_EXPAND_EXPAND, fContent);
        fGraphModel = model;
        fGraphModel.addGraphModelChangeListener(new GraphModelListener());
        initComponents();

        injectGraphModel(model);
    }

    protected void initComponents() {
        fULCGraph = new ULCGraph();
        fULCGraph.addGraphListener(new ULCGraphListener());
        fULCGraphComponent = new ULCGraphComponent(fULCGraph);
        fULCGraphComponent.setTransferHandler(new VertexDropHandler(fULCGraphComponent));
        fContent.add(ULCBoxPane.BOX_EXPAND_EXPAND, fULCGraphComponent);
        fNodesMap = new LinkedHashMap<String, ComponentNode>();
        fNodesToBeAdded = new LinkedHashMap<Vertex, ComponentNode>();
        fConnectionsMap = new LinkedHashMap<String, Connection>();
        fConnectionsToBeAdded = new ArrayList<Connection>();
        fCurrentComponentDefinition = null;
        fCurrentVertex = null;
        fLayOuter = new LayOuter();
    }

    public ULCBoxPane getView() {
        return fMainView;
    }

    public void setVisible(boolean visible) {
        if (fMainView != null) {
            fMainView.setVisible(visible);
            updateULCGraph();
        }
    }

    public void injectGraphModel(AbstractGraphModel model) {
        for (ComponentNode node : model.getAllComponentNodes()) {
            Vertex vertex = createVertex(node);
            fNodesToBeAdded.put(vertex, node);
            fCurrentVertex = null;
        }
        for (Connection c : model.getAllConnections()) {
            fConnectionsToBeAdded.add(c);
        }
    }

    private void updateULCGraph() {
        List<Vertex> vertices = new ArrayList<Vertex>();
        vertices.addAll(fNodesToBeAdded.keySet());
        fLayOuter.layout(vertices);
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
                String id = c.getFrom().getComponentNode().getName() + "." + c.getFrom().getName()
                        + "_" + c.getTo().getComponentNode().getName() + "." + c.getTo().getName();
                Edge e = new Edge(id, outPort, inPort);
                fULCGraph.addEdge(e);
                fConnectionsMap.put(e.getId(), c);
            } catch (Exception ex) {
                System.out.println("Problem occurred while adding edge to ULC graph: \n" + ex.getMessage());
            }
        }
        fConnectionsToBeAdded = new ArrayList<Connection>();

        fULCGraphComponent.layout();
    }

    private Vertex createVertex(ComponentNode node) {
        Vertex vertex = new Vertex(node.getName());
        vertex.setTitle(node.getName());
        vertex.setId(node.getName());
        vertex.setTemplateId(node.getType().getTypeClass().getName());
        vertex.setStyle("swimlane");
        addPorts(vertex, PaletteService.getInstance().getComponentDefinition(vertex.getTemplateId()), node.getName());
        return vertex;
    }

    private void addPorts(Vertex vertex, ComponentDefinition componentDefinition, String nodeName) {

        Map<Field, Class> fieldClassMap = GroovyUtils.obtainPorts(componentDefinition, "in");
        for (Map.Entry<Field, Class> entry : fieldClassMap.entrySet()) {
            final String portName = entry.getKey().getName();
            final String className = entry.getValue().getName();
            final Port port = new Port(nodeName + "_" + portName, PortType.IN, className, portName);
            port.addConstraint(new PortConstraint(className, 0, 100));
            vertex.addPort(port);
        }
        fieldClassMap = GroovyUtils.obtainPorts(componentDefinition, "out");
        for (Map.Entry<Field, Class> entry : fieldClassMap.entrySet()) {
            final String portName = entry.getKey().getName();
            final String className = entry.getValue().getName();
            final Port port = new Port(nodeName + "_" + portName, PortType.OUT, className, portName);
            port.addConstraint(new PortConstraint(className, 0, 100));
            vertex.addPort(port);
        }
    }

    private org.pillarone.riskanalytics.graph.core.graph.model.Port getGraphPort(Port ulcPort) {
        Vertex vertex = getParentVertex(ulcPort);
        if (vertex == null) {
            System.out.println("Parent vertex not found for port " + ulcPort.getId());
            return null;
        }
        ComponentNode node = fNodesMap.get(vertex.getId());
        if (node == null) {
            System.out.println("Node to given vertex found: " + vertex.getId());
            return null;
        }
        return node.getPort(ulcPort.getTitle());
    }

    private Vertex getParentVertex(Port p) {
        for (String vertexId : fNodesMap.keySet()) {
            Vertex v = fULCGraph.getVertex(vertexId);
            if (v.getPorts().contains(p)) {
                return v;
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


    /**
     * Reacts to changes in the ULC graph - updates the graph model
     */
    private class ULCGraphListener implements IGraphListener {

        public void vertexAdded(@NotNull Vertex vertex) {
            if (vertex.getId() == null) {
                vertex.setId("" + System.currentTimeMillis()+Math.random());
            }
            fULCGraphComponent.shrinkVertex(vertex);
        }

        public void vertexRemoved(@NotNull Vertex vertex) {
            String id = vertex.getId();
            if (fNodesMap.containsKey(id)) {
                // fULCGraph.removeVertex(fULCGraph.getVertex(id));
                ComponentNode node = fNodesMap.get(id);
                fNodesMap.remove(id);
                fGraphModel.removeComponentNode(node);
            }
        }

        public void edgeAdded(@NotNull Edge edge) {
            fCurrentEdge = edge;
            edge.setId("conn_" + fGraphModel.getAllConnections().size());
            org.pillarone.riskanalytics.graph.core.graph.model.Port outPort = getGraphPort((Port) edge.getSource());
            org.pillarone.riskanalytics.graph.core.graph.model.Port inPort = getGraphPort((Port) edge.getTarget());
            if (outPort != null && inPort != null) {
                fGraphModel.createConnection(outPort, inPort);
            } else {
                System.out.println("Ports could not be properly identified - no connection added to the graph model.");
            }
        }

        public void edgeRemoved(@NotNull Edge edge) {
            String id = edge.getId();
            if (fConnectionsMap.containsKey(id)) {
                fULCGraph.removeEdge(fULCGraph.getEdge(id));
                Connection conn = fConnectionsMap.get(id);
                fConnectionsMap.remove(id);
                fGraphModel.removeConnection(conn);
            }
        }

    }

    private class GraphModelListener implements IGraphModelChangeListener {

        public void nodeAdded(ComponentNode node) {
            if (fCurrentVertex != null) {
                fNodesMap.put(fCurrentVertex.getId(), node);
                fCurrentVertex.setTitle(node.getName());
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
            }
            fULCGraph.upload();
        }

        public void nodesSelected(List<ComponentNode> nodes) {

        }

        public void connectionsSelected(List<Connection> connections) {

        }

        public void selectionCleared() {

        }

        public void filtersApplied() {
            // TODO - once the selection possibilities are available
        }

        public void nodePropertyChanged(ComponentNode node, String propertyName, Object oldValue, Object newValue) {
            if (!fNodesToBeAdded.values().contains(node)) {
                String oldName = null;
                Iterator<Map.Entry<String, ComponentNode>> it = fNodesMap.entrySet().iterator();
                while (it.hasNext() && oldName == null) {
                    Map.Entry<String, ComponentNode> e = it.next();
                    if (e.getValue() == node) {
                        oldName = e.getKey();
                        fNodesMap.remove(oldName);
                        fNodesMap.put(node.getName(), node);
                    }
                }
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
            }
            fULCGraph.upload();
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

    static final Dimension DIM = new Dimension(150, 100);
    static final int XOFFSET = 20;
    static final int YOFFSET = 20;

    private class VertexDropHandler extends GraphTransferHandler {

        private VertexDropHandler(ULCGraphComponent inGraphComponent) {
            super(inGraphComponent);
        }

        @Override
        public boolean importData(final ULCComponent inTargetComponent, final Transferable inTransferable) {

            final Object data = inTransferable.getTransferData(DataFlavor.DROP_FLAVOR);
            if (data instanceof Vertex) {
                final Vertex transferData = (Vertex) data;
                transferData.setId("internal" + new Date().getTime());

                fCurrentComponentDefinition = PaletteService.getInstance().getComponentDefinition(transferData.getTemplateId());
                NodeNameDialog nodeNameDialog = new NodeNameDialog(UlcUtilities.getWindowAncestor(fContent), fGraphModel);
                nodeNameDialog.setModal(true);
                nodeNameDialog.setVisible(true);
                fCurrentVertex = transferData;


                return true;
            }
            return false;
        }
    }

    private class LayOuter {

        Map<Vertex, Point> vertexPositions;
        int xDelta, yDelta;
        Dimension sceneDimension;

        LayOuter() {
            sceneDimension = new Dimension(800, 500);
            vertexPositions = new LinkedHashMap<Vertex, Point>();
        }

        /**
         * For the vertices already in the scene find suitable point where to place the vertices to be added.
         *
         * @param verticesToAdd
         */
        public void layout(List<Vertex> verticesToAdd) {
            if (verticesToAdd == null || verticesToAdd.size() == 0) {
                return;
            }
            int n = verticesToAdd.size();
            int row = -1;
            int col = -1;
            int rowSize = (int) Math.ceil(Math.sqrt(n));
            xDelta = (int) (sceneDimension.getWidth() - XOFFSET - DIM.getWidth()) / Math.max(rowSize - 1, 1);
            int rows = n / rowSize;
            yDelta = (int) (sceneDimension.getHeight() - YOFFSET - DIM.getHeight()) / Math.max(rows - 1, 1);
            for (Vertex v : verticesToAdd) {
                col++;
                if (col % rowSize == 0) {
                    row++;
                    col = 0;
                }
                int x = XOFFSET + col * xDelta;
                int y = YOFFSET + row * yDelta;
                v.setRectangle(new Rectangle(new Point(x, y), DIM));
                vertexPositions.put(v, v.getRectangle().getLocation());
            }
        }

        public void remove(List<Vertex> verticesToRemove) {
            for (Vertex v : verticesToRemove) {
                remove(v);
            }
        }

        public void remove(Vertex vertexToRemove) {
            vertexPositions.remove(vertexToRemove);
        }
    }
}
