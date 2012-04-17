package org.pillarone.riskanalytics.graph.formeditor.examples;

import org.pillarone.riskanalytics.core.components.Component;
import org.pillarone.riskanalytics.core.components.ComponentCategory;
import org.pillarone.riskanalytics.core.packets.PacketList;
import org.pillarone.riskanalytics.core.wiring.WiringValidation;

/**
 * 
 */
@ComponentCategory(categories={"Claim","Utilities","Arithmetics"})
public class Aggregator extends Component {
    @WiringValidation(connections={1,Integer.MAX_VALUE},packets={1,Integer.MAX_VALUE})
    private PacketList<ClaimPacket> inClaims = new PacketList<ClaimPacket>(ClaimPacket.class);
    private PacketList<ClaimPacket> outClaims = new PacketList<ClaimPacket>(ClaimPacket.class);

    public void doCalculation() {
        double value = 0.0;
        for (int i = 0; i < getInClaims().size(); i++) {
            value += getInClaims().get(i).getValue();
        }
        ClaimPacket claim = new ClaimPacket();
        claim.setValue(value);
        getOutClaims().add(claim);
    }

    public PacketList<ClaimPacket> getInClaims() {
        return inClaims;
    }

    public void setInClaims(PacketList<ClaimPacket> inClaims) {
        this.inClaims = inClaims;
    }

    public PacketList<ClaimPacket> getOutClaims() {
        return outClaims;
    }

    public void setOutClaims(PacketList<ClaimPacket> outClaims) {
        this.outClaims = outClaims;
    }
}
