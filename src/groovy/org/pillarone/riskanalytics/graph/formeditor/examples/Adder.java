package org.pillarone.riskanalytics.graph.formeditor.examples;

import org.pillarone.riskanalytics.core.components.Component;
import org.pillarone.riskanalytics.core.components.ComponentCategory;
import org.pillarone.riskanalytics.core.packets.PacketList;
import org.pillarone.riskanalytics.core.wiring.WiringValidation;

/**
 * 
 */
@ComponentCategory(categories={"Claims","Utilities","Arithmetics"})
public class Adder extends Component {
    @WiringValidation(connections={1,1},packets={1,1})
    private PacketList<ClaimPacket> inClaims1 = new PacketList<ClaimPacket>(ClaimPacket.class);

    @WiringValidation(connections={1,1},packets={1,1})
    private PacketList<ClaimPacket> inClaims2 = new PacketList<ClaimPacket>(ClaimPacket.class);

    private PacketList<ClaimPacket> outClaims = new PacketList<ClaimPacket>(ClaimPacket.class);

    public void doCalculation() {
        ClaimPacket claim = new ClaimPacket();
        claim.setValue(inClaims1.get(0).getValue()+inClaims2.get(0).getValue());
        outClaims.add(claim);
    }
}
