package org.pillarone.riskanalytics.graph.formeditor.examples;

import org.pillarone.riskanalytics.core.components.Component;
import org.pillarone.riskanalytics.core.components.ComponentCategory;
import org.pillarone.riskanalytics.core.packets.PacketList;
import org.pillarone.riskanalytics.core.wiring.WiringValidation;

/**
 * 
 */
@ComponentCategory(categories={"Claims","Utilities","Arithmetics"})
public class Multiplier extends Component {

    private double parmValue = 1.0;

    @WiringValidation(connections={1,1},packets={1,1})
    private PacketList<ClaimPacket> inClaims = new PacketList<ClaimPacket>(ClaimPacket.class);

    private PacketList<ClaimPacket> outClaims = new PacketList<ClaimPacket>(ClaimPacket.class);

    public void doCalculation() {
        for (int i = 0; i < inClaims.size(); i++) {
            ClaimPacket claim = new ClaimPacket();
            claim.setValue(inClaims.get(i).getValue()*parmValue);
            outClaims.add(claim);
        }
    }
}
