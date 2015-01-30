package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "staff_activity_type")
public class StaffActivityType extends AbstractEntity {
	private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue()
    @Column(name="id")
    private int id;
    @Column(name="staff_activity_group")
    private int sa_group;
    @Column(name="name")
    private String name;
    
    //db drived
    @Column(name="sa_group_name", updatable=false, insertable=false)
    private String sa_group_name;

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

	public int getSa_group() {
		return sa_group;
	}

	public void setSa_group(int sa_group) {
		this.sa_group = sa_group;
	}

	public String getSa_group_name() {
		return sa_group_name;
	}

	public void setSa_group_name(String sa_group_name) {
		this.sa_group_name = sa_group_name;
	}


}
