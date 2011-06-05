package org.pillarone.riskanalytics.graph.formeditor.ui.model;

import com.ulcjava.base.application.tree.DefaultMutableTreeNode;
import org.pillarone.riskanalytics.graph.core.palette.model.ComponentDefinition;

/**
 * Created by IntelliJ IDEA.
 * User: martin.melchior
 * Date: 05.04.11
 * Time: 16:35
 * To change this template use File | Settings | File Templates.
 */
public class TypeTreeNode extends DefaultMutableTreeNode {
    String fTypeName = null;
    String fPackagePath = null;

    public TypeTreeNode(String path, String name) {
        super(path);
        fPackagePath = path;
        fTypeName = name;
    }

    public TypeTreeNode(ComponentDefinition cd) {
        super(cd);
        fPackagePath = cd.getTypeClass().getName();
        String[] pathElms = fPackagePath.split(ComponentTypeTreeModelFactory.PATHSEP);
        fTypeName = pathElms[pathElms.length-1].trim();
    }

    public String toString() {
        return fTypeName;
    }

    public String getName() {
            return fTypeName;
        }

    public String getPackagePath() {
        return fPackagePath;
    }
}
