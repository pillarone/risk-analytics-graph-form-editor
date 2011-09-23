dataSource {
    pooled = true
    driverClassName = "org.hsqldb.jdbcDriver"
    username = "sa"
    password = ""
}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = true
    cache.provider_class = 'net.sf.ehcache.hibernate.EhCacheProvider'
}
// environment specific settings
environments {
    development {
        dataSource {
            dbCreate = "create-drop" // one of 'create', 'create-drop','update'
            url = "jdbc:hsqldb:mem:devDB"
        }
    }
    test {
        dataSource {
            dbCreate = "update"
            url = "jdbc:hsqldb:mem:testDb"
        }
    }
    production {
        dataSource {
            dbCreate = "update"
            url = "jdbc:hsqldb:file:prodDb;shutdown=true"
        }
    }
     mysql {
        dataSource {
            // Setting up mysql:
            //   create database p1rat;
            //   create user 'p1rat'@'localhost' identified by 'p1rat';
            //   grant all on table p1rat.* to 'p1rat'@'localhost';
            // required for batch uploads:
            //   grant file on *.* to 'p1rat'@'localhost';
            dbCreate = "update" // should always stay on update! use InitDatabase script to drop/create DB
            url = "jdbc:mysql://localhost/p1rat"
            driverClassName = "com.mysql.jdbc.Driver"
            dialect = "org.hibernate.dialect.MySQL5Dialect"
            username = "p1rat"
            password = "p1rat"
            pooled = true
        }
    }
}
