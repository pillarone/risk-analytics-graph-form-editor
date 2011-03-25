package org.pillarone.riskanalytics.graph.formeditor.util

import org.pillarone.riskanalytics.graph.core.loader.ClassRepository
import org.pillarone.riskanalytics.graph.core.loader.DatabaseClassLoader

class GroovyUtils {

    public static Class persistClass(byte[] data, String name) {
        ClassRepository.withTransaction {
            new ClassRepository(name: name, data: data).save()
        }
        return new DatabaseClassLoader(Thread.currentThread().contextClassLoader).loadClass(name)
    }
}
