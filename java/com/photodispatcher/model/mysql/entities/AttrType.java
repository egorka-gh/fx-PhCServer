package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "attr_type")
public class AttrType extends AbstractEntity {
	private static final long serialVersionUID = 1L;

	/*
	public AttrType() {
		setHasAutoId(true);
	}
	*/

	//database props
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private int id;
    @Column(name="attr_fml")
    private int attr_fml;
    @Column(name="name")
    private String name;
    @Column(name="field")
    private String field;
    @Column(name="list")
    private boolean list;
    @Column(name="persist")
    private boolean persist;

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getAttr_fml() {
		return attr_fml;
	}
	public void setAttr_fml(int attr_fml) {
		this.attr_fml = attr_fml;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
