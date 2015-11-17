package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "lab_rolls")
public class LabRoll extends AbstractEntity {
	private static final long serialVersionUID = 1L;

	//db fileds
    @Id
    @Column(name="lab_device")
    private int lab_device;
    @Id
    @Column(name="width")
    private int width;
    @Id
    @Column(name="paper")
    private int paper;
    
    @Column(name="len_std")
    private int len_std;
    @Column(name="len")
    private int len;
    @Column(name="is_online")
    private boolean is_online;
    
    //runtime
    @Column(name="lab", updatable=false, insertable=false)
    private int lab;
    @Column(name="is_used", updatable=false, insertable=false)
    private boolean is_used;
    @Column(name="paper_name", updatable=false, insertable=false)
    private String paper_name;

    @Column(name="print_queue_len", updatable=false, insertable=false)
    private float printQueueLen;
    
    @Column(name="print_queue_time", updatable=false, insertable=false)
    private float printQueueTime;
    
    
	public int getLab_device() {
		return lab_device;
	}
	public void setLab_device(int lab_device) {
		this.lab_device = lab_device;
	}
	public int getPaper() {
		return paper;
	}
	public void setPaper(int paper) {
		this.paper = paper;
	}
	public int getLen_std() {
		return len_std;
	}
	public void setLen_std(int len_std) {
		this.len_std = len_std;
	}
	public int getLen() {
		return len;
	}
	public void setLen(int len) {
		this.len = len;
	}
	public boolean isIs_online() {
		return is_online;
	}
	public void setIs_online(boolean is_online) {
		this.is_online = is_online;
	}
	public boolean isIs_used() {
		return is_used;
	}
	public void setIs_used(boolean is_used) {
		this.is_used = is_used;
	}
	public String getPaper_name() {
		return paper_name;
	}
	public void setPaper_name(String paper_name) {
		this.paper_name = paper_name;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getLab() {
		return lab;
	}
	public void setLab(int lab) {
		this.lab = lab;
	}
	public float getPrintQueueLen() {
		return printQueueLen;
	}
	public void setPrintQueueLen(float printQueueLen) {
		this.printQueueLen = printQueueLen;
	}
	public float getPrintQueueTime() {
		return printQueueTime;
	}
	public void setPrintQueueTime(float printQueueTime) {
		this.printQueueTime = printQueueTime;
	}

}
