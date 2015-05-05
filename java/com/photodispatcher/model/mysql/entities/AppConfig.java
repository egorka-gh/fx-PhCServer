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

    @Column(name="production")
    private int production;
    @Column(name="clean_fs")
    private boolean clean_fs;
    @Column(name="clean_fs_state")
    private int clean_fs_state;
    @Column(name="clean_fs_days")
    private int clean_fs_days;
    @Column(name="clean_fs_limit")
    private int clean_fs_limit;

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

	public boolean isClean_fs() {
		return clean_fs;
	}

	public void setClean_fs(boolean clean_fs) {
		this.clean_fs = clean_fs;
	}

	public int getClean_fs_state() {
		return clean_fs_state;
	}

	public void setClean_fs_state(int clean_fs_state) {
		this.clean_fs_state = clean_fs_state;
	}

	public int getClean_fs_days() {
		return clean_fs_days;
	}

	public void setClean_fs_days(int clean_fs_days) {
		this.clean_fs_days = clean_fs_days;
	}

	public int getClean_fs_limit() {
		return clean_fs_limit;
	}

	public void setClean_fs_limit(int clean_fs_limit) {
		this.clean_fs_limit = clean_fs_limit;
	}

}
