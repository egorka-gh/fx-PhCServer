package com.photodispatcher.model.mysql.entities.report;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import com.photodispatcher.model.mysql.entities.AbstractEntity;

@Table(name="source_type")
public class ReportSourceType extends AbstractEntity{
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name="id")
    private int id;
    @Column(name="name")
    private String name;
    
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
}
