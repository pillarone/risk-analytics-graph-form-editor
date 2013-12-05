import grails.util.Holders
import org.pillarone.riskanalytics.application.ui.extension.WindowRegistry
import org.pillarone.riskanalytics.core.util.ResourceBundleRegistry
import org.pillarone.riskanalytics.graph.formeditor.application.GraphEditorComponentCreator

class RiskAnalyticsGraphFormEditorGrailsPlugin {
    // the plugin version
    def version = "1.9-SNAPSHOT"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.2.1 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    def author = "Your name"
    def authorEmail = ""
    def title = "Plugin summary/headline"
    def description = '''\\
Brief description of the plugin.
'''

    def groupId = "org.pillarone"

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/risk-analytics-graph-form-editor"

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before
    }

    def doWithSpring = {
        // TODO Implement runtime spring config (optional)
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { applicationContext ->
        ResourceBundleRegistry.addBundle("COMPONENT_DEFINITION_HELP", "org.pillarone.riskanalytics.graph.formeditor.examples.componentDefinitionHelp")

        boolean visualBuilderEnabled = false
        if (Holders.config.containsKey("visualBuilderEnabled")) {
            visualBuilderEnabled = Holders.config.visualBuilderEnabled
        }

        if (visualBuilderEnabled) {
            WindowRegistry.registerWindow("Graph", new GraphEditorComponentCreator())
        }
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }
}
