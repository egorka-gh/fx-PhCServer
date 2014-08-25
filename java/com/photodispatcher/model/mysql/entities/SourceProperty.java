package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "phcconfig.src_type_prop_val")
public class SourceProperty extends AbstractEntity {
	private static final long serialVersionUID = 1L;
	
    @Id
    @GeneratedValue
    @Column(name="id")
    private int id;
    @Column(name="src_type")
    private int src_type;
    @Column(name="src_type_prop")
    private int src_type_prop;
    @Column(name="value")
    private String value;
    
    //ref
    @Column(name="name", insertable=false, updatable=false)
    private String name;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getSrc_type() {
		return src_type;
	}

	public void setSrc_type(int src_type) {
		this.src_type = src_type;
	}

	public int getSrc_type_prop() {
		return src_type_prop;
	}

	public void setSrc_type_prop(int src_type_prop) {
		this.src_type_prop = src_type_prop;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
