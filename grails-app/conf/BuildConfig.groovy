import org.apache.ivy.plugins.resolver.FileSystemResolver

grails.project.dependency.resolution = {
    inherits "global" // inherit Grails' default dependencies
    log "warn"

    repositories {
        grailsHome()
        grailsCentral()
    }

    credentials {
        realm = "Canoo Nexus Repository"
        host = "ci.canoo.com"
        username = ""
        password = ""
    }

    def ulcClientJarResolver = new FileSystemResolver()
    String absolutePluginDir = grailsSettings.projectPluginsDir.absolutePath

    ulcClientJarResolver.addArtifactPattern "${absolutePluginDir}/ulc-[revision]/web-app/lib/[artifact].[ext]"
    ulcClientJarResolver.addArtifactPattern "${basedir}/web-app/lib/[artifact]-[revision].[ext]"
    ulcClientJarResolver.name = "ulc"

    resolver ulcClientJarResolver

    mavenRepo "https://repository.intuitive-collaboration.com/nexus/content/repositories/pillarone-public/"
    mavenRepo "https://ci.canoo.com/nexus/content/groups/public"

    String ulcVersion = "ria-suite-u2"

    plugins {
        runtime ":background-thread:1.3"
        runtime ":hibernate:1.3.7"
        runtime ":joda-time:0.5"
        runtime ":maven-publisher:0.7.5"
        runtime ":quartz:0.4.2"
        runtime ":spring-security-core:1.1.2"
        runtime ":jetty:1.2-SNAPSHOT"
        compile "com.canoo:ulc:${ulcVersion}"
        runtime "org.pillarone:pillar-one-ulc-extensions:0.1"

        test ":code-coverage:1.2.4"

        if (appName == "RiskAnalyticsGraphFormEditor") {
            runtime "org.pillarone:risk-analytics-core:1.4-BETA-1-kti"
            runtime("org.pillarone:risk-analytics-graph-core:0.4.2") { transitive = false }
        }

    }

    dependencies {
        compile group: 'canoo', name: 'ulc-applet-client', version: ulcVersion
        compile group: 'canoo', name: 'ulc-base-client', version: ulcVersion
        compile group: 'canoo', name: 'ulc-base-trusted', version: ulcVersion
        compile group: 'canoo', name: 'ulc-jnlp-client', version: ulcVersion
        compile group: 'canoo', name: 'ulc-servlet-client', version: ulcVersion
        compile group: 'canoo', name: 'ulc-standalone-client', version: ulcVersion

        compile group: 'canoo', name: 'ULCGraph-client', version: "0.6-SNAPSHOT"
        compile group: 'jgraphx', name: 'jgraphx', version: "1.7.1.0"

    }
}

grails.project.dependency.distribution = {
    String password = ""
    String user = ""
    String scpUrl = ""
    try {
        Properties properties = new Properties()
        properties.load(new File("${userHome}/deployInfo.properties").newInputStream())

        user = properties.get("user")
        password = properties.get("password")
        scpUrl = properties.get("url")
    } catch (Throwable t) {
    }
    remoteRepository(id: "pillarone", url: scpUrl) {
        authentication username: user, password: password
    }
}

coverage {
    enabledByDefault = false
    xml = true
    exclusions = [
            'models/**',
            '**/*Test*',
            '**/com/energizedwork/grails/plugins/jodatime/**',
            '**/grails/util/**',
            '**/org/codehaus/**',
            '**/org/grails/**',
            '**GrailsPlugin**',
            '**TagLib**'
    ]
}
//grails.plugin.location.'risk-analytics-graph-core' = "../RiskAnalyticsGraphCore"
