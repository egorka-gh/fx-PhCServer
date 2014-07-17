package com.photodispatcher.model.mysql.entities;

import java.io.Serializable;

public class SqlResult implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private boolean complete=true;
	private int errCode=0;
	private String errMesage;
	private String sql;
	
	public boolean isComplete() {
		return complete;
	}
	public void setComplete(boolean complete) {
		this.complete = complete;
	}
	
	public int getErrCode() {
		return errCode;
	}
	public void setErrCode(int errCode) {
		this.errCode = errCode;
	}
	
	public String getErrMesage() {
		return errMesage;
	}
	public void setErrMesage(String errMesage) {
		this.errMesage = errMesage;
	}
	
	public String getSql() {
		return sql;
	}
	public void setSql(String sql) {
		this.sql = sql;
	}
	
}
