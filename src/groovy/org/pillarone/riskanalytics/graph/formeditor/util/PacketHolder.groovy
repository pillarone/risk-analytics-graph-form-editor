package org.pillarone.riskanalytics.graph.formeditor.util

import org.pillarone.riskanalytics.core.packets.Packet
import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolder

/**
 * @author martin.melchior
 */
class PacketHolder extends ParameterHolder {

    Packet value

    PacketHolder(Parameter p) {
        super(p)
    }

    @Override
    void setParameter(Parameter parameter) {
        this.value = parameter.packetValue
    }

    @Override
    Object getBusinessObject() {
        return value
    }

    @Override protected void updateValue(Object newValue) {
        value = (Packet) newValue
    }

    @Override
    void applyToDomainObject(Parameter parameter) {
        parameter.packetValue = value
    }

    @Override
    Parameter createEmptyParameter() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

}
