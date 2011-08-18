package org.pillarone.riskanalytics.graph.formeditor.ui.view

import com.ulcjava.base.application.ULCHtmlPane
import com.ulcjava.base.application.event.HyperlinkEvent
import com.ulcjava.base.application.ClientContext
import com.ulcjava.base.application.event.IHyperlinkListener
import org.pillarone.riskanalytics.graph.formeditor.util.UIUtils

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */
class ResourceLinkHtmlPane extends ULCHtmlPane {

    public static final String RESOURCE_KEY = "resourceKey"
    public static final String ARGS = "args"
    private String htmlText
    private List renderedResources

    public ResourceLinkHtmlPane() {
        setVeto true
        renderedResources = []
        addListener()
    }

    protected void addListener() {
        addHyperlinkListener(new OpenLinkListener())
    }

    @Override
    void setText(String text) {
        super.setText(text)
        htmlText = text
    }

    class OpenLinkListener implements IHyperlinkListener {

        void linkActivated(HyperlinkEvent hyperlinkEvent) {
            String url = null
            try {
                url = hyperlinkEvent.getURL().toExternalForm()
            } catch (NullPointerException ex) {
                url = hyperlinkEvent.getDescription()
            }
            if (url == null) return
            showDocument(url)
        }

        void linkError(HyperlinkEvent hyperlinkEvent) {
        }

        private void showDocument(String url) {
            if (!url.startsWith("resourceKey")) {
                ClientContext.showDocument(url, "_new")
                return
            }
            Map resource = UIUtils.parseResourceURL(url)
            if (!resource.empty && !renderedResources.contains(resource[RESOURCE_KEY])) {
                String newResource = org.pillarone.riskanalytics.graph.core.graph.util.UIUtils.getPropertyValue(null, "COMPONENT_DEFINITION_HELP", "['${resource[RESOURCE_KEY]}']")
                if (htmlText.indexOf("</html>") > 0) {
                    htmlText = htmlText.replace("</html>", "<br><hr><br>${newResource}</html>")
                } else {
                    htmlText += "\n" + newResource
                }
                setText(htmlText)
                renderedResources << resource[RESOURCE_KEY]
            }
        }
    }
}
