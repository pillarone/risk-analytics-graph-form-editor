package org.pillarone.riskanalytics.graph.formeditor.util

import com.ulcjava.base.application.util.ULCIcon
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */

//todo fja: use RA UIUtils 
class UIUtils {

    public static final String ICON_DIRECTORY = "/org/pillarone/riskanalytics/graph/formeditor/icons/"
    static Log LOG = LogFactory.getLog(UIUtils)

    public static ULCIcon getIcon(String fileName) {
        URL url = new UIUtils().class.getResource(ICON_DIRECTORY + fileName)
        if (url) {
            return new ULCIcon(url)
        }
    }

    public static Map parseResourceURL(String url) {
        Map res = [:]
        try {
            List parameters = url.split(",")
            parameters.each {String param ->
                List items = param.split(":")
                res[items[0]] = items[1]
            }
        } catch (Exception ex) {
            LOG.error(ex)
        }
        return res;
    }
}
