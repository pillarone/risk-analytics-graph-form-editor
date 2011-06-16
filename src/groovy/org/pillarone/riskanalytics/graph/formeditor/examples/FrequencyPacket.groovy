package org.pillarone.riskanalytics.graph.formeditor.examples

import org.pillarone.riskanalytics.core.packets.Packet

/**
 * 
 */
class FrequencyPacket extends Packet {

    int value

    public Map<String, Number> getValuesToSave() throws IllegalAccessException {
        Map<String, Number> valuesToSave = new HashMap<String, Number>(1);
        valuesToSave.put("frequency", value);
        return valuesToSave;
    }
}
