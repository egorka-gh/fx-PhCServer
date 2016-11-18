package com.photodispatcher.model.mysql.entities;

import java.io.Serializable;

public class DmlResult<T> extends SqlResult implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private T item;

	public T getItem() {
		return item;
	}

	public void setItem(T item) {
		this.item = item;
	}
	
}
