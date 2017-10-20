package com.photodispatcher.model.mysql.entities;

import java.util.Date;

import javax.persistence.Column;

public class SpyData extends AbstractEntity {
	private static final long serialVersionUID = 1L;
	
    @Column(name="id")
    private String id;
    @Column(name="sub_id")
    private String sub_id;
    @Column(name="state")
    private int state;
    @Column(name="start_date")
    private Date start_date;
    @Column(name="state_date")
    private Date state_date;
    @Column(name="transit_date")
    private Date transit_date;
    @Column(name="is_reject")
    private boolean is_reject;
    @Column(name="lastDate")
    private Date lastDate;
    @Column(name="resetDate")
    private Date resetDate;
    @Column(name="reset")
    private boolean reset;
    @Column(name="book_type")
    private int book_type;
    @Column(name="delay")
    private int delay;
    @Column(name="alias")
    private String alias;
    
    @Column(name="op_name")
    private String op_name;
    @Column(name="book_part")
    private int book_part;
    @Column(name="bp_name")
    private String bp_name;
    @Column(name="bt_name")
    private String bt_name;
    
    
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getSub_id() {
		return sub_id;
	}
	public void setSub_id(String sub_id) {
		this.sub_id = sub_id;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public Date getStart_date() {
		return start_date;
	}
	public void setStart_date(Date start_date) {
		this.start_date = start_date;
	}
	public Date getState_date() {
		return state_date;
	}
	public void setState_date(Date state_date) {
		this.state_date = state_date;
	}
	public Date getLastDate() {
		return lastDate;
	}
	public void setLastDate(Date lastDate) {
		this.lastDate = lastDate;
	}
	public Date getResetDate() {
		return resetDate;
	}
	public void setResetDate(Date resetDate) {
		this.resetDate = resetDate;
	}
	public boolean isReset() {
		return reset;
	}
	public void setReset(boolean reset) {
		this.reset = reset;
	}
	public int getBook_type() {
		return book_type;
	}
	public void setBook_type(int book_type) {
		this.book_type = book_type;
	}
	public int getDelay() {
		return delay;
	}
	public void setDelay(int delay) {
		this.delay = delay;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public String getOp_name() {
		return op_name;
	}
	public void setOp_name(String op_name) {
		this.op_name = op_name;
	}
	public int getBook_part() {
		return book_part;
	}
	public void setBook_part(int book_part) {
		this.book_part = book_part;
	}
	public String getBp_name() {
		return bp_name;
	}
	public void setBp_name(String bp_name) {
		this.bp_name = bp_name;
	}
	public String getBt_name() {
		return bt_name;
	}
	public void setBt_name(String bt_name) {
		this.bt_name = bt_name;
	}
	public Date getTransit_date() {
		return transit_date;
	}
	public void setTransit_date(Date transit_date) {
		this.transit_date = transit_date;
	}
	public boolean isIs_reject() {
		return is_reject;
	}
	public void setIs_reject(boolean is_reject) {
		this.is_reject = is_reject;
	}
    
}
