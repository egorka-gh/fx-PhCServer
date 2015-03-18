package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "rack_tech_point")
public class RackTechPoint extends AbstractEntity {
	private static final long serialVersionUID = 1L;
	
    @Id
    @Column(name="rack")
    private int rack;
    @Id
    @Column(name="tech_point")
    private int tech_point;

    //runtime
    @Column(name="inuse", updatable=false, insertable=false)
    private boolean inuse;

    //ref
    @Column(name="rack_name", updatable=false, insertable=false)
    private String rack_name;
    @Column(name="tech_point_name", updatable=false, insertable=false)
    private String tech_point_name;
    
	public int getRack() {
		return rack;
	}
	public void setRack(int rack) {
		this.rack = rack;
	}
	public int getTech_point() {
		return tech_point;
	}
	public void setTech_point(int tech_point) {
		this.tech_point = tech_point;
	}
	public boolean isInuse() {
		return inuse;
	}
	public void setInuse(boolean inuse) {
		this.inuse = inuse;
	}
	public String getRack_name() {
		return rack_name;
	}
	public void setRack_name(String rack_name) {
		this.rack_name = rack_name;
	}
	public String getTech_point_name() {
		return tech_point_name;
	}
	public void setTech_point_name(String tech_point_name) {
		this.tech_point_name = tech_point_name;
	}

}
