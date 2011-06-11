package org.pillarone.riskanalytics.graph.formeditor.examples;

import org.pillarone.riskanalytics.core.components.Component;
import org.pillarone.riskanalytics.core.components.ComponentCategory;
import org.pillarone.riskanalytics.core.packets.PacketList;
import org.pillarone.riskanalytics.core.util.MathUtils;
import org.pillarone.riskanalytics.core.wiring.WiringValidation;
import umontreal.iro.lecuyer.randvar.NormalGen;
import umontreal.iro.lecuyer.randvar.RandomVariateGen;

/**
 *
 */
@ComponentCategory(categories={"Claims","Generators","Risk"})
public class SingleClaimsGenerator extends Component {

    private RandomVariateGen generator = new NormalGen(MathUtils.getRandomStreamBase(), 100, 5);

    @WiringValidation(connections = {0,Integer.MAX_VALUE},packets = {0,Integer.MAX_VALUE})
    private PacketList<FrequencyPacket> inFrequency = new PacketList<FrequencyPacket>(FrequencyPacket.class);

    private PacketList<ClaimPacket> outClaims = new PacketList<ClaimPacket>(ClaimPacket.class);

    public void doCalculation() {
        int freq = 0;
        if (isReceiverWired(inFrequency)) {
            for (int i = 0; i < inFrequency.size(); i++) {
                freq += inFrequency.get(i).getValue();
            }
        } else {
            freq = 1;
        }
        for (int i = 0; i < inFrequency.get(0).getValue(); i++) {
            ClaimPacket c = new ClaimPacket();
            c.setValue(generator.nextDouble());
            outClaims.add(c);
        }
    }
}
