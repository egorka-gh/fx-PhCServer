package com.photodispatcher.model.mysql.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "prn_strategy")
public class PrnStrategy extends AbstractEntity {
	private static final long serialVersionUID = 1L;
	
    @Id
    @GeneratedValue
    @Column(name="id")
    private int id;

    @Column(name="lab")
    private int lab;
    
    @Column(name="is_active")
    private boolean is_active;
    
    @Column(name="strategy_type")
    private int strategy_type;

    @Column(name="priority")
    private int priority;

    @Column(name="time_start")
    private Date time_start;

    @Column(name="last_start")
    private Date last_start;

    @Column(name="time_end")
    private Date time_end;

    @Column(name="refresh_interval")
    private int refresh_interval;

    @Column(name="width")
    private int width;

    @Column(name="paper")
    private int paper;

    @Column(name="limit_type")
    private int limit_type;

    @Column(name="limit_val")
    private int limit_val;

    @Column(name="limit_done")
    private int limit_done;

    @Column(name="order_type")
    private int order_type;
    
    //db drived
    @Column(name="strategy_type_name", updatable=false, insertable=false)
    private String strategy_type_name;

    @Column(name="lab_name", updatable=false, insertable=false)
    private String lab_name;

    @Column(name="paper_name", updatable=false, insertable=false)
    private String paper_name;

    @Column(name="limit_type_name", updatable=false, insertable=false)
    private String limit_type_name;

    @Column(name="order_type_name", updatable=false, insertable=false)
    private String order_type_name;

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

	public boolean isIs_active() {
		return is_active;
	}

	public void setIs_active(boolean is_active) {
		this.is_active = is_active;
	}

	public int getStrategy_type() {
		return strategy_type;
	}

	public void setStrategy_type(int strategy_type) {
		this.strategy_type = strategy_type;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
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

	public Date getTime_end() {
		return time_end;
	}

	public void setTime_end(Date time_end) {
		this.time_end = time_end;
	}

	public int getRefresh_interval() {
		return refresh_interval;
	}

	public void setRefresh_interval(int refresh_interval) {
		this.refresh_interval = refresh_interval;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getPaper() {
		return paper;
	}

	public void setPaper(int paper) {
		this.paper = paper;
	}

	public int getLimit_type() {
		return limit_type;
	}

	public void setLimit_type(int limit_type) {
		this.limit_type = limit_type;
	}

	public int getLimit_val() {
		return limit_val;
	}

	public void setLimit_val(int limit_val) {
		this.limit_val = limit_val;
	}

	public int getLimit_done() {
		return limit_done;
	}

	public void setLimit_done(int limit_done) {
		this.limit_done = limit_done;
	}

	public int getOrder_type() {
		return order_type;
	}

	public void setOrder_type(int order_type) {
		this.order_type = order_type;
	}

	public String getStrategy_type_name() {
		return strategy_type_name;
	}

	public void setStrategy_type_name(String strategy_type_name) {
		this.strategy_type_name = strategy_type_name;
	}

	public String getLab_name() {
		return lab_name;
	}

	public void setLab_name(String lab_name) {
		this.lab_name = lab_name;
	}

	public String getPaper_name() {
		return paper_name;
	}

	public void setPaper_name(String paper_name) {
		this.paper_name = paper_name;
	}

	public String getLimit_type_name() {
		return limit_type_name;
	}

	public void setLimit_type_name(String limit_type_name) {
		this.limit_type_name = limit_type_name;
	}

	public String getOrder_type_name() {
		return order_type_name;
	}

	public void setOrder_type_name(String order_type_name) {
		this.order_type_name = order_type_name;
	}

}
