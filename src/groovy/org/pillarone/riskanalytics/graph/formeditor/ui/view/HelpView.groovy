package org.pillarone.riskanalytics.graph.formeditor.ui.view

import com.ulcjava.base.application.ULCBoxPane
import com.ulcjava.base.application.util.HTMLUtilities
import org.pillarone.riskanalytics.graph.core.graph.util.UIUtils
import com.canoo.ulc.graph.ULCGraph
import com.canoo.ulc.graph.model.Vertex
import com.ulcjava.base.application.ULCFiller
import com.ulcjava.base.application.ULCHtmlPane
import com.ulcjava.base.application.util.Color
import com.canoo.ulc.graph.IGraphSelectionListener
import org.pillarone.riskanalytics.graph.core.graph.model.ComponentNode
import org.pillarone.riskanalytics.graph.core.graph.model.filters.IComponentNodeFilter
import org.pillarone.riskanalytics.graph.core.graph.model.Connection
import org.pillarone.riskanalytics.graph.core.graph.model.ComposedComponentNode
import org.pillarone.riskanalytics.graph.core.graph.model.Port
import org.pillarone.riskanalytics.graph.core.palette.service.PaletteService
import org.pillarone.riskanalytics.graph.formeditor.util.ParameterUtilities
import org.pillarone.riskanalytics.graph.formeditor.ui.model.treetable.NodeNameFilter

/**
 * @author fouad.jaada@intuitive-collaboration.com, martin.melchior@fhnw.ch
 */
class HelpView implements ISelectionListener {

    ULCBoxPane content
    ResourceLinkHtmlPane label

    public HelpView() {
        init()
    }

    private void init() {
        initComponents()
        layoutComponents()
        attachListeners()
    }

    private void initComponents() {
        content = new ULCBoxPane(1, 2)
        content.setBackground(Color.white)
        label = new ResourceLinkHtmlPane()
    }

    private void layoutComponents() {
        content.add(ULCBoxPane.BOX_LEFT_TOP, label)
        content.add(ULCBoxPane.BOX_EXPAND_EXPAND, new ULCFiller())
    }

    private void attachListeners() {
    }

    public void setSelectedComponents(List<ComponentNode> selectedNodes) {
        ComponentNode lastNode = selectedNodes != null && selectedNodes.size() > 0 ? selectedNodes[-1] : null
        if (lastNode) {
            String propertyKey = lastNode.getType().getTypeClass().getName()
            String value = UIUtils.getPropertyValue(null, "COMPONENT_DEFINITION_HELP", "['$propertyKey']")
            value = createFullDescription(value, lastNode)
            value = "<div style='100%'>$value</div>"
            String htmlText = HTMLUtilities.convertToHtml(value ? value : "")
            label.setText(htmlText)
        }
    }

    public void nodeSelected(String path) {
        String value = UIUtils.getPropertyValue(null, "COMPONENT_DEFINITION_HELP", "['$path']")
        value = "<div style='100%'>$value</div>"
        String htmlText = HTMLUtilities.convertToHtml(value ? value : "")
        label.setText(htmlText)
    }

    public void setSelectedConnections(List<Connection> selectedConnections) {
        // nothing to do here
    }

    public void applyFilter(IComponentNodeFilter filter) {
        // nothing to do here
    }

    void applyFilter(NodeNameFilter filter) {
    }

    public void clearSelection() {
        label.setText ""
    }

    private static String createFullDescription(String description, ComponentNode node) {
        String title = node.type.getSimpleName()
        List<String> categoriesList = PaletteService.getInstance().getCategoriesFromDefinition(node.getType())
        String fullName = node.type.getTypeClass().getName()
        String type = node instanceof ComposedComponentNode ? "ComposedComponent" : "Component"
        List<Port> portList = node.getInPorts() + node.getOutPorts()
        Map<String, Object> parameterMap = ParameterUtilities.getParameterObjects(node)
        String htmlTitle = "<b><h3> $title </h3></b>"
        String htmlCategories = "<h4>Categories: </h4>" + Arrays.toString(categoriesList as String[])
        String htmlClassName = "<h4>Class Name: </h4> <code> $fullName </code>"
        String htmlType = "<h4>Type: </h4><code> $type </code>"
        String htmlPortsList = "<h4>Ports: </h4> <ul>"
        for (Port p: portList) {
            String portName = p.getName()
            String packetInfo = p.getPacketType().getSimpleName()
            String packetCardinality = p.connectionCardinality ? ", ($p.connectionCardinality.from, $p.connectionCardinality.to )" : ""
            htmlPortsList <<= "<li><code> $portName [ $packetInfo $packetCardinality ] </code></li>"
        }
        htmlPortsList <<= "</ul>"
        String htmlParameterList = "<h4>Parameters: </h4> <ul>"
        for (Map.Entry<String, Object> p: parameterMap) {
            String paramName = p.getKey()
            String paramType = p.getValue().getClass().getName()
            htmlParameterList <<= "<li><code> $paramName ( $paramType ) </code></li>"
        }
        htmlParameterList <<= "</ul>"
        String htmlDescription = "<h4>Description: </h4>" + description

        return htmlTitle + " " + htmlDescription + " " + htmlCategories + " " + htmlClassName + " " + htmlType + " " + htmlPortsList + " " + htmlParameterList
    }
}

