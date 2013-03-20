package org.pillarone.riskanalytics.graph.formeditor.examples;

import org.pillarone.riskanalytics.core.components.Component;
import org.pillarone.riskanalytics.core.components.ComponentCategory;
import org.pillarone.riskanalytics.core.packets.PacketList;
import org.pillarone.riskanalytics.core.wiring.WiringValidation;

/**
 * 
 */
@ComponentCategory(categories={"Claim","R/I","Risk"})
public class QuotaShare extends Component {

    private double parmQuota = 0.0;

    private PacketList<ClaimPacket> inSingleClaims = new PacketList<ClaimPacket>(ClaimPacket.class);
    private PacketList<ClaimPacket> inAggregateClaims = new PacketList<ClaimPacket>(ClaimPacket.class);

    private PacketList<ClaimPacket> outCededSingleClaims = new PacketList<ClaimPacket>(ClaimPacket.class);
    private PacketList<ClaimPacket> outCededAggregateClaims = new PacketList<ClaimPacket>(ClaimPacket.class);
    private PacketList<ClaimPacket> outRetainedSingleClaims = new PacketList<ClaimPacket>(ClaimPacket.class);
    private PacketList<ClaimPacket> outRetainedAggregateClaims = new PacketList<ClaimPacket>(ClaimPacket.class);

    public void doCalculation() {
        for (ClaimPacket claim : getInAggregateClaims()) {
            double gross = claim.getValue();
            double ceded = getParmQuota()*gross;
            double retained = gross-ceded;
            ClaimPacket claimCeded = new ClaimPacket();
            claimCeded.setValue(ceded);
            getOutCededAggregateClaims().add(claimCeded);
            ClaimPacket claimRetained = new ClaimPacket();
            claimRetained.setValue(retained);
            getOutRetainedAggregateClaims().add(claimRetained);
        }

        for (ClaimPacket claim : getInSingleClaims()) {
            double gross = claim.getValue();
            double ceded = getParmQuota()*gross;
            double retained = gross-ceded;
            ClaimPacket claimCeded = new ClaimPacket();
            claimCeded.setValue(ceded);
            getOutCededSingleClaims().add(claimCeded);
            ClaimPacket claimRetained = new ClaimPacket();
            claimRetained.setValue(retained);
            getOutRetainedSingleClaims().add(claimRetained);
        }
    }

    public double getParmQuota() {
        return parmQuota;
    }

    public void setParmQuota(double qs) {
        this.parmQuota = qs;
    }

    public PacketList<ClaimPacket> getInSingleClaims() {
        return inSingleClaims;
    }

    public void setInSingleClaims(PacketList<ClaimPacket> inSingleClaims) {
        this.inSingleClaims = inSingleClaims;
    }

    public PacketList<ClaimPacket> getInAggregateClaims() {
        return inAggregateClaims;
    }

    public void setInAggregateClaims(PacketList<ClaimPacket> inAggregateClaims) {
        this.inAggregateClaims = inAggregateClaims;
    }

    public PacketList<ClaimPacket> getOutCededSingleClaims() {
        return outCededSingleClaims;
    }

    public void setOutCededSingleClaims(PacketList<ClaimPacket> outCededSingleClaims) {
        this.outCededSingleClaims = outCededSingleClaims;
    }

    public PacketList<ClaimPacket> getOutCededAggregateClaims() {
        return outCededAggregateClaims;
    }

    public void setOutCededAggregateClaims(PacketList<ClaimPacket> outCededAggregateClaims) {
        this.outCededAggregateClaims = outCededAggregateClaims;
    }

    public PacketList<ClaimPacket> getOutRetainedSingleClaims() {
        return outRetainedSingleClaims;
    }

    public void setOutRetainedSingleClaims(PacketList<ClaimPacket> outRetainedSingleClaims) {
        this.outRetainedSingleClaims = outRetainedSingleClaims;
    }

    public PacketList<ClaimPacket> getOutRetainedAggregateClaims() {
        return outRetainedAggregateClaims;
    }

    public void setOutRetainedAggregateClaims(PacketList<ClaimPacket> outRetainedAggregateClaims) {
        this.outRetainedAggregateClaims = outRetainedAggregateClaims;
    }
}
