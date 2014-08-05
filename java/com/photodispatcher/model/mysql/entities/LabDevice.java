package com.photodispatcher.model.mysql.entities;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Table(name = "phcconfig.lab_device")
public class LabDevice extends AbstractEntity {
	private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column(name="id")
    private int id;
    @Column(name="lab")
    private int lab;
    @Column(name="name")
    private String name;
    @Column(name="tech_point")
    private int tech_point;
    @Column(name="speed1")
    private float speed1;
    @Column(name="speed2")
    private float speed2;

  //db drived
    @Column(name="tech_point_name", updatable=false, insertable=false)
    private String tech_point_name;
    
	//db childs
    @Transient
    private List<LabRoll> rolls;
    @Transient
    private List<LabTimetable> timetable;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getLab() {
		return lab;
	}
	public void setLab(int lab) {
		this.lab = lab;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getTech_point() {
		return tech_point;
	}
	public void setTech_point(int tech_point) {
		this.tech_point = tech_point;
	}
	public float getSpeed1() {
		return speed1;
	}
	public void setSpeed1(float speed1) {
		this.speed1 = speed1;
	}
	public float getSpeed2() {
		return speed2;
	}
	public void setSpeed2(float speed2) {
		this.speed2 = speed2;
	}
	public String getTech_point_name() {
		return tech_point_name;
	}
	public void setTech_point_name(String tech_point_name) {
		this.tech_point_name = tech_point_name;
	}
	public List<LabRoll> getRolls() {
		return rolls;
	}
	public void setRolls(List<LabRoll> rolls) {
		this.rolls = rolls;
	}
	public List<LabTimetable> getTimetable() {
		return timetable;
	}
	public void setTimetable(List<LabTimetable> timetable) {
		this.timetable = timetable;
	}

}
