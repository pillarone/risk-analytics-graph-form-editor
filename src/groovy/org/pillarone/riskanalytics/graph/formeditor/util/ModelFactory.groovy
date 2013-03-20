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

import org.pillarone.riskanalytics.graph.core.graph.model.InPort
import org.pillarone.riskanalytics.graph.core.graph.model.ComposedComponentGraphModel

import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel

/**
 */
class ModelFactory {

    /**
     * Provides a StochasticModel in form of a
     * @param graphModel
     * @return
     */

    public StochasticModel getModelInstance(final ModelGraphModel graphModel) {

        StochasticModel model = new StochasticModel() {

            public String getName() {
                return graphModel.packageName+"."+graphModel.name
            }

            public String getPackage() {
                return graphModel.packageName
            }

            @Override
            void initComponents() {
                for (ComponentNode node : graphModel.allComponentNodes) {
                    Class componentClazz = node.type.typeClass
                    Component comp = componentClazz.newInstance()
                    // this is required as initAllComponents() is not working as the components don't exist as property within the ModelFactory
                    allComponents << comp
                    if (comp instanceof ComposedComponent) {
                        allComposedComponents << comp
                    }
                    this.metaClass."$node.name"=comp
                    setComponentNames(comp, node)
                }
            }

            void setComponentNames(Component c, ComponentNode node) {
                if (c.name==null) {
                    c.name = node.name
                }
                if (node instanceof ComposedComponentNode) {
                    ComposedComponentNode ccNode = (ComposedComponentNode) node
                    for (ComponentNode subNode : ccNode.componentGraph.allComponentNodes) {
                        Component subComp = c."$subNode.name"
                        setComponentNames(subComp, subNode)
                    }
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
                for (Component starter : getStarterComponents(this, graphModel)) {
                    addStartComponent(starter)
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

    /**
     * Provides a test model for composed components in form of a StochasticModel - given a ComposedComponentGraphModel and a parametrization.
     * This model consists of all the inner components of the ComposedComponent under test
     * and additional helper components that feed the replicating in ports. Hence, teh inner components
     * of the ComposedComponent under test and the helper components appear at the same hierarchy level
     * of the test model. Actually, it would be nicer to use design a test model consisting of the
     * new ComposedComponent and the helper components that are connected to the replicating ports.
     * This would require that the ComposedComponent already can be instantiated as Component's.
     * However, this is not as easy as it sound since the ComposedComponent only exists at the design level
     * (ComposedComponentGraphModel) and not as a deployed ComposedComponent-class.
     * @param ccGraphModel
     * @param parameterization
     * @return
     */
    public StochasticModel getComposedComponentTestModel(final ComposedComponentGraphModel ccGraphModel) {

        StochasticModel model = new StochasticModel() {

            Map<String,PacketProvider> packetProviders = new HashMap<String,PacketProvider>();

            public String getName() {
                return "ComposedComponentTest"
            }

            public String getPackage() {
                return ccGraphModel.packageName
            }

            @Override
            void initComponents() {
                // instantiate the inner components
                for (ComponentNode node : ccGraphModel.allComponentNodes) {
                    Class componentClazz = node.type.typeClass
                    Component comp = componentClazz.newInstance()
                    this.metaClass."$node.name"=comp
                }

                // iterate through the outer ports
                // check whether there is mock data included in the parametrization associated with these ports
                // create a provider component for that
                for (InPort port : ccGraphModel.outerInPorts) {
                    PacketProvider provider = new PacketProvider(port.packetType, port.name)
                    String ppPath = provider.name
                    packetProviders.put(ppPath, provider)
                    this.metaClass."$ppPath" = provider
                }
            }

            @Override
            void initAllComponents() {
                super.initAllComponents()
                for (Component starter : getStarterComponents(this, ccGraphModel)) {
                    addStartComponent(starter)
                }
                for (PacketProvider pp : packetProviders.values()) {
                    addStartComponent(pp)
                }
            }


            @Override
            void wireComponents() {
                for (Connection connection : ccGraphModel.allConnections) {
                    if (!connection.isReplicatingConnection()) {
                        Component sender = this."$connection.from.componentNode.name"
                        PacketList source = sender."$connection.from.name"
                        Component receiver = this."$connection.to.componentNode.name"
                        PacketList target = receiver."$connection.to.name"
                        ITransmitter transmitter = new Transmitter(sender, source, receiver, target)
                        sender.allOutputTransmitter << transmitter
                        receiver.allInputTransmitter << transmitter
                    } else if (ccGraphModel.outerInPorts.contains(connection.from)){
                        String ppPath = PacketProvider.getPacketProviderPath(connection.from.name)
                        if (packetProviders.containsKey(ppPath)) {
                            PacketProvider sender = packetProviders.get(ppPath)
                            PacketList source = sender.outPacket
                            Component receiver = this."$connection.to.componentNode.name"
                            PacketList target = receiver."$connection.to.name"
                            ITransmitter transmitter = new Transmitter(sender, source, receiver, target)
                            sender.allOutputTransmitter << transmitter
                            receiver.allInputTransmitter << transmitter
                        }
                    }
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
        Class ccClazz = cc.getClass()
        List<Component> startComponents = new ArrayList<Component>()
        try {
            // in case the class implements a doCalculation I assume that the call to the starter component is done
            ccClazz.getDeclaredMethod("doCalculation", null)
        } catch (NoSuchMethodException ex) {
            // if no such method is found assume that start component has not been set --> hence we need to search for it
            // TODO: for deployment in RA we need to do changes in RA core as well...
            List<Connection> internalConnections = ccNode.componentGraph.allConnections
            for (ComponentNode node: ccNode.componentGraph.allComponentNodes) {
                boolean inConnected = false
                for (InPort inport: node.inPorts) {
                    Connection conn = internalConnections.find {it.to == inport}
                    if (conn != null && !conn.isReplicatingConnection()) {
                        inConnected = true
                    } else if (conn != null) {
                        inConnected = true
                        // TODO check whether this port is connected from outside - if yes, set isConnected to true
                        // InPort replPort = (InPort) conn.from
                        // InPort externallyVisibleInPort = (InPort) ccNode.getPort(replPort.name)
                    }
                }
                if (!inConnected) {
                    Component c = cc."$node.name"
                    if (node instanceof ComposedComponentNode) {
                        List<Component> startingSubComps = getStarterComponents((ComposedComponent)c, (ComposedComponentNode)node)
                        if (startingSubComps.size()>0) {
                            startComponents.addAll(startingSubComps)
                        }
                    }
                    startComponents.add(c)
                }
            }
        }
        return startComponents
    }

    public static List<Component> getStarterComponents(StochasticModel model, AbstractGraphModel graphModel) {
        List<Component> startComponents = new ArrayList<Component>()
        List<Connection> internalConnections = graphModel.allConnections
        for (ComponentNode node: graphModel.allComponentNodes) {
            // check first the sub-components (recursively)
            Component c = model."$node.name"
            if (node instanceof ComposedComponentNode) {
                List<Component> startingSubComps = getStarterComponents((ComposedComponent)c, (ComposedComponentNode)node)
                if (startingSubComps.size()>0) {
                    startComponents.addAll(startingSubComps)
                }
            }

            // then check whether the current component is in-connected, if not add it to the starter component
            boolean inConnected = false
            for (InPort inport: node.inPorts) {
                if (internalConnections.find {it.to == inport} != null) {
                    inConnected = true
                }
            }
            if (!inConnected) {
                startComponents.add(c)
            }
        }
        return startComponents
    }
}
