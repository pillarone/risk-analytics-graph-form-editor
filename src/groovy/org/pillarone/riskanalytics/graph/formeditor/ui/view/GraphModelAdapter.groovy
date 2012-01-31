package org.pillarone.riskanalytics.graph.formeditor.ui.view

import org.pillarone.riskanalytics.core.model.StochasticModel
import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel
import org.pillarone.riskanalytics.graph.core.graph.model.ComponentNode
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.graph.core.graph.model.ComposedComponentGraphModel
import org.pillarone.riskanalytics.graph.formeditor.util.PacketProvider
import org.pillarone.riskanalytics.core.packets.Packet
import org.pillarone.riskanalytics.graph.core.graph.model.InPort


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

        if (graphModel instanceof ComposedComponentGraphModel) {
            ComposedComponentGraphModel ccGraphModel = (ComposedComponentGraphModel)graphModel
            for (InPort port : ccGraphModel.outerInPorts) {
                PacketProvider provider = new PacketProvider(port.packetType, port.name)
                components.put(provider.name, provider)
            }
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
