package org.pillarone.riskanalytics.graph.formeditor.ui

import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel

/**
 * @author martin.melchior
 */
public interface IModelRenameListener {
    void modelRenamed(AbstractGraphModel modelWithNewName, String oldName, String oldPackageName)
}