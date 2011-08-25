package org.pillarone.riskanalytics.graph.formeditor.util

import org.pillarone.riskanalytics.core.parameterization.ParameterizationHelper
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.parameter.comment.Comment
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolder
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolderFactory
import org.pillarone.riskanalytics.graph.core.graph.model.ComponentNode
import org.pillarone.riskanalytics.core.components.Component
import java.lang.reflect.Field

/**
 * 
 */
class ParameterUtilities {

    public static Parameterization loadParametrization(String content, String name) {
        ConfigObject data = new ConfigSlurper().parse(content);
        spreadRanges(data)
        Parameterization params = createParameterizationFromConfigObject(data, name);
        return params
    }

    private static void spreadRanges(ConfigObject config) {
        def rangeKeys = [:]
        List ranges = []
        config.each {key, value ->
            if (value instanceof ConfigObject) {
                spreadRanges(value)
            }
            if (key instanceof Range) {
                ranges << key
                key.each {
                    rangeKeys[it] = value
                }
            }
        }
        config.putAll(rangeKeys)
        ranges.each {
            config.remove(it)
        }
    }

    public static Parameterization createParameterizationFromConfigObject(ConfigObject configObject, String paramName) {
        Parameterization parametrization = new Parameterization(paramName)
        int periodCount = 1
        if (configObject.containsKey("periodCount")) {
            periodCount = configObject.periodCount
        }
        parametrization.periodCount
        Map map = configObject.flatten()
        int maxPeriod = 0
        int period = 0
        map.each { key0, value ->
            String key = (String) key0
            if (key.startsWith("components")) {
                String path = key - "components."
                path = path.replace(".",":")
                String lastElement = path.substring(path.lastIndexOf(":")+1)
                try {
                    period = Integer.parseInt(lastElement)
                    path = path.substring(0,path.lastIndexOf(":"))
                    ParameterHolder holder = ParameterHolderFactory.getHolder(path, period, value)
                    parametrization.addParameter(holder)
                } catch (Exception ex) {
                    ParameterHolder holder = ParameterHolderFactory.getHolder(path, 0, value)
                    parametrization.addParameter(holder)
                }
                maxPeriod = Math.max(period,maxPeriod)
            }
        }
        parametrization.periodCount = maxPeriod+1
        return parametrization
    }

    public static Map<String,Object> getParameterObjects(ComponentNode node) {
        Map<String, Object> result = [:]
        Class componentClass = node.getType().getTypeClass()
        Component component = (Component) componentClass.newInstance()
        Field[] fields = componentClass.declaredFields
        for (Field field in fields) {
            if (field.name.startsWith("parm")) {
                field.accessible = true
                Object value = field.get(component)
                if (!field.type.isPrimitive() && !value) {
                    value = field.type.newInstance()
                }
                result.put(field.name, value)
            }
        }
        return result
    }
}
