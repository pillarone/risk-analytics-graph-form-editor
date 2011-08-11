package org.pillarone.riskanalytics.graph.formeditor.ui.model.treetable

import org.pillarone.riskanalytics.graph.core.graphimport.ComposedComponentGraphImport
import org.pillarone.riskanalytics.graph.core.graph.model.*

class GraphElementTreeBuilder {

    GraphElementNode root
    Map elementNodes = [:]

    def GraphElementTreeBuilder() {
    }

    public GraphElementTreeBuilder(AbstractGraphModel model) {
        root = buildNode(model)
    }

    /**
     * Search for the child elements in the given graph element
     * @param parent
     * @return
     */
    protected List<GraphElement> searchForChildren(GraphElement parent) {
        if (parent instanceof ComposedComponentNode) {
            ComposedComponentNode cc = (ComposedComponentNode) parent;
            if (cc.getComponentGraph() == null) {
                ComposedComponentGraphImport importer = new ComposedComponentGraphImport();
                ComposedComponentGraphModel ccModel = (ComposedComponentGraphModel) importer.importGraph(cc.getType().getTypeClass(), "");
                cc.setComponentGraph(ccModel);
            }
            return searchForChildren(((ComposedComponentNode) parent).getComponentGraph());
        }
        List<GraphElement> children = new ArrayList<GraphElement>();
        if (parent instanceof ComponentNode) {
            ComponentNode node = (ComponentNode) parent;
            children.addAll(node.getInPorts());
            children.addAll(node.getOutPorts());
        } else if (parent instanceof AbstractGraphModel) {
            // TODO: How to deal with the filter --> probably should be a property of the node
            children.addAll(((AbstractGraphModel) parent).getAllComponentNodes());
            if (parent instanceof ComposedComponentGraphModel) {
                ComposedComponentGraphModel node = (ComposedComponentGraphModel) parent;
                children.addAll(node.getOuterInPorts());
                children.addAll(node.getOuterOutPorts());
            }
        }
        return children;
    }

    public GraphElementNode buildNode(GraphElement element) {
        GraphElementNode node = new GraphElementNode(element, element.name)
        elementNodes[element] = node
        for (GraphElement el : searchForChildren(element)) {
            GraphElementNode childNode = buildNode(el)
            if (childNode) {
                node.add(childNode)
            }
        }
        return node
    }

    public GraphElementNode findNode(GraphElement element) {
        return elementNodes[element];
    }
}
