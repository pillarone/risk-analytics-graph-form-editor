package org.pillarone.riskanalytics.graph.formeditor.examples;

import org.pillarone.riskanalytics.core.components.Component;
import org.pillarone.riskanalytics.core.components.ComponentCategory;
import org.pillarone.riskanalytics.core.packets.PacketList;
import org.pillarone.riskanalytics.core.util.MathUtils;
import umontreal.iro.lecuyer.randvar.NegativeBinomialGen;
import umontreal.iro.lecuyer.randvar.PoissonGen;
import umontreal.iro.lecuyer.randvar.RandomVariateGenInt;

/**
 *
 */
@ComponentCategory(categories={"Generators","Risk"})
public class NegativeBinomialFrequencyGenerator extends Component {

    private double parmGamma = 0.5;
    private double parmP = 0.5;
    private PacketList<FrequencyPacket> outFrequency = new PacketList<FrequencyPacket>(FrequencyPacket.class);

    public void doCalculation() {
        RandomVariateGenInt generator = new NegativeBinomialGen(MathUtils.getRandomStreamBase(), getParmGamma(), getParmP());
        FrequencyPacket frequency = new FrequencyPacket();
        frequency.setValue(generator.nextInt());
        getOutFrequency().add(frequency);
    }

    public double getParmP() {
        return parmP;
    }

    public void setParmP(double parmP) {
        this.parmP = parmP;
    }

    public double getParmGamma() {
        return parmGamma;
    }

    public void setParmGamma(double parmGamma) {
        this.parmGamma = parmGamma;
    }

    public PacketList<FrequencyPacket> getOutFrequency() {
        return outFrequency;
    }

    public void setOutFrequency(PacketList<FrequencyPacket> outFrequency) {
        this.outFrequency = outFrequency;
    }
}
