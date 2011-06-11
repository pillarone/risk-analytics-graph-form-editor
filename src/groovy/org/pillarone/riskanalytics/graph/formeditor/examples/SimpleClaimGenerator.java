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
public class SimpleClaimGenerator extends Component {

    private RandomVariateGen generator = new NormalGen(MathUtils.getRandomStreamBase(), 100, 5);

    @WiringValidation(connections = {0,Integer.MAX_VALUE},packets = {0,Integer.MAX_VALUE})
    private PacketList<FrequencyPacket> inFrequency = new PacketList<FrequencyPacket>(FrequencyPacket.class);

    private PacketList<ClaimPacket> outClaims = new PacketList<ClaimPacket>(ClaimPacket.class);

    public void doCalculation() {
        ClaimPacket claim = new ClaimPacket();
        double value = 0.0;
        if (isReceiverWired(inFrequency)) {
            int freq = 0;
            for (int i = 0; i < inFrequency.size(); i++) {
                freq += inFrequency.get(i).getValue();
            }
            for (int i = 0; i < inFrequency.get(0).getValue(); i++) {
                value += generator.nextDouble();
            }
        } else {
            value += generator.nextDouble();
        }
        claim.setValue(value);
        outClaims.add(claim);
    }
}
