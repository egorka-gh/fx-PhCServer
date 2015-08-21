package com.photodispatcher.model.mysql.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "lab_stop_log")
public class LabStopLog extends AbstractEntity {
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue
	@Column(name="id")
	private int id;
	
	@Column(name="lab")
	private int lab;
	@Column(name="lab_device")
	private int lab_device;
	@Column(name="lab_stop_type")
	private int lab_stop_type;
	
	@Column(name="time_from")
	private Date time_from;
	
	@Column(name="time_to")
	private Date time_to;
	
	@Column(name="time_created")
	private Date time_created;
	
	@Column(name="time_updated")
	private Date time_updated;
	
	@Column(name="log_comment")
	private String log_comment;
	
	@Column(name="lab_stop_type_name", insertable=false, updatable=false)
	private String lab_stop_type_name;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public int getLab_device() {
		return lab_device;
	}

	public void setLab_device(int lab_device) {
		this.lab_device = lab_device;
	}
	
	public int getLab_stop_type() {
		return lab_stop_type;
	}

	public void setLab_stop_type(int lab_stop_type) {
		this.lab_stop_type = lab_stop_type;
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
	
	public String getLog_comment() {
		return log_comment;
	}
	public void setLog_comment(String comment) {
		this.log_comment = comment;
	}
	
	public String getLab_stop_type_name() {
		return lab_stop_type_name;
	}
	public void setLab_stop_type_name(String lab_stop_type_name) {
		this.lab_stop_type_name = lab_stop_type_name;
	}
	
	public Date getTime_created() {
		return time_created;
	}

	public void setTime_created(Date time_created) {
		this.time_created = time_created;
	}
	
	public Date getTime_updated() {
		return time_updated;
	}

	public void setTime_updated(Date time_updated) {
		this.time_updated = time_updated;
	}
	public int getLab() {
		return lab;
	}
	public void setLab(int lab) {
		this.lab = lab;
	}
	
}
