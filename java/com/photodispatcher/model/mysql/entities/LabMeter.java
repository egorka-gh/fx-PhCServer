package com.photodispatcher.model.mysql.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "lab_meter")
public class LabMeter extends AbstractEntity {
	private static final long serialVersionUID = 1L;

    @Id
    @Column(name="lab")
    private int lab;
    @Id
    @Column(name="lab_device")
    private int lab_device;
    @Id
    @Column(name="meter_type")
    private int meter_type;
    @Column(name="start_time")
    private Date start_time;
    @Column(name="last_time")
    private Date last_time;
    
    @Column(name="print_group")
    private String print_group;
    @Column(name="state")
    private int state;
    @Column(name="amt")
    private int amt;
    
    //db drived
    @Column(name="server_time", updatable=false, insertable=false)
    private Date server_time;
    
	public int getLab_device() {
		return lab_device;
	}
	public void setLab_device(int lab_device) {
		this.lab_device = lab_device;
	}
	public int getMeter_type() {
		return meter_type;
	}
	public void setMeter_type(int meter_type) {
		this.meter_type = meter_type;
	}
	public Date getStart_time() {
		return start_time;
	}
	public void setStart_time(Date start_time) {
		this.start_time = start_time;
	}
	public Date getLast_time() {
		return last_time;
	}
	public void setLast_time(Date last_time) {
		this.last_time = last_time;
	}
	public String getPrint_group() {
		return print_group;
	}
	public void setPrint_group(String print_group) {
		this.print_group = print_group;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public int getAmt() {
		return amt;
	}
	public void setAmt(int amt) {
		this.amt = amt;
	}
	public int getLab() {
		return lab;
	}
	public void setLab(int lab) {
		this.lab = lab;
	}
	public Date getServer_time() {
		return server_time;
	}
	public void setServer_time(Date server_time) {
		this.server_time = server_time;
	}
}
