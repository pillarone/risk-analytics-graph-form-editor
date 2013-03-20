package org.pillarone.riskanalytics.graph.formeditor.util

import be.devijver.wikipedia.Parser
import com.ulcjava.base.application.util.HTMLUtilities
import com.ulcjava.base.application.util.ULCIcon
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.graph.core.graph.model.Port
import org.springframework.web.util.HtmlUtils
import org.pillarone.riskanalytics.graph.core.graph.model.ComponentNode
import org.pillarone.riskanalytics.graph.core.graph.model.ComposedComponentGraphModel
import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel

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

    public static String convertWikiToHtml(String text) {
        if (!text) return ""
        String wiki = null
        try {
            // \n causes hiding of links
            //workaround: replace all endline with html code
            text = text.replaceAll("\n", "<br>")
            java.io.StringWriter writer = new java.io.StringWriter();
            (new Parser()).withVisitor(text, new HtmlVisitor(writer, null));
            wiki = writer.toString()
        } catch (Exception ex) {
            wiki = text
        }
        return HTMLUtilities.convertToHtml(HtmlUtils.htmlUnescape(wiki))
    }

    public static String getConnectionEntryName(Port p) {
        String name = "";
        if (p.getComponentNode() != null) {
            name += p.getComponentNode().getDisplayName() + " > ";
        }
        name += p.getDisplayName();
        return name;
    }

    /**
     * Identify an inner port within a model from its display name of the form <component name> | <port name>.
     * It is not applicable to replicating ports of composed component graph models.
     *
     * @param displayName
     * @param model
     * @return
     */
    public static Port getPortFromConnectionEntryName(String displayName, AbstractGraphModel model, boolean isInPort) {
        displayName = displayName.trim()
        String[] nameParts = displayName.split(">")
        if (nameParts.length == 2)  {
            boolean isSubComponent = model instanceof ComposedComponentGraphModel
            String nodeDisplayName = nameParts[0].trim()
            String nodeTechnicalName = org.pillarone.riskanalytics.graph.core.graph.util.UIUtils.formatTechnicalName(nodeDisplayName, ComponentNode.class, isSubComponent)
            ComponentNode node = model.findNodeByName(nodeTechnicalName)
            if (node != null) {
                String portDisplayName = nameParts[1].trim();
                String portTechnicalName = org.pillarone.riskanalytics.graph.core.graph.util.UIUtils.formatTechnicalPortName(portDisplayName, isInPort)
                Port p = node.getPort(portTechnicalName);
                return p;
            }
        } else if (nameParts.length == 1) {
            if (model instanceof ComposedComponentGraphModel) {
                String portDisplayName = nameParts[0].trim();
                String portTechnicalName = org.pillarone.riskanalytics.graph.core.graph.util.UIUtils.formatTechnicalPortName(portDisplayName, isInPort)
                List<Port> outerPorts = ((ComposedComponentGraphModel) model).getOuterOutPorts()+((ComposedComponentGraphModel) model).getOuterInPorts()
                for (Port p : outerPorts) {
                    if (p.getName().equals(portTechnicalName)) {
                        return p;
                    }
                }
            }
        }
        return null;
    }

    public static String getWatchesDisplayName(String path) {
        String[] pathElements = GraphModelUtilities.getPathElements(path);
        StringBuffer buffer = new StringBuffer()
        boolean isFirst = true
        for (String pathElm : pathElements) {
            if (isFirst) {
                isFirst = false
            } else {
                buffer.append(" > ")
            }
            buffer.append(org.pillarone.riskanalytics.graph.core.graph.util.UIUtils.formatDisplayName(pathElm))
        }
        return buffer.toString()
    }
}
