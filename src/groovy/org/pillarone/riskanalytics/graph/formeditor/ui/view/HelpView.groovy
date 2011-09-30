package org.pillarone.riskanalytics.graph.formeditor.ui.view

import com.ulcjava.base.application.ULCBoxPane
import com.ulcjava.base.application.ULCFiller
import com.ulcjava.base.application.ULCLabel
import com.ulcjava.base.application.util.Color
import com.ulcjava.base.application.util.Font
import com.ulcjava.base.application.util.HTMLUtilities
import org.pillarone.riskanalytics.core.components.ComposedComponent
import org.pillarone.riskanalytics.graph.core.graph.model.filters.IComponentNodeFilter
import org.pillarone.riskanalytics.graph.core.graph.util.UIUtils
import org.pillarone.riskanalytics.graph.core.palette.model.ComponentDefinition
import org.pillarone.riskanalytics.graph.core.palette.service.PaletteService
import org.pillarone.riskanalytics.graph.formeditor.ui.model.treetable.NodeNameFilter
import org.pillarone.riskanalytics.graph.formeditor.util.GraphModelUtilities
import org.pillarone.riskanalytics.graph.formeditor.util.ParameterUtilities
import org.pillarone.riskanalytics.graph.core.graph.model.*
import com.ulcjava.base.application.ULCCardPane
import com.ulcjava.base.application.ULCTextArea
import com.ulcjava.base.application.ULCButton
import com.ulcjava.base.application.event.IActionListener
import com.ulcjava.base.application.event.ActionEvent
import com.ulcjava.base.application.ULCHtmlPane
import com.ulcjava.base.application.event.IHyperlinkListener
import com.ulcjava.base.application.event.HyperlinkEvent
import com.ulcjava.base.application.ClientContext
import com.ulcjava.base.application.ULCTextField

/**
 * @author fouad.jaada@intuitive-collaboration.com, martin.melchior@fhnw.ch
 */
class HelpView implements ISelectionListener, IHelpViewable {

    ULCBoxPane mainComponent
    BreadCrumbsModelSelector modelPathSelector
    ULCCardPane contentArea
    HelpHtmlPane htmlTextPane
    ULCTextArea editableTextPane
    ULCBoxPane buttonPane
    ULCButton editButton

    HelpEntry modelHelpEntry
    Map<String,HelpEntry> cache = new LinkedHashMap<String,HelpEntry>() // TODO could be declared thread local
    Map<GraphElement, LinkedList<GraphElement>> cachedPaths = [:]

    AbstractGraphModel graphModel

    public HelpView() {
        init()
    }

    public void injectGraphModel(AbstractGraphModel model) {
        graphModel = model
        showHelp(model)
    }

    private void init() {
        initComponents()
        layoutComponents()
        attachListeners()
    }

    private void initComponents() {
        mainComponent = new ULCBoxPane(true)
        modelPathSelector = new BreadCrumbsModelSelector()
        contentArea = new ULCCardPane()

        htmlTextPane = new HelpHtmlPane()
        editableTextPane = new ULCTextArea("...")
        editableTextPane.setEditable(true)
        contentArea.addCard("text", editableTextPane)
        contentArea.addCard("html", htmlTextPane)

        buttonPane = new ULCBoxPane(false)
        editButton = new ULCButton("edit");
        editButton.setEnabled false
        editButton.setToolTipText("Edit the description of this model/component.");
        ULCButton okButton = new ULCButton("ok");
        okButton.setEnabled false
        ULCButton cancelButton = new ULCButton("cancel");
        cancelButton.setEnabled false

        editButton.addActionListener(
            new IActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    editableTextPane.setText(modelHelpEntry.description)
                    contentArea.setSelectedComponent(editableTextPane);
                    editButton.setEnabled false
                    okButton.setEnabled true
                    cancelButton.setEnabled true
                }
            }
        )
        IActionListener okAction = new IActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                graphModel.helpText = editableTextPane.getText()
                modelHelpEntry = createHelpEntry(graphModel)
                viewEntry(modelHelpEntry, true)
                contentArea.setSelectedComponent(htmlTextPane);
                /*String text = modelHelpEntry.description + " " + modelHelpEntry.autoText
                htmlTextPane.setText(HTMLUtilities.convertToHtml("<div style='100%'> $text </div>"))
                contentArea.setSelectedComponent(htmlTextPane);*/
                editButton.setEnabled true
                okButton.setEnabled false
                cancelButton.setEnabled false
            }
        }
        okButton.addActionListener(okAction)

        IActionListener cancelAction = new IActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                contentArea.setSelectedComponent(htmlTextPane);
                editButton.setEnabled true
                okButton.setEnabled false
                cancelButton.setEnabled false
            }
        }
        cancelButton.addActionListener(cancelAction)
        buttonPane.add(ULCBoxPane.BOX_LEFT_CENTER, editButton)
        buttonPane.add(ULCBoxPane.BOX_EXPAND_CENTER, ULCFiller.createHorizontalGlue())
        buttonPane.add(ULCBoxPane.BOX_LEFT_CENTER, cancelButton)
        buttonPane.add(ULCBoxPane.BOX_LEFT_CENTER, okButton)

    }

    private void layoutComponents() {
        if (modelPathSelector) {
            mainComponent.add(ULCBoxPane.BOX_LEFT_CENTER, modelPathSelector)
        }
        mainComponent.add(ULCBoxPane.BOX_EXPAND_EXPAND, contentArea)
        mainComponent.add(ULCBoxPane.BOX_LEFT_BOTTOM, buttonPane)
    }

    private void attachListeners() {
        if (modelPathSelector) {
            modelPathSelector.helpView = this
        }
    }

    void showHelp(ComponentDefinition componentDefinition) {
        String path = componentDefinition.getTypeClass().getName()
        if (!cache.containsKey(path)) {
            cache[path] = createHelpEntry(componentDefinition)
        }
        HelpEntry entry = cache[path]
        viewEntry(entry, false)
    }

    public void showHelp(ComponentNode node) {
        String path = node.getType().typeClass.getName()
        if (!cache.containsKey(path)) {
            cache[path] = createHelpEntry(node)
        }
        viewEntry(cache[path], true)

        LinkedList<GraphElement> modelPath
        if (!cachedPaths.containsKey(node)) {
            modelPath = GraphModelUtilities.getModelTreePath(node, graphModel)
            if (modelPath) {
                modelPath.add(0,graphModel)
                cachedPaths.put(node, modelPath)
            }
        } else {
            modelPath = cachedPaths[node]
        }
        modelPathSelector.setCurrentPath(modelPath)
    }

    public void showHelp(AbstractGraphModel model) {
        if (!modelHelpEntry) {
            modelHelpEntry = createHelpEntry(model)
        }
        viewEntry(modelHelpEntry, true)

        if (!cachedPaths.containsKey(model)) {
            LinkedList<GraphElement> modelPath = new LinkedList<GraphElement>()
            modelPath << model
            modelPathSelector.setCurrentPath(modelPath)
            cachedPaths[model] = modelPath
        }
        LinkedList<GraphElement> modelPath = cachedPaths[model]
        modelPathSelector.setCurrentPath(modelPath)
    }

    public void setSelectedComponents(List<ComponentNode> selectedNodes) {
        ComponentNode node = selectedNodes != null && selectedNodes.size() > 0 ? selectedNodes[-1] : null
        if (node) {
            showHelp(node)
        }
    }

    public void nodeSelected(String path) {
    }

    private void viewEntry(HelpEntry entry, boolean showContext) {
        if (modelPathSelector) {
            modelPathSelector.setVisible(showContext)
        }
        String text = "<h3>$entry.title</h3>"
        text <<= (entry.description != null ? entry.description : "No description yet. \n") + " " + entry.autoText
        htmlTextPane.setText(HTMLUtilities.convertToHtml("<div style='width:100%'> $text </div>"))
        contentArea.setSelectedComponent(htmlTextPane);
        editButton.setEnabled(entry.isEditable)
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
        // nothing to do here
    }

    private HelpEntry createHelpEntry(AbstractGraphModel model) {
        HelpEntry entry = new HelpEntry()

        entry.title = model.getName()

        String path = model.getPackageName()+"."+model.getName()
        String type = model instanceof ComposedComponentGraphModel ? "ComposedComponent" : "Component"
        List<String> categoriesList = null
        Map<String, Object> parameters = null
        List<Port> ports = null
        if (model instanceof ComposedComponentGraphModel) {
            ports = ((ComposedComponentGraphModel)model).outerInPorts + ((ComposedComponentGraphModel)model).outerOutPorts
        }
        entry.autoText = createAutoDescription(path, type, categoriesList, ports, parameters)

        entry.description = model.helpText

        entry.isEditable = true

        return entry
    }

    private HelpEntry createHelpEntry(ComponentNode node) {
        HelpEntry entry = createHelpEntry(node.getType())
        entry.title = node.getType().simpleName
        return entry
    }

    private HelpEntry createHelpEntry(ComponentDefinition componentDefinition) {
        HelpEntry entry = new HelpEntry()

        String path = componentDefinition.getTypeClass().getName()
        entry.description = UIUtils.getPropertyValue(null, "COMPONENT_DEFINITION_HELP", "['$path']")

        String title = path.substring(path.lastIndexOf('.')+1)
        entry.title = title

        ComponentNode node
        String type
        if (ComposedComponent.isAssignableFrom(componentDefinition.typeClass)) {
            node = ComposedComponentNode.createInstance(componentDefinition, "dummy")
            type = "ComposedComponent"
        } else {
            node = ComponentNode.createInstance(componentDefinition, "dummy")
            type = "Component"
        }
        List<String> categoriesList = PaletteService.getInstance().getCategoriesFromDefinition(componentDefinition)
        List<Port> ports = node.getInPorts() + node.getOutPorts()
        Map<String, Object> parameters = ParameterUtilities.getParameterObjects(node)
        entry.autoText = createAutoDescription(path, type, categoriesList, ports, parameters)

        entry.isEditable = false

        return entry
    }

    private static String createAutoDescription(String fullPath, String type,
                List<String> categoriesList, List<Port> portList, Map<String, Object> parameters) {

        String description = ""
        if (categoriesList) {
            String htmlCategories = "<h4>Categories: </h4>" + Arrays.toString(categoriesList as String[])
            description <<= " " + htmlCategories
        }

        String htmlClassName = "<h4>Class Name: </h4> <code> $fullPath </code>"
        description <<= " " + htmlClassName

        if (type) {
            String htmlType = "<h4>Type: </h4><code> $type </code>"
            description <<= " " + htmlType
        }

        if (portList) {
            String htmlPortsList = "<h4>Ports: </h4> <ul>"
            for (Port p: portList) {
                String portName = p.getName()
                String packetInfo = p.getPacketType().getSimpleName()
                String packetCardinality = p.connectionCardinality ? ", ($p.connectionCardinality.from, $p.connectionCardinality.to )" : ""
                htmlPortsList <<= "<li><code> $portName [ $packetInfo $packetCardinality ] </code></li>"
            }
            htmlPortsList <<= "</ul>"
            description <<= " " + htmlPortsList
        }

        if (parameters) {
            String htmlParameterList = "<h4>Parameters: </h4> <ul>"
            for (Map.Entry<String, Object> p: parameters) {
                String paramName = p.getKey()
                String paramType = p.getValue().getClass().getName()
                htmlParameterList <<= "<li><code> $paramName ( $paramType ) </code></li>"
            }
            htmlParameterList <<= "</ul>"
            description <<= " " + htmlParameterList
        }
        return description
    }

    class HelpEntry {
        String title
        String description
        String autoText
        boolean isEditable
    }

    public static final String RESOURCE_KEY = "resourceKey"
    public static final String ARGS = "args"
    class HelpHtmlPane extends ULCHtmlPane {
        private String htmlText

        public HelpHtmlPane() {
            setVeto true
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

        void linkError(HyperlinkEvent hyperlinkEvent) {}

        private void showDocument(String url) {
            if (!url.startsWith("resourceKey")) {
                ClientContext.showDocument(url, "_new")
                return
            }
            Map resource = org.pillarone.riskanalytics.graph.formeditor.util.UIUtils.parseResourceURL(url)
            if (!resource.empty && resource[RESOURCE_KEY]) {
                String path = resource[RESOURCE_KEY]
                ComponentDefinition definition = PaletteService.instance.getComponentDefinition(path)
                try {
                    showHelp(definition)
                } catch (Exception ex) {}
            }
        }
    }
}

interface IHelpViewable {
    void showHelp(ComponentDefinition cd)
}

