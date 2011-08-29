package org.pillarone.riskanalytics.graph.formeditor.examples

import org.pillarone.riskanalytics.core.components.ComposedComponent
import org.pillarone.riskanalytics.core.wiring.WireCategory
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.wiring.PortReplicatorCategory
import org.pillarone.riskanalytics.core.components.ComponentCategory
import org.pillarone.riskanalytics.core.wiring.WiringUtils

/**
 *
 */
@ComponentCategory(categories=["Claims","Generators","Risk"])
class PoissonLognormalCompoundClaims extends ComposedComponent {
    /**
     * Provides a list of the generated single claims.
     */
    PacketList<ClaimPacket> outClaims = new PacketList<ClaimPacket>(ClaimPacket.class)

    /**
     * Provides the sum of the generated single claims.
     */
    PacketList<ClaimPacket> outAggregateClaims = new PacketList<ClaimPacket>(ClaimPacket.class)

    /**
     * Aggregates the single claims (per period and iteration).
     */
    Aggregator subAggregator = new Aggregator(name: 'subAggregator')

    /**
     * Poisson frequency generator - parametrized by the mean frequency per period (see <code>subFrequencyGen.parmMean</code>):
     */
    PoissonFrequencyGenerator subFrequencyGen = new PoissonFrequencyGenerator(name: 'subFrequencyGen')

    /**
     * Log-Normal single claims generator. Receives a claims frequency from the frequency generator
     * and generates an according number of single claims using a Log-Normal distribution.
     * The distribution is parametrized by the mean and standard deviation of the underlying normal distribution
     * (see the parameters <code>subClaimsGen.parmMu</code> and <code>subClaimsGen.parmSigma</code>).
     */
    SingleLogNormalClaimsGenerator subClaimsGen = new SingleLogNormalClaimsGenerator(name: 'subClaimsGen')

    public void wire() {
        WiringUtils.use(PortReplicatorCategory) {
            this.outClaims = subClaimsGen.outClaims
            this.outAggregateClaims = subAggregator.outClaims
        }

        WiringUtils.use(WireCategory) {
            subClaimsGen.inFrequency = subFrequencyGen.outFrequency
            subAggregator.inClaims = subClaimsGen.outClaims
        }
    }
}
