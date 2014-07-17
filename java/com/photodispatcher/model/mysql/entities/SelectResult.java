package com.photodispatcher.model.mysql.entities;

import java.io.Serializable;
import java.util.List;

public class SelectResult<T> extends SqlResult implements Serializable {
	private static final long serialVersionUID = 1L;

	private List<T> data;

	public List<T> getData() {
		return data;
	}
	public void setData(List<T> data) {
		this.data = data;
	}

}
