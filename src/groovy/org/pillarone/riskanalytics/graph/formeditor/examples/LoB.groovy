package org.pillarone.riskanalytics.graph.formeditor.examples;


import org.pillarone.riskanalytics.core.components.ComponentCategory
import org.pillarone.riskanalytics.core.components.ComposedComponent
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.wiring.PortReplicatorCategory
import org.pillarone.riskanalytics.core.wiring.WireCategory
import org.pillarone.riskanalytics.core.wiring.WiringUtils

/**
 * 
 */
@ComponentCategory(categories=["Claim","Risk"])
public class LoB extends ComposedComponent {
    PacketList<ClaimPacket> outSingleGrossClaims = new PacketList<ClaimPacket>(ClaimPacket.class);
    PacketList<ClaimPacket> outSingleRetainedClaims = new PacketList<ClaimPacket>(ClaimPacket.class);
    PacketList<ClaimPacket> outAttrGrossClaims = new PacketList<ClaimPacket>(ClaimPacket.class);
    PacketList<ClaimPacket> outAttrRetainedClaims = new PacketList<ClaimPacket>(ClaimPacket.class);
    PacketList<ClaimPacket> outTotalGrossClaims = new PacketList<ClaimPacket>(ClaimPacket.class);
    PacketList<ClaimPacket> outTotalNetClaims = new PacketList<ClaimPacket>(ClaimPacket.class);

    PoissonFrequencyGenerator subLargeFrequencyGen = new PoissonFrequencyGenerator(name: 'subLargeFrequencyGen');
    SingleLogNormalClaimsGenerator subLargeClaimsGen = new SingleLogNormalClaimsGenerator(name: 'subLargeClaimsGen');
    AggregateNormalClaimGenerator subAttrClaimsGen = new AggregateNormalClaimGenerator(name: 'subAttrClaimsGen');

    QuotaShare subQuotaShareContract = new QuotaShare(name: 'subQuotaShareContract');
    ExcessOfLoss subXLContract = new ExcessOfLoss(name: 'subXLContract');

    Aggregator subAggregatorGross = new Aggregator(name: 'subAggregatorGross');
    Aggregator subAggregatorNet = new Aggregator(name: 'subAggregatorNet');

    @Override
    public void wire() {
        WiringUtils.use(PortReplicatorCategory) {
            this.outSingleGrossClaims = subLargeClaimsGen.outClaims;
            this.outSingleRetainedClaims = subXLContract.outRetainedClaims;
            this.outAttrGrossClaims = subAttrClaimsGen.outClaims;
            this.outAttrRetainedClaims = subQuotaShareContract.outRetainedAggregateClaims;
            this.outTotalGrossClaims = subAggregatorGross.outClaims;
            this.outTotalNetClaims = subAggregatorNet.outClaims;
        }

        WiringUtils.use(WireCategory) {
            subLargeClaimsGen.inFrequency = subLargeFrequencyGen.outFrequency

            subQuotaShareContract.inAggregateClaims = subAttrClaimsGen.outClaims
            subQuotaShareContract.inSingleClaims = subLargeClaimsGen.outClaims

            subXLContract.inClaims = subQuotaShareContract.outRetainedSingleClaims

            subAggregatorGross.inClaims = subLargeClaimsGen.outClaims
            subAggregatorGross.inClaims = subAttrClaimsGen.outClaims

            subAggregatorNet.inClaims = subXLContract.outRetainedClaims
            subAggregatorNet.inClaims = subQuotaShareContract.outRetainedAggregateClaims
        }
    }
}
