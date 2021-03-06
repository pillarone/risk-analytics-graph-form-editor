package org.pillarone.riskanalytics.graph.formeditor.util

import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.packets.Packet
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.graph.core.loader.ClassRepository
import org.pillarone.riskanalytics.graph.core.loader.DatabaseClassLoader
import org.pillarone.riskanalytics.graph.core.palette.model.ComponentDefinition
import org.pillarone.riskanalytics.graph.core.loader.ClassType

class GroovyUtils {

    public static Class persistClass(ClassType classType, byte[] data, String name) {
        DatabaseClassLoader classLoader = Thread.currentThread().contextClassLoader
        synchronized (classLoader) {
            ClassRepository.withTransaction {
                new ClassRepository(name: name, data: data, classType: classType).save()
            }
            classLoader.refresh()
        }
        return classLoader.loadClass(name)
    }

    public static Map<Field, Class> obtainPorts(ComponentDefinition definition, String prefix) {
        Map<String, Class> result = [:]

        Class currentClass = definition.typeClass
        while (currentClass != Component.class) {
            for (Field field in currentClass.declaredFields) {
                if (field.name.startsWith(prefix) && PacketList.isAssignableFrom(field.type)) {
                    Class packetType = Packet
                    Type genericType = field.genericType
                    if (genericType instanceof ParameterizedType) {
                        packetType = genericType.actualTypeArguments[0]
                    }
                    result.put(field, packetType)
                }
            }
            currentClass = currentClass.superclass
        }

        return result
    }
}
