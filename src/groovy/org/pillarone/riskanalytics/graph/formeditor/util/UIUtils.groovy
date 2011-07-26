package org.pillarone.riskanalytics.graph.formeditor.util

import com.ulcjava.base.application.util.ULCIcon

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */

//todo fja: use RA UIUtils 
class UIUtils {

    public static final String ICON_DIRECTORY = "/org/pillarone/riskanalytics/graph/formeditor/icons/"
    
    public static ULCIcon getIcon(String fileName) {
        URL url = new UIUtils().class.getResource(ICON_DIRECTORY + fileName)
        if (url) {
            return new ULCIcon(url)
        }
    }
}
