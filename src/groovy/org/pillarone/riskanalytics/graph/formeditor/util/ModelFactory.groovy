package org.pillarone.riskanalytics.graph.formeditor.util

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.model.StochasticModel
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.wiring.ITransmitter
import org.pillarone.riskanalytics.core.wiring.Transmitter
import org.pillarone.riskanalytics.graph.core.graph.model.ComponentNode
import org.pillarone.riskanalytics.graph.core.graph.model.Connection
import org.pillarone.riskanalytics.graph.core.graph.model.ModelGraphModel
import org.pillarone.riskanalytics.core.components.ComposedComponent
import org.pillarone.riskanalytics.graph.core.graph.model.ComposedComponentNode
import java.lang.reflect.Method
import org.pillarone.riskanalytics.graph.core.graph.model.InPort

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
                    Component comp = (Component) this."$node.name";
                    if (comp instanceof ComposedComponent) {
                        ComposedComponent cc = (ComposedComponent) comp;
                        List<Component> starters = getStarterComponents(cc, (ComposedComponentNode)node)
                        starters.each {it -> addStartComponent(it)}
                    } else {
                        addStartComponent(comp)
                    }
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


    public static List<Component> getStarterComponents(ComposedComponent cc, ComposedComponentNode ccNode) {
        Class ccClazz = cc.getClass();
        List<Component> startComponents = new ArrayList<Component>()
        try {
            // in case the class implements a doCalculation I assume that the call to the starter component is done
            ccClazz.getDeclaredMethod("doCalculation", null)
            startComponents.add(cc)
        } catch (NoSuchMethodException ex) {
            // if no such method is found assume that start component has not been set --> hence we need to search for it
            // TODO: for deployment in RA we need to do changes in RA core as well...
            List<Connection> internalConnections = ccNode.componentGraph.allConnections
            for (ComponentNode node: ccNode.componentGraph.allComponentNodes) {
                boolean inConnected = false
                for (InPort inport: node.inPorts) {
                    if (internalConnections.find {it.to == inport} != null) {
                        inConnected = true;
                    }
                }
                if (!inConnected) {
                    Component c = cc."$node.name"
                    if (node instanceof ComposedComponentNode) {
                        List<Component> startingSubComps = getStarterComponents((ComposedComponent)c, (ComposedComponentNode)node)
                        if (startingSubComps.size()>0) {
                            startComponents.addAll(startingSubComps)
                        }
                    } else {
                        startComponents.add(c);
                    }
                }
            }
        }
        return startComponents

    }


}
