package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "attr_json_map")
public class AttrJsonMap extends AbstractEntity {
	private static final long serialVersionUID = 1L;
	
    @Id
    @Column(name="src_type")
    private int src_type;
    @Id
    @Column(name="attr_type")
    private int attr_type;
    @Column(name="json_key")
    private String json_key;
    
    //ref
    @Column(name="field", insertable=false, updatable=false)
    private String field;
    @Column(name="list", insertable=false, updatable=false)
    private boolean list;
    @Column(name="persist", insertable=false, updatable=false)
    private boolean persist;
    
	public int getSrc_type() {
		return src_type;
	}
	public void setSrc_type(int src_type) {
		this.src_type = src_type;
	}
	public int getAttr_type() {
		return attr_type;
	}
	public void setAttr_type(int attr_type) {
		this.attr_type = attr_type;
	}
	public String getJson_key() {
		return json_key;
	}
	public void setJson_key(String json_key) {
		this.json_key = json_key;
	}
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
	public boolean isList() {
		return list;
	}
	public void setList(boolean list) {
		this.list = list;
	}
	public boolean isPersist() {
		return persist;
	}
	public void setPersist(boolean persist) {
		this.persist = persist;
	}

}
