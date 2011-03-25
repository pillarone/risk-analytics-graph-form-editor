package org.pillarone.riskanalytics.graph.formeditor.util;

import org.pillarone.riskanalytics.core.model.registry.ModelRegistry;
import org.pillarone.riskanalytics.graph.core.graph.model.*;
import org.pillarone.riskanalytics.graph.core.graphexport.AbstractGraphExport;
import org.pillarone.riskanalytics.graph.core.graphexport.ComposedComponentGraphExport;
import org.pillarone.riskanalytics.graph.core.graphexport.GraphExportService;
import org.pillarone.riskanalytics.graph.core.graphexport.ModelGraphExport;
import org.pillarone.riskanalytics.graph.core.palette.model.ComponentDefinition;
import org.pillarone.riskanalytics.graph.core.palette.service.PaletteService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GraphModelUtilities {

    /**
     * Find a component node with given name in graph model. Return null if not found.
     *
     * @param model model to search the component node in.
     * @param name  name to search for.
     * @return
     */
    public static ComponentNode findComponentNode(AbstractGraphModel model, String name) {
        for (ComponentNode node : model.getAllComponentNodes()) {
            if (node.getName().equals(name)) {
                return node;
            }
        }
        return null;
    }

    /**
     * Replace the given component node in the given model by a new component node with given name and type.
     * This method can be used for renaming a component node or changing its type.
     * The given component node is then removed from the graph by delegating to
     * {@link AbstractGraphModel#removeComponentNode(ComponentNode)}
     * so that all the associated connections are also removed.
     * Then, a new component is added with given type and name.
     * Finally, connections are re-established as far as possible.
     *
     * @param node
     * @param newName
     * @param newTypeClassName
     * @param model
     * @return
     */
    public static ComponentNode replaceComponentNode(ComponentNode node, String newName, String newTypeClassName, AbstractGraphModel model) {
        if (!model.getAllComponentNodes().contains(node)) return null;

        boolean hasSameType = node.getType().getTypeClass().getName().equals(newTypeClassName);
        List<Connection> connections = GraphModelUtilities.getConnections(node, model);
        model.removeComponentNode(node);
        ComponentDefinition componentDefinition = hasSameType
                ? node.getType()
                : PaletteService.getInstance().getComponentDefinition(newTypeClassName);
        ComponentNode newNode = model.createComponentNode(componentDefinition, newName);
        // re-add all related connections so that all listeners are notified
        for (Connection c : connections) {
            boolean nodeHasFromPort = c.getFrom().getComponentNode() == node;
            String newPortNameThisNode = nodeHasFromPort ? c.getFrom().getName() : c.getTo().getName();
            Port portThisNode = newNode.getPort(newPortNameThisNode);
            Port portOtherNode = nodeHasFromPort ? c.getTo() : c.getFrom();
            if (portThisNode != null) {
                if (nodeHasFromPort) {
                    model.createConnection(portThisNode, portOtherNode);
                } else {
                    model.createConnection(portOtherNode, portThisNode);
                }
            }
        }
        return newNode;
    }


    public static boolean hasPorts(ComponentNode node) {
        return (node.getInPorts() != null && node.getInPorts().size() > 0)
                || (node.getOutPorts() != null && node.getOutPorts().size() > 0);
    }

    /**
     * Return port name for consisting of the form <component name>.<port name> for inner ports of
     * just the port name for replicating ports.
     *
     * @param p
     * @return
     */
    public static String getPortName(Port p) {
        if (p == null) {
            return null;
        }
        String portName = p.toString();
        if (p.getComponentNode() != null) {
            String compName = p.getComponentNode().getName();
            return compName + "." + portName;
        } else {
            return portName;
        }
    }

    /**
     * Search for a port within a model by its name of the form <component name>.<port name>.
     * For composed components outer ports just the port name is sufficient.
     *
     * @param name
     * @param model
     * @return
     */
    public static Port getPortFromName(String name, AbstractGraphModel model) {
        String trimmedName = name.split("\\s")[0].trim();
        String[] nameParts = trimmedName.split("\\.");
        if (nameParts.length == 2) {
            String portName = nameParts[nameParts.length - 1];
            String nodeName = trimmedName.substring(0, trimmedName.length() - portName.length() - 1);
            ComponentNode node = findComponentNode(model, nodeName);
            if (node != null) {
                Port p = node.getPort(portName);
                if (p != null) {
                    return p;
                }
            }
        } else if (nameParts.length == 1) {
            if (model instanceof ComposedComponentGraphModel) {
                for (Port p : ((ComposedComponentGraphModel) model).getOuterOutPorts()) {
                    if (p.getName().equals(name)) {
                        return p;
                    }
                }
                for (Port p : ((ComposedComponentGraphModel) model).getOuterInPorts()) {
                    if (p.getName().equals(name)) {
                        return p;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Check whether there are connections to or from the given component node in the given model.
     *
     * @param node
     * @param model
     * @return
     */
    public static boolean isConnected(ComponentNode node, AbstractGraphModel model) {
        for (Port p : node.getInPorts()) {
            for (Connection c : model.getAllConnections()) {
                if (c.getTo() == p) return true;
            }
        }
        for (Port p : node.getOutPorts()) {
            for (Connection c : model.getAllConnections()) {
                if (c.getFrom() == p) return true;
            }
        }
        return false;
    }

    /**
     * Return all connections from or to the given component node in the given model.
     *
     * @param node
     * @param model
     * @return
     */
    public static List<Connection> getConnections(ComponentNode node, AbstractGraphModel model) {
        List<Connection> connections = new ArrayList<Connection>();
        for (Port p : node.getInPorts()) {
            for (Connection c : model.getAllConnections()) {
                if (c.getTo() == p) {
                    connections.add(c);
                }
            }
        }
        for (Port p : node.getOutPorts()) {
            for (Connection c : model.getAllConnections()) {
                if (c.getFrom() == p) {
                    connections.add(c);
                }
            }
        }
        return connections;
    }

    /**
     * Create the groovy code to the given graph model.
     *
     * @param model
     * @return
     */
    public static String getGroovyModelCode(AbstractGraphModel model) {
        AbstractGraphExport exporter;
        if (model instanceof ModelGraphModel) {
            exporter = new ModelGraphExport();
        } else {
            exporter = new ComposedComponentGraphExport();
        }
        String modelText = exporter.exportGraph(model);
        return modelText;
    }

    public static void exportToApplication(ModelGraphModel model) {
        GraphExportService graphExportService = new GraphExportService();
        Map<String, byte[]> stringMap = graphExportService.exportGraphToBinary(model);
        String name = null;
        byte[] data = null;
        for (Map.Entry<String, byte[]> entry : stringMap.entrySet()) {
            name = entry.getKey();
            data = entry.getValue();
        }
        Class clazz = GroovyUtils.persistClass(data, name);
        ModelRegistry.getInstance().addModel(clazz);
    }

    /**
     * Create another textual representation of the given graph model.
     *
     * @param model
     * @return
     */
    public static String toText(AbstractGraphModel model) {
        boolean isModel = model instanceof ModelGraphModel;
        StringBuffer text = new StringBuffer();
        // graph model type
        text.append("GraphModel is of type "
                + (isModel ? ModelGraphModel.class.getName()
                : ComposedComponentGraphModel.class.getName()) + ". \n");

        // nodes
        text.append("Component nodes: \n");
        for (ComponentNode node : model.getAllComponentNodes()) {
            text.append("	" + node.getName());
            if (isModel && ((ModelGraphModel) model).getStartComponents().contains(node)) {
                text.append("\t STARTER");
            }
            text.append("\n");
        }


        // outer ports
        if (!isModel) {
            text.append("Outer ports: \n");
            for (Port p : ((ComposedComponentGraphModel) model).getOuterInPorts()) {
                text.append("\t IN " + p.getName() + "\n");
            }
            for (Port p : ((ComposedComponentGraphModel) model).getOuterOutPorts()) {
                text.append("\t OUT " + p.getName() + "\n");
            }
        }

        // connections
        text.append("Connections: \n");
        for (Connection c : model.getAllConnections()) {
            text.append("\t FROM " + c.getFrom().getName()
                    + " TO " + c.getTo().getName());
            if (c.getFrom().isComposedComponentOuterPort() || c.getTo().isComposedComponentOuterPort()) {
                text.append("\t IS REPLICA \n");
            } else {
                text.append("\n");
            }
        }

        return text.toString();
    }
}
