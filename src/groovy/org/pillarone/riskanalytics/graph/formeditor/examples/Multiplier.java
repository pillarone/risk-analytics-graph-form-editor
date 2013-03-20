package org.pillarone.riskanalytics.graph.formeditor.examples;

import org.pillarone.riskanalytics.core.components.Component;
import org.pillarone.riskanalytics.core.components.ComponentCategory;
import org.pillarone.riskanalytics.core.packets.PacketList;
import org.pillarone.riskanalytics.core.wiring.WiringValidation;

/**
 * 
 */
@ComponentCategory(categories={"Utilities","Arithmetics"})
public class Multiplier extends Component {

    private double parmValue = 1.0;

    @WiringValidation(connections={1,1},packets={1,1})
    private PacketList<ClaimPacket> inClaims = new PacketList<ClaimPacket>(ClaimPacket.class);

    private PacketList<ClaimPacket> outClaims = new PacketList<ClaimPacket>(ClaimPacket.class);

    public void doCalculation() {
        for (int i = 0; i < getInClaims().size(); i++) {
            ClaimPacket claim = new ClaimPacket();
            claim.setValue(getInClaims().get(i).getValue()* getParmValue());
            getOutClaims().add(claim);
        }
    }

    public double getParmValue() {
        return parmValue;
    }

    public void setParmValue(double parmValue) {
        this.parmValue = parmValue;
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
