import org.pillarone.riskanalytics.core.output.batch.results.MysqlBulkInsert
import org.pillarone.riskanalytics.core.output.batch.results.SQLServerBulkInsert
import org.pillarone.riskanalytics.core.output.batch.calculations.MysqlCalculationsBulkInsert
import org.pillarone.riskanalytics.core.output.batch.calculations.GenericBulkInsert
import grails.plugins.springsecurity.SecurityConfigType

// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

// if(System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.types = [html: ['text/html', 'application/xhtml+xml'],
        xml: ['text/xml', 'application/xml'],
        text: 'text-plain',
        js: 'text/javascript',
        rss: 'application/rss+xml',
        atom: 'application/atom+xml',
        css: 'text/css',
        csv: 'text/csv',
        all: '*/*',
        json: ['application/json', 'text/json'],
        form: 'application/x-www-form-urlencoded',
        multipartForm: 'multipart/form-data'
]
// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"

// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true

maxIterations = 100000
keyFiguresToCalculate = null
resultBulkInsert = null
userLogin = false
// a cron for a batch, A cron expression is a string comprised of 6 or 7 fields separated by white space.
// Fields can contain any of the allowed values: Sec Min Hour dayOfMonth month dayOfWeek Year
// Fire every 60 minutes
batchCron = "0 0/10 * * * ?"
// When the number of iterations exceed this number and one or more SingleValueCollectingModeStrategy is used,
iterationCountThresholdForWarningWhenUsingSingleCollector = 999

visualBuilderEnabled = true

environments {

    development {
        ExceptionSafeOut = System.out
        log4j = {
            appenders {

                String layoutPattern = "[%d{dd.MMM.yyyy HH:mm:ss,SSS}] - %t (%X{username}) - %-5p %c{1} %m%n"
                console name: 'stdout', layout: pattern(conversionPattern: layoutPattern)

            }
            root {
                error()
                additivity = false
            }

            def infoPackages = [
                    'org.pillarone.riskanalytics',
            ]

            def debugPackages = [
                    'org.pillarone.riskanalytics.core.fileimport'
            ]

            info(
                    stdout: infoPackages,
                    additivity: false
            )

            debug(
                    stdout: debugPackages,
                    additivity: false
            )

        }
        keyFiguresToCalculate = [
                'stdev': true,
                'percentile': [0.0, 10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0, 100.0],
                'var': [99, 99.5],
                'tvar': [99, 99.5],
                'pdf': 200
        ]
    }
    test {
        ExceptionSafeOut = System.out
        resultBulkInsert = org.pillarone.riskanalytics.core.output.batch.results.GenericBulkInsert
        calculationBulkInsert = GenericBulkInsert
        keyFiguresToCalculate = [
                'stdev': true,
                'percentile': [0.0, 10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0, 100.0],
                'var': [99, 99.5],
                'tvar': [99, 99.5],
                'pdf': 200
        ]
    }
    mysql {
        resultBulkInsert = MysqlBulkInsert
        calculationBulkInsert = MysqlCalculationsBulkInsert
        ExceptionSafeOut = System.out
        models = ['CoreModel']
        log4j = {
            appenders {

                String layoutPattern = "[%d{dd.MMM.yyyy HH:mm:ss,SSS}] - %t (%X{username}) - %-5p %c{1} %m%n"

                console name: 'stdout', layout: pattern(conversionPattern: layoutPattern)

            }
            root {
                error()
                additivity = false
            }

            def infoPackages = [
                    'org.pillarone.riskanalytics',
            ]

            def debugPackages = [
                    'org.pillarone.riskanalytics.core.fileimport'
            ]

            info(
                    stdout: infoPackages,
                    additivity: false
            )

            debug(
                    stdout: debugPackages,
                    additivity: false
            )

        }
        keyFiguresToCalculate = [
                'percentile': [0.5, 50, 99.5],
                'stdev': true
        ]
    }
    production {
        resultBulkInsert = MysqlBulkInsert
        calculationBulkInsert = MysqlCalculationsBulkInsert
        userLogin = true
        maxIterations = 10000
        models = ["MultiYearMultiLine"]
        keyFiguresToCalculate = [
                'stdev': true,
        ]
    }

    standalone {
        resultBulkInsert = DerbyBulkInsert
        calculationBulkInsert = MysqlCalculationsBulkInsert
        ExceptionSafeOut = System.err
        maxIterations = 10000
        models = ["MultiYearMultiLine"]
        keyFiguresToCalculate = [
                'stdev': true,
                'percentile': [0.0, 10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0, 100.0],
                'var': [99, 99.5],
                'tvar': [99, 99.5],
                'pdf': 200
        ]
    }
}

log4j = {
    appenders {
        console name: 'stdout', layout: pattern(conversionPattern: '[%d] %-5p %c{1} %m%n')
        file name: 'file', file: 'RiskAnalytics.log', layout: pattern(conversionPattern: '[%d] %-5p %c{1} %m%n')
    }
    root {
        error 'stdout', 'file'
        additivity = false
    }
    error 'org.codehaus.groovy.grails.web.servlet',  //  controllers
            'org.codehaus.groovy.grails.web.pages', //  GSP
            'org.codehaus.groovy.grails.web.sitemesh', //  layouts
            'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
            'org.codehaus.groovy.grails.web.mapping', // URL mapping
            'org.codehaus.groovy.grails.commons', // core / classloading
            'org.codehaus.groovy.grails.plugins', // plugins
            'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
//        'org.springframework',
//        'org.hibernate',
            'org.pillarone.modelling.fileimport',
            'org.pillarone.modelling.ui.util.ExceptionSafe',
            'org.pillarone.riskanalytics.core.wiring',
            'org.pillarone.modelling.domain',
            'org.pillarone.modelling.util'
    info()
    debug()
    warn()
}

grails {
    plugins {
        springsecurity {
            userLookup {
                userDomainClassName = 'org.pillarone.riskanalytics.core.user.Person'
                authorityJoinClassName = 'org.pillarone.riskanalytics.core.user.PersonAuthority'
            }
            authority {
                className = 'org.pillarone.riskanalytics.core.user.Authority'
            }
            securityConfigType = SecurityConfigType.InterceptUrlMap
            interceptUrlMap = [
                    '/login/**': ['IS_AUTHENTICATED_ANONYMOUSLY'],
                    '/**/css/**': ['IS_AUTHENTICATED_ANONYMOUSLY'],
                    '/**/js/**': ['IS_AUTHENTICATED_ANONYMOUSLY'],
                    '/**/images/**': ['IS_AUTHENTICATED_ANONYMOUSLY'],
                    '/**/*.jar': ['IS_AUTHENTICATED_ANONYMOUSLY'],
                    '/ulcserverendpoint/**': ['IS_AUTHENTICATED_ANONYMOUSLY'],
                    '/css/**': ['IS_AUTHENTICATED_ANONYMOUSLY'],
                    '/person/**': ['ROLE_ADMIN'],
                    '/authority/**': ['ROLE_ADMIN'],
                    '/**': ['IS_AUTHENTICATED_REMEMBERED'],
            ]
        }
    }
}