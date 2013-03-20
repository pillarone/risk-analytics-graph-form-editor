package org.pillarone.riskanalytics.graph.formeditor.ui.model.beans;


public class TypeDefinitionBean {
    public static final String MODEL = "Model";
    public static final String COMPOSED_COMPONENT = "ComposedComponent";
    private String name;
    private String packageName = "models";
    private String baseType;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getBaseType() {
        return baseType;
    }

    public void setBaseType(String baseType) {
        this.baseType = baseType;
    }

    public void reset() {
        name = null;
        packageName = null;
        baseType = null;
    }
}
