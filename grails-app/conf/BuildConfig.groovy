import org.apache.ivy.plugins.resolver.FileSystemResolver

grails.project.dependency.resolution = {
    inherits "global" // inherit Grails' default dependencies
    log "warn"

    repositories {
        grailsHome()
        grailsCentral()
        def ulcClientJarResolver = new FileSystemResolver()
        String absolutePluginDir = grailsSettings.projectPluginsDir.absolutePath

        ulcClientJarResolver.addArtifactPattern "${absolutePluginDir}/ulc-[revision]/web-app/lib/[artifact].[ext]"
        ulcClientJarResolver.addArtifactPattern "${basedir}/web-app/lib/[artifact]-[revision].[ext]"
        ulcClientJarResolver.name = "ulc"

        resolver ulcClientJarResolver

        mavenRepo "https://repository.intuitive-collaboration.com/nexus/content/repositories/pillarone-public/"
        mavenRepo "https://ci.canoo.com/nexus/content/groups/public"
    }

    String ulcVersion = "ria-suite-u5"

    plugins {
        runtime ":background-thread:1.3"
        runtime ":hibernate:2.2.1"
        runtime ":joda-time:0.5"
        runtime ":maven-publisher:0.7.5"
        runtime ":quartz:0.4.2"
        runtime ":spring-security-core:1.2.7.3"
        compile "com.canoo:ulc:${ulcVersion}"
        runtime "org.pillarone:pillar-one-ulc-extensions:0.3"

        test ":code-coverage:1.2.4"

        if (appName == "RiskAnalyticsGraphFormEditor") {
            runtime "org.pillarone:risk-analytics-core:1.7-a2"
            runtime "org.pillarone:risk-analytics-application:1.7-a3"
            runtime("org.pillarone:risk-analytics-graph-core:0.9") { transitive = false }
        }

    }

    dependencies {
        compile group: 'canoo', name: 'ulc-applet-client', version: ulcVersion
        compile group: 'canoo', name: 'ulc-base-client', version: ulcVersion
        compile group: 'canoo', name: 'ulc-base-trusted', version: ulcVersion
        compile group: 'canoo', name: 'ulc-jnlp-client', version: ulcVersion
        compile group: 'canoo', name: 'ulc-servlet-client', version: ulcVersion
        compile group: 'canoo', name: 'ulc-standalone-client', version: ulcVersion

        compile group: 'canoo', name: 'ULCGraph-client', version: "0.7.1"
        compile group: 'canoo', name: 'SlideInPanel-client', version: "0.1-SNAPSHOT"
        compile group: 'jgraphx', name: 'jgraphx', version: "1.7.1.0"
        //required for ulc tests
        test 'org.mortbay.jetty:jetty:6.1.21', 'org.mortbay.jetty:jetty-plus:6.1.21'
        test 'org.mortbay.jetty:jetty-util:6.1.21', 'org.mortbay.jetty:jetty-naming:6.1.21'
        test('org.mortbay.jetty:jsp-2.0:6.1.21') {
            excludes 'commons-el', 'ant', 'slf4j-api', 'slf4j-simple', 'jcl104-over-slf4j', 'xercesImpl', 'xmlParserAPIs'
        }
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
// grails.plugin.location.'risk-analytics-graph-core' = "../RiskAnalyticsGraphCore"
