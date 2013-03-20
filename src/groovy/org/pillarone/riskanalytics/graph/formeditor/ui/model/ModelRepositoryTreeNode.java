package org.pillarone.riskanalytics.graph.formeditor.ui.model;

import com.ulcjava.base.application.tree.DefaultMutableTreeNode;
import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel;
import org.pillarone.riskanalytics.graph.core.palette.model.ComponentDefinition;

/**
 */
public class ModelRepositoryTreeNode extends DefaultMutableTreeNode {

    String fName = null;
    String fPackageName = null;

    public ModelRepositoryTreeNode(String path, String name) {
        super(path);
        fPackageName = path;
        fName = name;
    }

    public ModelRepositoryTreeNode(AbstractGraphModel model) {
        super(model);
        fPackageName = model.getPackageName();
        fName = model.getName();
    }

    public String toString() {
        return fPackageName + "." + fName;
    }

    public String getName() {
        return fName;
    }

    public String getPackageName() {
        return fPackageName;
    }

    public void setName(String fName) {
        this.fName = fName;
    }

    public void setPackageName(String fPackageName) {
        this.fPackageName = fPackageName;
    }
}
