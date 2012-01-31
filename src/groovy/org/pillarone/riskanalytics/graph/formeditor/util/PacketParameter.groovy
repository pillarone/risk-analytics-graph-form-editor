package org.pillarone.riskanalytics.graph.formeditor.util

import org.pillarone.riskanalytics.core.packets.Packet
import org.pillarone.riskanalytics.core.parameter.Parameter

/**
 * @author martin.melchior
 */
class PacketParameter extends Parameter {
    Packet packetValue

    Class persistedClass() {
        PacketParameter
    }
}
