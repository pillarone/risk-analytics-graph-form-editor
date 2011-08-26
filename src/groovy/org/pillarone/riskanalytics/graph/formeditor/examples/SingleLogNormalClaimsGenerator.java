package org.pillarone.riskanalytics.graph.formeditor.examples;

import org.pillarone.riskanalytics.core.components.Component;
import org.pillarone.riskanalytics.core.components.ComponentCategory;
import org.pillarone.riskanalytics.core.packets.PacketList;
import org.pillarone.riskanalytics.core.util.MathUtils;
import org.pillarone.riskanalytics.core.wiring.WiringValidation;
import umontreal.iro.lecuyer.randvar.LognormalGen;
import umontreal.iro.lecuyer.randvar.NormalGen;
import umontreal.iro.lecuyer.randvar.RandomVariateGen;

/**
 *
 */
@ComponentCategory(categories={"Claims","Generators","Risk"})
public class SingleLognormalClaimsGenerator extends Component {

    private double parmMu = 1.0;
    private double parmSigma = 1.0;

    @WiringValidation(connections = {0,Integer.MAX_VALUE},packets = {0,Integer.MAX_VALUE})
    private PacketList<FrequencyPacket> inFrequency = new PacketList<FrequencyPacket>(FrequencyPacket.class);
    private PacketList<ClaimPacket> outClaims = new PacketList<ClaimPacket>(ClaimPacket.class);

    public void doCalculation() {
        RandomVariateGen generator =  new LognormalGen(MathUtils.getRandomStreamBase(), getParmMu(), getParmSigma());
        int freq = 0;
        if (isReceiverWired(getInFrequency())) {
            for (FrequencyPacket f : getInFrequency()) {
                freq += f.getValue();
            }
        } else {
            freq = 1;
        }
        for (int i = 0; i < freq; i++) {
            ClaimPacket c = new ClaimPacket();
            c.setValue(generator != null ? generator.nextDouble() : 0.0);
            getOutClaims().add(c);
        }
    }

    public double getParmMu() {
        return parmMu;
    }

    public void setParmMu(double parmMu) {
        this.parmMu = parmMu;
    }

    public double getParmSigma() {
        return parmSigma;
    }

    public void setParmSigma(double parmSigma) {
        this.parmSigma = parmSigma;
    }

    public PacketList<FrequencyPacket> getInFrequency() {
        return inFrequency;
    }

    public void setInFrequency(PacketList<FrequencyPacket> inFrequency) {
        this.inFrequency = inFrequency;
    }

    public PacketList<ClaimPacket> getOutClaims() {
        return outClaims;
    }

    public void setOutClaims(PacketList<ClaimPacket> outClaims) {
        this.outClaims = outClaims;
    }
}
