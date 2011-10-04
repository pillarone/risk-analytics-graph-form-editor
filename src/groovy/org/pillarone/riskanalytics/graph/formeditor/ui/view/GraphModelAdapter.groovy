package org.pillarone.riskanalytics.graph.formeditor.ui.view

import org.pillarone.riskanalytics.core.model.StochasticModel
import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel
import org.pillarone.riskanalytics.graph.core.graph.model.ComponentNode
import org.pillarone.riskanalytics.core.components.Component


class GraphModelAdapter extends StochasticModel {

    AbstractGraphModel graphModel

    Map<String, Component> components = [:]

    GraphModelAdapter(AbstractGraphModel graphModel) {
        this.graphModel = graphModel
        for (ComponentNode node in graphModel.allComponentNodes) {
            final Component instance = node.type.typeClass.newInstance()
            instance.name = node.name
            components.put(node.name, instance)
        }
    }

    @Override
    void initComponents() {

    }

    @Override
    void wireComponents() {

    }

    Map getProperties() {
        Map prop = super.getProperties()

        for (Component component in components.values()) {
            prop.put(component.name, component)
        }

        return prop
    }

    Object getAt(String propertyName) {
        try {
            return super.getAt(propertyName)
        } catch (MissingPropertyException e) {
            def result = components[propertyName]
            if (result == null) {
                throw new MissingPropertyException(propertyName)
            }
            return result
        }
    }

    @Override
    List<String> getSortedProperties() {
        return components.keySet().toList()
    }



    @Override
    String getName() {
        return graphModel.name - "Model"
    }


}
