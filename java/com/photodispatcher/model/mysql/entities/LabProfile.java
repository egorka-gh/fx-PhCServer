package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "lab_profile")
public class LabProfile extends AbstractEntity {
	private static final long serialVersionUID = 1L;

	//db fileds
    @Id
    @Column(name="lab")
    private int lab;
    @Id
    @Column(name="paper")
    private int paper;
    @Column(name="profile_file")
    private String profile_file;
    
    @Column(name="paper_name", updatable=false, insertable=false)
    private String paper_name;
    
	public int getPaper() {
		return paper;
	}
	public void setPaper(int paper) {
		this.paper = paper;
	}
	public String getProfile_file() {
		return profile_file;
	}
	public void setProfile_file(String profile_file) {
		this.profile_file = profile_file;
	}
	public String getPaper_name() {
		return paper_name;
	}
	public void setPaper_name(String paper_name) {
		this.paper_name = paper_name;
	}
	public int getLab() {
		return lab;
	}
	public void setLab(int lab) {
		this.lab = lab;
	}

    
}
