package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "rack")
public class Rack extends AbstractEntity {

	private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column(name="id")
    private int id;
    @Column(name="rack_type")
    private int rack_type;
    @Column(name="name")
    private String name;
    
    //ref
    @Column(name="rack_type_name", updatable=false, insertable=false)
    private String rack_type_name;
    
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getRack_type() {
		return rack_type;
	}
	public void setRack_type(int rack_type) {
		this.rack_type = rack_type;
	}
	public String getRack_type_name() {
		return rack_type_name;
	}
	public void setRack_type_name(String rack_type_name) {
		this.rack_type_name = rack_type_name;
	}

}
