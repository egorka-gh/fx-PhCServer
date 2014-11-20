package org.xreport.util;

public enum ParameterType {
	//date time
	PPeriod("period"),
	PDate("pdate"),
	PDateTime("pdatetime")
	/*
	,	//int
	PState("pstate"),
	PStateTo("pstateto"),
	PBookpart("pbookpart"),
	PToggle("ptoggle")
	*/
	;
	
	private ParameterType(String type) {
		this.type = type;
	}

    private String type;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

}
