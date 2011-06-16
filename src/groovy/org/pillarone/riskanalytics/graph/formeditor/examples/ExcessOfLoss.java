package org.pillarone.riskanalytics.graph.formeditor.examples;

import org.pillarone.riskanalytics.core.components.Component;
import org.pillarone.riskanalytics.core.components.ComponentCategory;
import org.pillarone.riskanalytics.core.packets.PacketList;
import org.pillarone.riskanalytics.core.wiring.WiringValidation;

/**
 * 
 */
@ComponentCategory(categories={"Claims","Reinsurance","Risk"})
public class ExcessOfLoss extends Component {

    private double parmRetention = Double.MAX_VALUE;
    private double parmLimit = 0.0;

    @WiringValidation(connections={1,1000},packets={1,1000})
    private PacketList<ClaimPacket> inClaims = new PacketList<ClaimPacket>(ClaimPacket.class);

    private PacketList<ClaimPacket> outCededClaims = new PacketList<ClaimPacket>(ClaimPacket.class);
    private PacketList<ClaimPacket> outRetainedClaims = new PacketList<ClaimPacket>(ClaimPacket.class);

    public void doCalculation() {
        for (int i = 0; i < getInClaims().size(); i++) {
            double gross = getInClaims().get(i).getValue();
            double ceded = Math.min(getParmLimit(), Math.max(gross - getParmRetention(), 0));
            double retained = gross-ceded;
            ClaimPacket claimCeded = new ClaimPacket();
            claimCeded.setValue(ceded);
            getOutCededClaims().add(claimCeded);
            ClaimPacket claimRetained = new ClaimPacket();
            claimRetained.setValue(retained);
            getOutRetainedClaims().add(claimRetained);
        }
    }

    public double getParmRetention() {
        return parmRetention;
    }

    public void setParmRetention(double parmRetention) {
        this.parmRetention = parmRetention;
    }

    public double getParmLimit() {
        return parmLimit;
    }

    public void setParmLimit(double parmLimit) {
        this.parmLimit = parmLimit;
    }

    public PacketList<ClaimPacket> getInClaims() {
        return inClaims;
    }

    public void setInClaims(PacketList<ClaimPacket> inClaims) {
        this.inClaims = inClaims;
    }

    public PacketList<ClaimPacket> getOutCededClaims() {
        return outCededClaims;
    }

    public void setOutCededClaims(PacketList<ClaimPacket> outCededClaims) {
        this.outCededClaims = outCededClaims;
    }

    public PacketList<ClaimPacket> getOutRetainedClaims() {
        return outRetainedClaims;
    }

    public void setOutRetainedClaims(PacketList<ClaimPacket> outRetainedClaims) {
        this.outRetainedClaims = outRetainedClaims;
    }
}
