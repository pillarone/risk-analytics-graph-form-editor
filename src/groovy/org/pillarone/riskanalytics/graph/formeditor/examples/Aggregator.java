package org.pillarone.riskanalytics.graph.formeditor.examples;

import org.pillarone.riskanalytics.core.components.Component;
import org.pillarone.riskanalytics.core.components.ComponentCategory;
import org.pillarone.riskanalytics.core.packets.PacketList;
import org.pillarone.riskanalytics.core.wiring.WiringValidation;

/**
 * 
 */
@ComponentCategory(categories={"Claims","Utilities","Arithmetics"})
public class Aggregator extends Component {
    @WiringValidation(connections={1,Integer.MAX_VALUE},packets={1,Integer.MAX_VALUE})
    private PacketList<ClaimPacket> inClaims = new PacketList<ClaimPacket>(ClaimPacket.class);

    private PacketList<ClaimPacket> outClaims = new PacketList<ClaimPacket>(ClaimPacket.class);

    public void doCalculation() {
        double value = 0.0;
        for (int i = 0; i < inClaims.size(); i++) {
            value += inClaims.get(i).getValue();
        }
        ClaimPacket claim = new ClaimPacket();
        claim.setValue(value);
        outClaims.add(claim);
    }
}
