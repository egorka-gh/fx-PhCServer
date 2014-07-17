package com.photodispatcher.model.mysql.entities;

import java.io.Serializable;

public abstract class AbstractEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	private int persistState=1; //0-new, -1 - changed, 1-persisted
	private boolean hasAutoId=false;//has surrogate auto increment id (int) 

	public int getPersistState() {
		return persistState;
	}
	public void setPersistState(int persistState) {
		this.persistState = persistState;
	}
	
	public boolean isHasAutoId() {
		return hasAutoId;
	}
	public void setHasAutoId(boolean hasAutoId) {
		this.hasAutoId = hasAutoId;
	}

}
