package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "staff")
public class Staff extends AbstractEntity {
	private static final long serialVersionUID = 1L;
	
    @Id
    @GeneratedValue()
    @Column(name="id")
    private int id;
    @Column(name="staff_group")
    private int staff_group;
    @Column(name="name")
    private String name;
    @Column(name="pwd")
    private String pwd;
    @Column(name="barcode")
    private String barcode;
    @Column(name="active")
    private boolean active;
    
    //db drived
    @Column(name="staff_group_name", updatable=false, insertable=false)
    private String staff_group_name;

    
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
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	public String getBarcode() {
		return barcode;
	}
	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public String getStaff_group_name() {
		return staff_group_name;
	}
	public void setStaff_group_name(String staff_group_name) {
		this.staff_group_name = staff_group_name;
	}
	public int getStaff_group() {
		return staff_group;
	}
	public void setStaff_group(int staff_group) {
		this.staff_group = staff_group;
	}

}
