package org.pillarone.riskanalytics.graph.formeditor.examples

import org.pillarone.riskanalytics.core.packets.Packet

/**
 * 
 */
class ClaimPacket extends Packet {

    double value

    public Map<String, Number> getValuesToSave() throws IllegalAccessException {
        Map<String, Number> valuesToSave = new HashMap<String, Number>(1);
        valuesToSave.put("claim", value);
        return valuesToSave;
    }
}
