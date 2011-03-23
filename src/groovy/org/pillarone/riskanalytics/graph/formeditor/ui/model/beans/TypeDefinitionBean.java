package org.pillarone.riskanalytics.graph.formeditor.ui.model.beans;


public class TypeDefinitionBean {
	private String name;
	private String packageName;
	private boolean model;
	
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
	public boolean isModel() {
		return model;
	}
	public void setModel(boolean model) {
		this.model = model;
	}
	
	public void reset() {
		name = null;
		packageName = null;
		model = false;
	}
}
