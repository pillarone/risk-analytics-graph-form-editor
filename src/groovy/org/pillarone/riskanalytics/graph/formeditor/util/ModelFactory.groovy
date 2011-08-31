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
import org.pillarone.riskanalytics.graph.core.graph.model.ComposedComponentGraphModel
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.packets.Packet

/**
 */
class ModelFactory {

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


    public StochasticModel getComposedComponentTestModel(final ComposedComponentGraphModel ccGraphModel,
                                                         final Parameterization parameterization) {

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
                    String ppPath = "provider_"+port.name
                    List params = parameterization.getParameters(ppPath+":parmPacket")
                    if ( params!= null && params.size()>0) {
                        PacketProvider provider = new PacketProvider()
                        packetProviders.put(ppPath, provider)
                        this.metaClass."$ppPath" = provider
                    }
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
                        String ppPath = "provider_"+connection.from.name
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
            startComponents.add(cc)
        } catch (NoSuchMethodException ex) {
            // if no such method is found assume that start component has not been set --> hence we need to search for it
            // TODO: for deployment in RA we need to do changes in RA core as well...
            List<Connection> internalConnections = ccNode.componentGraph.allConnections
            for (ComponentNode node: ccNode.componentGraph.allComponentNodes) {
                boolean inConnected = false
                for (InPort inport: node.inPorts) {
                    if (internalConnections.find {it.to == inport} != null) {
                        inConnected = true
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
                        startComponents.add(c)
                    }
                }
            }
        }
        return startComponents
    }

    public static List<Component> getStarterComponents(StochasticModel model, ComposedComponentGraphModel ccGraphModel) {
        List<Component> startComponents = new ArrayList<Component>()
        List<Connection> internalConnections = ccGraphModel.allConnections
        for (ComponentNode node: ccGraphModel.allComponentNodes) {
            boolean inConnected = false
            for (InPort inport: node.inPorts) {
                if (internalConnections.find {it.to == inport} != null) {
                    inConnected = true
                }
            }
            if (!inConnected) {
                Component c = model."$node.name"
                if (node instanceof ComposedComponentNode) {
                    List<Component> startingSubComps = getStarterComponents((ComposedComponent)c, (ComposedComponentNode)node)
                    if (startingSubComps.size()>0) {
                        startComponents.addAll(startingSubComps)
                    }
                } else {
                    startComponents.add(c)
                }
            }
        }
        return startComponents
    }
}

class PacketProvider extends Component {

    private Packet parmPacket = new Packet();

    public PacketList<Packet> outPacket = new PacketList<Packet>();

    protected void doCalculation() {
        outPacket.add(parmPacket)
    }
}
