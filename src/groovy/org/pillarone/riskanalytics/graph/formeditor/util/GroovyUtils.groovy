package org.pillarone.riskanalytics.graph.formeditor.util

import org.pillarone.riskanalytics.graph.core.loader.ClassRepository
import org.pillarone.riskanalytics.graph.core.loader.DatabaseClassLoader

class GroovyUtils {

    public static Class persistClass(byte[] data, String name) {
        DatabaseClassLoader classLoader = Thread.currentThread().contextClassLoader
        synchronized (classLoader) {
            ClassRepository.withTransaction {
                new ClassRepository(name: name, data: data).save()
            }
            classLoader.refresh()
        }
        return classLoader.loadClass(name)
    }
}
