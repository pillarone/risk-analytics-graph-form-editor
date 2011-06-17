package org.pillarone.riskanalytics.graph.formeditor.util

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.model.StochasticModel
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.wiring.ITransmitter
import org.pillarone.riskanalytics.core.wiring.Transmitter
import org.pillarone.riskanalytics.graph.core.graph.model.ComponentNode
import org.pillarone.riskanalytics.graph.core.graph.model.Connection
import org.pillarone.riskanalytics.graph.core.graph.model.ModelGraphModel

/**
 */
class ModelFactory {

    public StochasticModel getModelInstance(final ModelGraphModel graphModel) {

        StochasticModel model = new StochasticModel() {

            @Override
            void initComponents() {
                for (ComponentNode node : graphModel.allComponentNodes) {
                    Class componentClazz = node.type.typeClass
                    Component comp = componentClazz.newInstance()
                    this.metaClass."$node.name"=comp
                }
            }

            @Override
            void initAllComponents() {
                super.initAllComponents()
                /*dynamicallyCreatedComponents.each { node, component ->
                    allComponents << component
                    if (component instanceof ComposedComponent) {
                        allComposedComponents << component
                    }
                }*/

                graphModel.startComponents = graphModel.resolveStartComponents()
                graphModel.startComponents.each {node ->
                    addStartComponent(this."$node.name")
                }

            }

            @Override
            void wireComponents() {
                for (Connection connection : graphModel.allConnections) {
                    Component sender = this."$connection.from.componentNode.name"
                    PacketList source = sender."$connection.from.name"
                    Component receiver = this."$connection.to.componentNode.name"
                    PacketList target = receiver."$connection.to.name"
                    ITransmitter transmitter = new Transmitter(sender, source, receiver, target)
                    sender.allOutputTransmitter << transmitter
                    receiver.allInputTransmitter << transmitter
                }
            }

            @Override
            void wire() {
                wireComponents()
                traverseSubComponents()
                for (Component component: allComponents) {
                    component.validateWiring()
                }
            }
        }

        return model
    }
}
