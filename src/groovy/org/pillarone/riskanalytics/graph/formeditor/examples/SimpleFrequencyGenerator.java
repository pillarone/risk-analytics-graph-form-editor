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
public class SimpleFrequencyGenerator extends Component {

    private RandomVariateGenInt generator = new PoissonGen(MathUtils.getRandomStreamBase(), 10);
    private PacketList<FrequencyPacket> outFrequency = new PacketList<FrequencyPacket>(FrequencyPacket.class);

    public void doCalculation() {
        FrequencyPacket frequency = new FrequencyPacket();
        frequency.setValue(generator.nextInt());
        outFrequency.add(frequency);
    }
}
