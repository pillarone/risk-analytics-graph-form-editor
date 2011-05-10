package org.pillarone.riskanalytics.graph.formeditor.ui.view;


import com.canoo.ulc.graph.ULCGraph;
import com.canoo.ulc.graph.ULCGraphComponent;
import com.canoo.ulc.graph.ULCGraphPalette;
import com.canoo.ulc.graph.event.IGraphListener;
import com.canoo.ulc.graph.model.cell.Cell;
import com.canoo.ulc.graph.shared.CellMetaData;
import com.canoo.ulc.graph.shared.PortType;
import com.canoo.ulc.graph.shared.ShapeTemplate;
import com.ulcjava.applicationframework.application.AbstractBean;
import com.ulcjava.applicationframework.application.ApplicationContext;
import com.ulcjava.base.application.*;
import com.ulcjava.base.application.util.Dimension;
import com.ulcjava.base.application.util.Point;
import com.ulcjava.base.application.util.Rectangle;
import org.pillarone.riskanalytics.core.components.Component;
import org.pillarone.riskanalytics.core.packets.Packet;
import org.pillarone.riskanalytics.core.packets.PacketList;
import org.pillarone.riskanalytics.graph.core.graph.model.*;
import org.pillarone.riskanalytics.graph.core.palette.model.ComponentDefinition;
import org.pillarone.riskanalytics.graph.core.palette.service.PaletteService;
import org.pillarone.riskanalytics.graph.formeditor.util.GraphModelUtilities;
import org.pillarone.riskanalytics.graph.formeditor.util.GroovyUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SingleModelVisualView extends AbstractBean implements GraphModelViewable {
    private ApplicationContext fApplicationContext;

    private AbstractGraphModel fGraphModel;
    private boolean fIsModel;

    private ULCGraph ulcGraph;

    private ULCBoxPane fMainView;
    private ULCBoxPane content;

    public SingleModelVisualView(ApplicationContext ctx, AbstractGraphModel model) {
        super();
        fMainView = new ULCBoxPane(1, 1, 2, 2);
        content = new ULCBoxPane(2, 1);
        fMainView.add(ULCBoxPane.BOX_EXPAND_EXPAND, content);
        fApplicationContext = ctx;
        fGraphModel = model;
        fGraphModel.addGraphModelChangeListener(new GraphModelListener());
        initComponents();
        injectGraphModel(model);
    }

    protected void initComponents() {
        ulcGraph = new ULCGraph();
        ulcGraph.upload();
        ulcGraph.addGraphListener(new ULCGraphListener());

        ULCGraphComponent component = new ULCGraphComponent(ulcGraph);
        content.add(ULCBoxPane.BOX_EXPAND_EXPAND, component);
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
        int x = 5;
        int y = 5;
        for (ComponentNode node : model.getAllComponentNodes()) {
            addComponentToULCGraph(node, x, y);
            if (x+110>500) {
                x = 5;
                y = y+110;
                if (y>500) {
                    y = 5;
                }
            } else {
                x = x+110;
            }
        }

        for (Connection c : model.getAllConnections()) {
            String fromId = c.getFrom().getComponentNode().getName();
            String ToId = c.getTo().getComponentNode().getName();
            // TODO
        }
    }

    private void addComponentToULCGraph(ComponentNode node, int x, int y) {
        CellMetaData metaData = new CellMetaData(node.getName(), node.getName());
        int n = fGraphModel.getAllComponentNodes().size();
        Rectangle r = new Rectangle(new Point(x,y), new Dimension(100,100));
        ulcGraph.addVertex(metaData, r, "swimlane");
        // TODO
        // Cell cell = ulcGraph.getCell(node.getName());
        // addPorts(cell, node.getType());
    }

    private void addPorts(Cell cell, ComponentDefinition componentDefinition) {
        Map<Field, Class> fieldClassMap = GroovyUtils.obtainPorts(componentDefinition, "in");
        for (Map.Entry<Field, Class> entry : fieldClassMap.entrySet()) {
            ulcGraph.addPort(cell, PortType.IN, entry.getKey().getName());
        }
        fieldClassMap = GroovyUtils.obtainPorts(componentDefinition, "out");
        for (Map.Entry<Field, Class> entry : fieldClassMap.entrySet()) {
            ulcGraph.addPort(cell, PortType.OUT, entry.getKey().getName());
        }
    }

    private class ULCGraphListener implements IGraphListener {
        public void cellAdded(Cell cell) {
            // System.out.println(cell.getMetaData().getTemplateId());
            ComponentDefinition componentDefinition = PaletteService.getInstance().getComponentDefinition(cell.getMetaData().getTemplateId());
            addPorts(cell, componentDefinition);
            // System.out.println(cell.getStyle());
            cell.getMetaData().setTemplateId(getName());
            fGraphModel.createComponentNode(componentDefinition, cell.getMetaData().getTemplateId());
        }
    }

    private String getName() {
        String defaultName = "newName";
        Integer lastIndex = 0;
        for (ComponentNode node : fGraphModel.getAllComponentNodes()) {
            if (node.getName().startsWith(defaultName)) {
                if (node.getName().endsWith(lastIndex.toString())) {
                    lastIndex++;
                }
            }
        }
        return defaultName+"_"+lastIndex.toString();
    }

    private class GraphModelListener implements IGraphModelChangeListener {

        public void nodeAdded(ComponentNode node) {
            /*String nodeId = node.getName();
            if (ulcGraph.getCell(nodeId) == null) {
                int n = fGraphModel.getAllComponentNodes().size();
                int y = (5 + (n-1)*110)/550;
                int x = (5 + (n-1)*110 - y*550)/110;
                addComponentToULCGraph(node, 5+x*110, 5+y*110);
            }*/
        }

        public void nodeRemoved(ComponentNode node) {
            String nodeId = node.getName();
            if (ulcGraph.getCell(nodeId) != null) {
                // TODO
            }
        }

        public void connectionAdded(Connection c) {
        }

        public void connectionRemoved(Connection c) {
        }
    }
}
