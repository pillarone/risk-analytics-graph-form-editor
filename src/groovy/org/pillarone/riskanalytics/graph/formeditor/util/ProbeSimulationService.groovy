package org.pillarone.riskanalytics.graph.formeditor.util

import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.fileimport.FileImportService
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.packets.Packet
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.parameterization.StructureInformation
import org.pillarone.riskanalytics.core.simulation.engine.MappingCache
import org.pillarone.riskanalytics.core.simulation.engine.SimulationRunner
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope
import org.pillarone.riskanalytics.core.simulation.engine.grid.SimulationBlock
import org.pillarone.riskanalytics.core.simulation.item.ModelStructure
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.ResultConfiguration
import org.pillarone.riskanalytics.core.simulation.item.Simulation
import org.pillarone.riskanalytics.core.wiring.ITransmitter
import org.pillarone.riskanalytics.core.wiring.Transmitter
import org.pillarone.riskanalytics.graph.core.graph.model.ComponentNode
import org.pillarone.riskanalytics.graph.core.graph.model.ComposedComponentNode
import org.pillarone.riskanalytics.graph.core.graph.model.ModelGraphModel
import org.pillarone.riskanalytics.core.output.*

/**
 * 
 */
class ProbeSimulationService extends SimulationRunner {

    Map output // path -> field -> period -> iteration -> single values


    public SimulationRunner getSimulationRunner(ModelGraphModel graphModel, Parameterization parametrization) {
        // name
        String name = graphModel.name+"_temp"
        long time = System.currentTimeMillis()

        // model instance
        ModelFactory factory = new ModelFactory()
        Model model = factory.getModelInstance(graphModel)

        // create a simulation object for the probing
        Simulation simulation = new Simulation(name+"_"+time)
        simulation.id = "$time"
        simulation.numberOfIterations = 1
        simulation.beginOfFirstPeriod = new DateTime(time)
        simulation.randomSeed = Math.abs(new Long(time).intValue())
        simulation.modelClass = model.class
        simulation.periodCount = 1
        simulation.parameterization = parametrization
        // simulation.parameterization.parameterHolders = parametrization.parameterHolders.collect { it.clone() }
        simulation.structure = new ProbeModelStructure(name)

        // now create the runner and configure it
        SimulationRunner runner = SimulationRunner.createRunner()
        runner.currentScope.simulation = simulation
        runner.currentScope.model = model
        runner.currentScope.iterationScope.numberOfPeriods = simulation.periodCount
        runner.currentScope.simulationBlocks = [new SimulationBlock(0, simulation.numberOfIterations, 0)]
        runner.simulationAction.iterationAction.periodAction.model = model
        runner.currentScope.mappingCache = new MappingCache()

        // result template
        ICollectorOutputStrategy outputStrategy = new OutputStrategyInCache()
        runner.currentScope.outputStrategy = outputStrategy
        ResultConfiguration resultTemplate = new ProbeResultConfiguration(graphModel, runner.currentScope)
        runner.currentScope.simulation.template = resultTemplate
        runner.currentScope.resultConfiguration = resultTemplate

        return runner
    }

    void addToCache(SingleValueResultPOJO result) {
        if (output==null) {
            output = [:]
        }
        if (!output.containsKey(result.path.pathName)) {
            output[result.path.pathName]=[:]
        }
        Map field = (Map) output[result.path.pathName]
        if (!field.containsKey(result.field.fieldName)) {
            field[result.field.fieldName] = [:]
        }
        Map periods = (Map) field[result.field.fieldName]
        if (!periods.containsKey(result.period)) {
            periods[result.period] = [:]
        }
        Map iterations = (Map)periods[result.period]
        if (!iterations.containsKey(result.iteration)) {
            iterations[result.iteration] = []
        }
        iterations[result.iteration] << result.value
    }

    private class ProbeModelStructure extends ModelStructure {

        String configText = '''
            periodCount = 1
            company {
            }
        '''
        ProbeModelStructure(String name) {
            super(name)
            mapFromDao(null, true)
        }

        protected void mapFromDao(def source, boolean completeLoad) {
            org.pillarone.riskanalytics.core.util.GroovyUtils.parseGroovyScript configText, { ConfigObject config ->
                data = config
            }
            FileImportService.spreadRanges(data)
        }
    }

    private class ProbeResultConfiguration extends ResultConfiguration {

        SimulationScope fSimulationScope

        public ProbeResultConfiguration(ModelGraphModel graphModel, SimulationScope simulationScope) {
            super(graphModel.name)
            fSimulationScope = simulationScope
            collectors = []
            graphModel.allComponentNodes.each { component ->
                getCollectors(component, component.name).each { collector ->
                    collectors << collector
                }
            }
        }

        public List<PacketCollector> getResolvedCollectors(Model model, CollectorFactory collectorFactory) {
            return collectors
        }

        private List<PacketCollector> getCollectors(ComponentNode component, String path) {
            List<PacketCollector> collectors = []
            component.outPorts?.each { outPort ->
                PacketCollector collector = new ProbePacketCollector(fSimulationScope)
                collector.path = path+":"+outPort.name
                collectors << collector
            }
            if (component instanceof ComposedComponentNode) {
                ((ComposedComponentNode)component).componentGraph.allComponentNodes.each { subComponent ->
                    getCollectors(subComponent, path+":"+subComponent.name).each { collector ->
                        collectors << collector
                    }
                }
            }
            return collectors
        }
    }

    private class ProbePacketCollector extends PacketCollector {

        ProbePacketCollector(SimulationScope scope) {
            super()
            setMode(new SingleValueCollectMode())
            this.simulationScope = scope
            this.outputStrategy = scope.outputStrategy
        }

        public attachToModel(Model model, StructureInformation structureInformation) {
            def pathElements = path.split("\\:")
            def sender = model
            pathElements[1..-2].each {propertyName ->
                if (sender.properties.containsKey(propertyName)) {
                    sender = sender[propertyName]
                }
            }
            String outChannelName = pathElements[-1]
            PacketList source = sender."$outChannelName"
            ITransmitter transmitter = new Transmitter(sender, source, this, this.inPackets)
            sender.allOutputTransmitter << transmitter
            this.allInputTransmitter << transmitter
        }
    }

    private class OutputStrategyInCache implements ICollectorOutputStrategy {

        ICollectorOutputStrategy leftShift(List results) {
            for (SingleValueResultPOJO result in results) {
                addToCache(result)
            }
            return this
        }

        void finish() {}
    }

    static final String IDENTIFIER = "SINGLE"
    private class SingleValueCollectMode implements ICollectingModeStrategy {
        PacketCollector collector

        public List<SingleValueResultPOJO> collect(PacketList results) throws IllegalAccessException {
            List<SingleValueResultPOJO> pojoResults = new ArrayList<SingleValueResultPOJO>(results.size());
            int valueIndex = 0;
            for (Packet packet : results) {
                int period = packet?.period != null ? packet.period : collector.simulationScope.iterationScope.periodScope.currentPeriod
                DateTime date = packet?.date != null ? packet.date : collector.simulationScope.iterationScope.periodScope.currentPeriodStartDate
                int iteration = collector.simulationScope.iterationScope.currentIteration
                PathMapping path = new PathMapping(pathName: collector.path)
                for (Map.Entry<String, Number> entry : packet.getValuesToSave().entrySet()){
                    SingleValueResultPOJO singleValue = new SingleValueResultPOJO()
                    singleValue.setIteration(iteration)
                    singleValue.setPeriod(period)
                    singleValue.setPath(path)
                    singleValue.setField(new FieldMapping(fieldName:entry.key))
                    singleValue.setValueIndex(valueIndex)
                    singleValue.setValue(entry.value.doubleValue())
                    singleValue.setDate(date)
                    pojoResults << singleValue
                }
                valueIndex++;
            }
            return pojoResults;
        }

        public String getDisplayName(Locale locale) {
            return IDENTIFIER
        }

        public String getIdentifier() {
            return IDENTIFIER;
        }

        void setPacketCollector(PacketCollector packetCollector) {
            collector = packetCollector
        }
    }


}
