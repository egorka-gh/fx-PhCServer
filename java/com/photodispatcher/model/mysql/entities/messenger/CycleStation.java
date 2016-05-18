package com.photodispatcher.model.mysql.entities.messenger;

import com.photodispatcher.model.mysql.entities.AbstractEntity;

public class CycleStation extends AbstractEntity {
    private static final long serialVersionUID = 1L;

    private String id;
    private int type;
    private int subtype;
    private int state;
    private String type_name;
    private String name;
    
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getSubtype() {
		return subtype;
	}
	public void setSubtype(int subtype) {
		this.subtype = subtype;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType_name() {
		return type_name;
	}
	public void setType_name(String type_name) {
		this.type_name = type_name;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}

}
