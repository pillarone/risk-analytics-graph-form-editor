package org.pillarone.riskanalytics.graph.formeditor.util

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.packets.Packet
import org.pillarone.riskanalytics.core.packets.PacketList
import java.lang.reflect.Field

/**
 * @author martin.melchior
 */
public class PacketProvider extends Component {

    public static final String NAME_PREFIX = "packetProvider_"
    private Packet packet = null;
    private Map<String,String> nameMap = [:]
    Map<String,Object> parameterMap = [:]

    private PacketList<Packet> outPacket = new PacketList<Packet>();

    public PacketProvider(Class packetType, String portName) {
        this.name = getPacketProviderPath(portName)
        this.packet = (Packet) packetType.newInstance()
        Field[] fields = packetType.getDeclaredFields().findAll { field ->
            !field.name.startsWith("\$") && !field.name.startsWith("_") && !field.name.startsWith("metaClass")
        }
        for (Field field: fields) {
            String parmName = getParameterName(field.name)           
            Object value = null
            if (field.type.isPrimitive()) {
                switch (field.type) {
                    case Boolean.TYPE: value = Boolean.FALSE
                        break
                    case Integer.TYPE:
                    case Long.TYPE: value = 0
                        break
                    case Float.TYPE:
                    case Double.TYPE: value = 0.0
                        break
                }
            }
            if (value != null) {
                nameMap[parmName] = field.name
                parameterMap[parmName] = value
            }
        }        
    }

    public boolean hasParameters() {
        return parameterMap.size()>0
    }

    public static String getPacketProviderPath(String portName) {
        return NAME_PREFIX+portName[0].toUpperCase()+portName.substring(1)
    }

    public static String getParameterName(String fieldName) {
        return "parm"+fieldName[0].toUpperCase()+fieldName.substring(1) 
    }

    public static String getOriginalFieldName(String parmName) {
        String name = parmName[4..-1]
        return name[0].toLowerCase()+name.substring(1)
    }
    
    protected void doCalculation() {
        nameMap.each { parmName, fieldName ->
            packet."${fieldName}" = parameterMap[parmName]
        }
        outPacket.add(packet)
    }

    Map getProperties() {
        Map prop = super.getProperties()

        for (Map.Entry<String, Object> e : parameterMap.entrySet()) {
            prop.put(e.key, e.value)
        }

        return prop
    }

    void putAt(String propertyName, Object value) {
        try {
            super.putAt(propertyName, value)
        } catch (MissingPropertyException ex) {
            if (parameterMap.containsKey(propertyName)) {
                parameterMap[propertyName] = value
            } else {
                throw new MissingPropertyException(propertyName)
            }
        }
    }

    Object getAt(String propertyName) {
        try {
            return super.getAt(propertyName)
        } catch (MissingPropertyException e) {
            def result = parameterMap[propertyName]
            if (result == null) {
                throw new MissingPropertyException(propertyName)
            }
            return result
        }
    }

    void setOutPacket(PacketList<Packet> outPacket) {
        this.outPacket = outPacket
    }
    PacketList<Packet> getOutPacket() {
        return outPacket
    }
}
