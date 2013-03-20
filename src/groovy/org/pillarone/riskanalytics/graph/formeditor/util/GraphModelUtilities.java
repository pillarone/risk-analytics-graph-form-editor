package org.pillarone.riskanalytics.graph.formeditor.util;

import org.pillarone.riskanalytics.core.model.registry.ModelRegistry;
import org.pillarone.riskanalytics.graph.core.graph.model.*;
import org.pillarone.riskanalytics.graph.core.graphexport.AbstractGraphExport;
import org.pillarone.riskanalytics.graph.core.graphexport.ComposedComponentGraphExport;
import org.pillarone.riskanalytics.graph.core.graphexport.GraphExportService;
import org.pillarone.riskanalytics.graph.core.graphexport.ModelGraphExport;
import org.pillarone.riskanalytics.graph.core.loader.ClassType;
import org.pillarone.riskanalytics.graph.core.palette.model.ComponentDefinition;
import org.pillarone.riskanalytics.graph.core.palette.service.PaletteService;

import java.util.*;

public class GraphModelUtilities {

    public static final String PATHSEP = ":";
    public static final String PATHSEP_RESOLVE = "\\:";

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
        List<Connection> connections = model.getEmergingConnections(node);
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
        if (model instanceof ModelGraphModel) {
            ModelGraphModel m = (ModelGraphModel) model;
            if (m.getStartComponents().contains(node)) {
                m.getStartComponents().remove(node);
                m.getStartComponents().add(newNode);
            }
        }
        return newNode;
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
        String portName = p.getDisplayName();
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
            ComponentNode node = model.findNodeByName(nodeName);
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

    public static boolean isIncludedInRegistry(AbstractGraphModel model) {
        if (model instanceof ModelGraphModel) {
            for (Class c : ModelRegistry.getInstance().getAllModelClasses()) {
                if (c.getName().equals(model.getPackageName() + "." + model.getName())) {
                    return true;
                }
            }
        } else if (model instanceof ComposedComponentGraphModel) {
            for (ComponentDefinition cd : PaletteService.getInstance().getAllComponentDefinitions()) {
                if (cd.getTypeClass().getName().equals(model.getPackageName() + "." + model.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String getModelPath(String packageName, String name) {
        return packageName+ "." + name;
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

    public static void exportToApplication(AbstractGraphModel model) {
        GraphExportService graphExportService = new GraphExportService();
        Map<String, byte[]> stringMap = graphExportService.exportGraphToBinary(model);

        String className = model.getPackageName() + "." + model.getName();
        Class modelClass = null;

        for (Map.Entry<String, byte[]> entry : stringMap.entrySet()) {
            String name = entry.getKey();
            byte[] data = entry.getValue();

            if (entry.getKey().equals(className)) {
                modelClass = GroovyUtils.persistClass((model instanceof ModelGraphModel ? ClassType.MODEL : ClassType.COMPONENT), data, name);
            } else {
                GroovyUtils.persistClass(ClassType.DEPENDENCY, data, name);
            }
        }
        if (model instanceof ModelGraphModel) {
            ModelRegistry.getInstance().addModel(modelClass);
        } else {
            final ComponentDefinition componentDefinition = new ComponentDefinition();
            componentDefinition.setTypeClass(modelClass);
            PaletteService.getInstance().addComponentDefinition(componentDefinition);
        }
    }

    public static <S, T> Map<S, T> invertMap(Map<T, S> map) {
        HashMap<S, T> inverse = new HashMap<S, T>();
        for (Map.Entry<T, S> entry : map.entrySet()) {
            inverse.put(entry.getValue(), entry.getKey());
        }
        return inverse;
    }

    public static HashMap<String, List<ComponentNode>> getComponentPaths(Map<String, ComponentNode> nodesMap) {
        HashMap<String, List<ComponentNode>> paths = new HashMap<String, List<ComponentNode>>();
        for (Map.Entry<String, ComponentNode> entry : nodesMap.entrySet()) {
            List<ComponentNode> tmpList;
            if ((tmpList = paths.get(entry.getValue().getName())) == null) {
                tmpList = new ArrayList<ComponentNode>();
                paths.put(entry.getValue().getName(), tmpList);
            }
            tmpList.add(entry.getValue());
        }
        return paths;
    }

    public static String getPath(GraphElement element, final AbstractGraphModel model) {
        List<GraphElement> modelTreePath = getModelTreePath(element, model);
        if (modelTreePath.size()>0) {
            String path = modelTreePath.get(0).getName();
            for (int i = 1; i < modelTreePath.size(); i++) {
                path = path + PATHSEP + modelTreePath.get(i).getName();
            }
            return path;
        }
        return null;
    }

    public static String[] getPathElements(String path) {
        String[] pathElements = path.split(PATHSEP_RESOLVE);
        for (int i = 0; i < pathElements.length; i++) {
            pathElements[i] = pathElements[i].trim();
        }
        return pathElements;
    }

    public static List<GraphElement> getModelTreePath(GraphElement element, final AbstractGraphModel model) {
        List<GraphElement> modelTreePath = new LinkedList<GraphElement>();
        Iterator<ComponentNode> it = model.getAllComponentNodes().iterator();
        while (modelTreePath.size()==0 && it.hasNext()) {
            List<GraphElement> subTree = getModelTreePath(element, it.next());
            if (subTree.size()>0) {
                modelTreePath = subTree;
            }
        }
        return modelTreePath;
    }

    public static List<GraphElement> getModelTreePath(GraphElement element, final GraphElement context) {
        List<GraphElement> modelTreePath = new LinkedList<GraphElement>();
        if (element.equals(context)) {
            modelTreePath.add(element);
            return modelTreePath;
        } else if (context instanceof ComponentNode) {
            ComponentNode c = (ComponentNode) context;
            if (context instanceof ComposedComponentNode) {
                ComposedComponentNode cc = (ComposedComponentNode)c;
                for (ComponentNode node : cc.getComponentGraph().getAllComponentNodes()) {
                    List<GraphElement> subPath = getModelTreePath(element, node);
                    if (subPath.size()>0) {
                        modelTreePath.add(c);
                        modelTreePath.addAll(subPath);
                        return modelTreePath;
                    }
                }
            }
            for (Port p : ((ComponentNode)context).getInPorts()) {
                if (p.equals(element)) {
                    modelTreePath.add(c);
                    modelTreePath.add(element);
                    return modelTreePath;
                }
            }
            for (Port p : ((ComponentNode)context).getOutPorts()) {
                if (p.equals(element)) {
                    modelTreePath.add(c);
                    modelTreePath.add(element);
                    return modelTreePath;
                }
            }
        }
        return modelTreePath;
    }

    /**
     * Create another textual representation of the given graph model.
     *
     * @param model
     * @return
     */
    /*public static String toText(AbstractGraphModel model) {
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
    }*/
}
