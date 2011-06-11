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
        for (int i = 0; i < inClaims.size(); i++) {
            double gross = inClaims.get(i).getValue();
            double ceded = Math.min(parmLimit, Math.max(gross - parmRetention, 0));
            double retained = gross-ceded;
            ClaimPacket claimCeded = new ClaimPacket();
            claimCeded.setValue(ceded);
            outCededClaims.add(claimCeded);
            ClaimPacket claimRetained = new ClaimPacket();
            claimRetained.setValue(retained);
            outCededClaims.add(claimRetained);
        }
    }
}
