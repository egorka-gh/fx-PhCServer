package com.photodispatcher.model.mysql.entities;

import java.io.Serializable;

public class DmlResult extends SqlResult implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int lastId;

	public int getLastId() {
		return lastId;
	}
	public void setLastId(int lastId) {
		this.lastId = lastId;
	}
}
