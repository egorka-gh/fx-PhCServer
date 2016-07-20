package com.photodispatcher.model.mysql.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "prn_queue_start_timetable")
public class PrnQueueTimetable extends AbstractEntity {
	private static final long serialVersionUID = 1L;
	
    @Id
    @GeneratedValue
    @Column(name="id")
    private int id;
    @Column(name="is_active")
    private boolean is_active;
    @Column(name="lab_type")
    private int lab_type;
    @Column(name="strategy_type")
    private int strategy_type;
    @Column(name="time_start")
    private Date time_start;
    @Column(name="last_start")
    private Date last_start;
    
    //db drived
    @Column(name="strategy_type_name", updatable=false, insertable=false)
    private String strategy_type_name;
    @Column(name="lab_type_name", updatable=false, insertable=false)
    private String lab_type_name;
    @Column(name="last_start_c", updatable=false, insertable=false)
    private Date last_start_c;
    @Column(name="booksonly", updatable=false, insertable=false)
    private boolean booksonly;
    
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public boolean isIs_active() {
		return is_active;
	}
	public void setIs_active(boolean is_active) {
		this.is_active = is_active;
	}
	public int getLab_type() {
		return lab_type;
	}
	public void setLab_type(int lab_type) {
		this.lab_type = lab_type;
	}
	public int getStrategy_type() {
		return strategy_type;
	}
	public void setStrategy_type(int strategy_type) {
		this.strategy_type = strategy_type;
	}
	public Date getTime_start() {
		return time_start;
	}
	public void setTime_start(Date time_start) {
		this.time_start = time_start;
	}
	public Date getLast_start() {
		return last_start;
	}
	public void setLast_start(Date last_start) {
		this.last_start = last_start;
	}
	public String getStrategy_type_name() {
		return strategy_type_name;
	}
	public void setStrategy_type_name(String strategy_type_name) {
		this.strategy_type_name = strategy_type_name;
	}
	public String getLab_type_name() {
		return lab_type_name;
	}
	public void setLab_type_name(String lab_type_name) {
		this.lab_type_name = lab_type_name;
	}
	public Date getLast_start_c() {
		return last_start_c;
	}
	public void setLast_start_c(Date last_start_c) {
		this.last_start_c = last_start_c;
	}
	public boolean isBooksonly() {
		return booksonly;
	}
	public void setBooksonly(boolean booksonly) {
		this.booksonly = booksonly;
	}
}
