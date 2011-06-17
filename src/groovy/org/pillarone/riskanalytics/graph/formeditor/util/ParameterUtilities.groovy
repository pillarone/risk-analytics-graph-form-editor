package org.pillarone.riskanalytics.graph.formeditor.util

import org.pillarone.riskanalytics.core.parameterization.ParameterizationHelper
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.parameter.comment.Comment
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolder
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolderFactory

/**
 * 
 */
class ParameterUtilities {

    public static Parameterization loadParametrization(String content) {
        ConfigObject data = new ConfigSlurper().parse(content);
        spreadRanges(data)
        Parameterization params = createParameterizationFromConfigObject(data, "params1");
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
}
