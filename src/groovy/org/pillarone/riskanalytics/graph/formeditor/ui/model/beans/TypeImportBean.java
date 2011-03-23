package org.pillarone.riskanalytics.graph.formeditor.ui.model.beans;

public class TypeImportBean {

	private String clazzName;

	public String getClazzName() {
		return clazzName;
	}

	public void setClazzName(String clazzName) {
		this.clazzName = clazzName;
	}
	
	public void reset() {
		clazzName = null;
	}
}
