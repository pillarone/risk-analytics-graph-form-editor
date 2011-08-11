package org.pillarone.riskanalytics.graph.formeditor.ui.model.palette;

import com.ulcjava.base.application.tree.DefaultMutableTreeNode;
import org.pillarone.riskanalytics.graph.core.palette.model.ComponentDefinition;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.palette.ComponentTypeTreeModelFactory;

/**
 * 
 */
public class TypeTreeNode extends DefaultMutableTreeNode implements Comparable<TypeTreeNode> {
    String fSimpleName = null;
    String fFullName = null;
    String fParentPath = null;

    public TypeTreeNode(String parentName, String name) {
        super(parentName != null && parentName.length()>0 ? parentName+"."+name : name);
        fFullName = parentName != null && parentName.length()>0 ? parentName+"."+name : name;
        fSimpleName = name;
        fParentPath = parentName;
    }

    public TypeTreeNode(String fullName) {
        super(fullName);
        fFullName = fullName;
        fSimpleName = getSimpleName(fullName);
        fParentPath = getParentPath(fullName);
    }

    public TypeTreeNode(ComponentDefinition cd) {
        this(cd.getTypeClass().getName());
    }

    private String getParentPath(String fullName) {
        int index = fullName.lastIndexOf(".");
        if (index > 0) {
            return fullName.substring(0,index);
        }
        return "";
    }

    private String getSimpleName(String fullName) {
        int index = fullName.lastIndexOf(".");
        if (index < 0) {
            return fullName;
        } else if (index < fullName.length()-1) {
            return fullName.substring(index+1);
        }
        return "";
    }

    public String toString() {
        return fFullName;
    }

    public String getName() {
        return fSimpleName;
    }

    public String getParentPath() {
        return fParentPath;
    }

    public String getFullName() {
        return fFullName;
    }

    public int compareTo(TypeTreeNode o) {
        int index = this.fSimpleName.compareTo(o.getName());
        if (index==0) {
            index = this.fParentPath.compareTo(o.getParentPath());
        }
        return index;  
    }
}
