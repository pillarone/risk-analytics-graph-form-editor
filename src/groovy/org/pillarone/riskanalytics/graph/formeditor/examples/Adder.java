package org.pillarone.riskanalytics.graph.formeditor.examples;

import org.pillarone.riskanalytics.core.components.Component;
import org.pillarone.riskanalytics.core.components.ComponentCategory;
import org.pillarone.riskanalytics.core.packets.PacketList;
import org.pillarone.riskanalytics.core.wiring.WiringValidation;

/**
 * 
 */
@ComponentCategory(categories={"Claim","Utilities","Arithmetics"})
public class Adder extends Component {
    @WiringValidation(connections={1,1},packets={1,1})
    private PacketList<ClaimPacket> inClaims1 = new PacketList<ClaimPacket>(ClaimPacket.class);

    @WiringValidation(connections={1,1},packets={1,1})
    private PacketList<ClaimPacket> inClaims2 = new PacketList<ClaimPacket>(ClaimPacket.class);

    private PacketList<ClaimPacket> outClaims = new PacketList<ClaimPacket>(ClaimPacket.class);

    public void doCalculation() {
        ClaimPacket claim = new ClaimPacket();
        claim.setValue(getInClaims1().get(0).getValue()+ getInClaims2().get(0).getValue());
        getOutClaims().add(claim);
    }

    public PacketList<ClaimPacket> getInClaims1() {
        return inClaims1;
    }

    public void setInClaims1(PacketList<ClaimPacket> inClaims1) {
        this.inClaims1 = inClaims1;
    }

    public PacketList<ClaimPacket> getInClaims2() {
        return inClaims2;
    }

    public void setInClaims2(PacketList<ClaimPacket> inClaims2) {
        this.inClaims2 = inClaims2;
    }

    public PacketList<ClaimPacket> getOutClaims() {
        return outClaims;
    }

    public void setOutClaims(PacketList<ClaimPacket> outClaims) {
        this.outClaims = outClaims;
    }
}
