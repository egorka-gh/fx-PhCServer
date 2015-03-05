package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "app_config")
public class AppConfig extends AbstractEntity {
	private static final long serialVersionUID = 1L;
	
    @Id
    @Column(name="id")
    private int id;

    @Id
    @Column(name="production")
    private int production;

    //ref name
    @Column(name="production_name", updatable=false, insertable=false)
    private String production_name;


	public int getProduction() {
		return production;
	}

	public void setProduction(int production) {
		this.production = production;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getProduction_name() {
		return production_name;
	}

	public void setProduction_name(String production_name) {
		this.production_name = production_name;
	}

}
