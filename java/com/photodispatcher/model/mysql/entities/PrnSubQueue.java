package com.photodispatcher.model.mysql.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

@Table(name = "prn_sub_queue")
public class PrnSubQueue extends AbstractEntity {
	private static final long serialVersionUID = 1L;
	
    @Column(name="prn_queue")
    private int prn_queue;

    @Column(name="sub_queue")
    private int sub_queue;

    @Column(name="started")
    private Date started;

    @Column(name="complited")
    private Date complited;

    @Column(name="lab")
    private int lab;

    
    @Column(name="is_active", updatable=false, insertable=false)
    private boolean is_active;

	public int getPrn_queue() {
		return prn_queue;
	}

	public void setPrn_queue(int prn_queue) {
		this.prn_queue = prn_queue;
	}

	public int getSub_queue() {
		return sub_queue;
	}

	public void setSub_queue(int sub_queue) {
		this.sub_queue = sub_queue;
	}

	public boolean isIs_active() {
		return is_active;
	}

	public void setIs_active(boolean is_active) {
		this.is_active = is_active;
	}

	public Date getStarted() {
		return started;
	}

	public void setStarted(Date started) {
		this.started = started;
	}

	public Date getComplited() {
		return complited;
	}

	public void setComplited(Date complited) {
		this.complited = complited;
	}

	public int getLab() {
		return lab;
	}

	public void setLab(int lab) {
		this.lab = lab;
	}

}
