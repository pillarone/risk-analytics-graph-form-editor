package org.pillarone.riskanalytics.graph.formeditor.examples;

import org.pillarone.riskanalytics.core.components.Component;
import org.pillarone.riskanalytics.core.components.ComponentCategory;
import org.pillarone.riskanalytics.core.packets.PacketList;
import org.pillarone.riskanalytics.core.util.MathUtils;
import umontreal.iro.lecuyer.randvar.PoissonGen;
import umontreal.iro.lecuyer.randvar.RandomVariateGenInt;

/**
 *
 */
@ComponentCategory(categories={"Generators","Risk"})
public class PoissonFrequencyGenerator extends Component {

    private double parmMean = 1.0;
    private PacketList<FrequencyPacket> outFrequency = new PacketList<FrequencyPacket>(FrequencyPacket.class);

    public void doCalculation() {
        RandomVariateGenInt generator = new PoissonGen(MathUtils.getRandomStreamBase(), getParmMean());
        FrequencyPacket frequency = new FrequencyPacket();
        frequency.setValue(generator.nextInt());
        getOutFrequency().add(frequency);
    }

    public double getParmMean() {
        return parmMean;
    }

    public void setParmMean(double parmMean) {
        this.parmMean = parmMean;
    }

    public PacketList<FrequencyPacket> getOutFrequency() {
        return outFrequency;
    }

    public void setOutFrequency(PacketList<FrequencyPacket> outFrequency) {
        this.outFrequency = outFrequency;
    }
}
