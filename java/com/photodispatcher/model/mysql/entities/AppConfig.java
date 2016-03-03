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
    @Column(name="clean_fs_hour")
    private int clean_fs_hour;
    @Column(name="clean_nr_days")
    private int clean_nr_days;
    @Column(name="pdf_quality")
    private int pdf_quality;
    @Column(name="print_rotate")
    private boolean print_rotate;
    @Column(name="print_revers")
    private boolean print_revers;

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

	public int getClean_fs_hour() {
		return clean_fs_hour;
	}

	public void setClean_fs_hour(int clean_fs_hour) {
		this.clean_fs_hour = clean_fs_hour;
	}

	public int getClean_nr_days() {
		return clean_nr_days;
	}

	public void setClean_nr_days(int clean_nr_days) {
		this.clean_nr_days = clean_nr_days;
	}

	public int getPdf_quality() {
		return pdf_quality;
	}

	public void setPdf_quality(int pdf_quality) {
		this.pdf_quality = pdf_quality;
	}

	public boolean isPrint_rotate() {
		return print_rotate;
	}

	public void setPrint_rotate(boolean print_rotate) {
		this.print_rotate = print_rotate;
	}

	public boolean isPrint_revers() {
		return print_revers;
	}

	public void setPrint_revers(boolean print_revers) {
		this.print_revers = print_revers;
	}

}
