package com.photodispatcher.model.mysql.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "phcconfig.lab_timetable")
public class LabTimetable extends AbstractEntity {
	private static final long serialVersionUID = 1L;

    @Id
    @Column(name="lab_device")
    private int lab_device;
    @Column(name="day_id")
    private int day_id;
    @Column(name="time_from")
    private Date time_from;
    @Column(name="time_to")
    private Date time_to;
    @Column(name="is_online")
    private boolean is_online;
    
  //db drived
    @Column(name="day_id_name", updatable=false, insertable=false)
    private String day_id_name;

	public int getLab_device() {
		return lab_device;
	}

	public void setLab_device(int lab_device) {
		this.lab_device = lab_device;
	}

	public int getDay_id() {
		return day_id;
	}

	public void setDay_id(int day_id) {
		this.day_id = day_id;
	}

	public Date getTime_from() {
		return time_from;
	}

	public void setTime_from(Date time_from) {
		this.time_from = time_from;
	}

	public Date getTime_to() {
		return time_to;
	}

	public void setTime_to(Date time_to) {
		this.time_to = time_to;
	}

	public boolean isIs_online() {
		return is_online;
	}

	public void setIs_online(boolean is_online) {
		this.is_online = is_online;
	}

	public String getDay_id_name() {
		return day_id_name;
	}

	public void setDay_id_name(String day_id_name) {
		this.day_id_name = day_id_name;
	}
    
}
