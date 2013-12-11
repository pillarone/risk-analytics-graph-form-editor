grails.project.dependency.resolver = "maven"

grails.project.dependency.resolution = {
    inherits ("global") { // inherit Grails' default dependencies
        excludes "grails-plugin-testing"
    }
    log "warn"

    repositories {
        grailsHome()
        grailsCentral()
        mavenCentral()

        mavenRepo "https://repository.intuitive-collaboration.com/nexus/content/repositories/pillarone-public/"
        mavenRepo "https://repository.intuitive-collaboration.com/nexus/content/repositories/pillarone-public-snapshot/"
        mavenRepo "http://repo.spring.io/milestone/" //needed for spring-security-core 2.0-rc2 plugin
        mavenRepo "https://ci.canoo.com/nexus/content/repositories/public-releases"

    }

    String ulcVersion = "ria-suite-2013-2"

    plugins {
        runtime ":background-thread:1.3"
        runtime ":hibernate:3.6.10.3"
        runtime ":joda-time:0.5"
        runtime ":release:3.0.1"
        runtime ":quartz:0.4.2"
        runtime ":spring-security-core:2.0-RC2"
        compile "com.canoo:ulc:${ulcVersion}"
        runtime("org.pillarone:pillar-one-ulc-extensions:1.3") { transitive = false }
        runtime ":tomcat:7.0.42"

        test ":code-coverage:1.2.7"

        if (appName == "RiskAnalyticsGraphFormEditor") {
            runtime "org.pillarone:risk-analytics-core:1.9-SNAPSHOT"
            runtime("org.pillarone:risk-analytics-application:1.9-SNAPSHOT") { transitive = false }
            runtime("org.pillarone:risk-analytics-graph-core:1.9-SNAPSHOT") { transitive = false }
        }

    }

    dependencies {
        compile(group: 'com.canoo.ulc.ext', name: 'ULCGraph-client', version: '1.0.1') { transitive = false }
        compile(group: 'com.canoo.ulc.ext', name: 'ULCGraph-server', version: '1.0.1') { transitive = false }

        compile group: 'com.canoo.ulc.slideinpanel', name: 'SlideInPanel-client', version: "0.1"
        compile group: 'com.mxgraph', name: 'jgraphx', version: "1.10.0.3"
        //required for ulc tests
        test 'org.mortbay.jetty:jetty:6.1.21', 'org.mortbay.jetty:jetty-plus:6.1.21'
        test 'org.mortbay.jetty:jetty-util:6.1.21', 'org.mortbay.jetty:jetty-naming:6.1.21'
        test 'hsqldb:hsqldb:1.8.0.10'

        test("org.grails:grails-plugin-testing:2.2.3.FIXED")
        test("org.springframework:spring-test:3.2.4.RELEASE")
    }
}

grails.project.dependency.distribution = {
    String password = ""
    String user = ""
    String scpUrl = ""
    try {
        Properties properties = new Properties()
        String version = new GroovyClassLoader().loadClass('RiskAnalyticsGraphFormEditorGrailsPlugin').newInstance().version
        properties.load(new File("${userHome}/deployInfo.properties").newInputStream())
        user = properties.get("user")
        password = properties.get("password")

        if (version?.endsWith('-SNAPSHOT')){
            scpUrl = properties.get("urlSnapshot")
        }else {
            scpUrl = properties.get("url")
        }
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
