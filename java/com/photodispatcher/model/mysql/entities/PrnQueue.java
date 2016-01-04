package com.photodispatcher.model.mysql.entities;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Table(name = "prn_queue")
public class PrnQueue extends AbstractEntity {
	private static final long serialVersionUID = 1L;
	
    @Id
    @GeneratedValue
    @Column(name="id")
    private int id;

    @Column(name="strategy")
    private int strategy;

    @Column(name="is_active")
    private boolean is_active;

    @Column(name="created")
    private Date created;

    @Column(name="started")
    private Date started;

    @Column(name="complited")
    private Date complited;

    @Column(name="label")
    private String label;

    @Column(name="has_sub")
    private boolean has_sub;

    @Column(name="lab")
    private int lab;
    
    
    //db drived
    
    @Column(name="priority", updatable=false, insertable=false)
    private int priority;

    @Column(name="sub_queue", updatable=false, insertable=false)
    private int sub_queue;

    @Column(name="strategy_type", updatable=false, insertable=false)
    private int strategy_type;

    @Column(name="strategy_type_name", updatable=false, insertable=false)
    private String strategy_type_name;

    @Column(name="strategy_name", updatable=false, insertable=false)
    private String strategy_name;

    //childs
    @Transient
    private List<PrintGroup> printGroups;

    
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getStrategy() {
		return strategy;
	}

	public void setStrategy(int strategy) {
		this.strategy = strategy;
	}

	public boolean isIs_active() {
		return is_active;
	}

	public void setIs_active(boolean is_active) {
		this.is_active = is_active;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
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

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isHas_sub() {
		return has_sub;
	}

	public void setHas_sub(boolean has_sub) {
		this.has_sub = has_sub;
	}

	public int getLab() {
		return lab;
	}

	public void setLab(int lab) {
		this.lab = lab;
	}

	public int getStrategy_type() {
		return strategy_type;
	}

	public void setStrategy_type(int strategy_type) {
		this.strategy_type = strategy_type;
	}

	public String getStrategy_type_name() {
		return strategy_type_name;
	}

	public void setStrategy_type_name(String strategy_type_name) {
		this.strategy_type_name = strategy_type_name;
	}

	public String getStrategy_name() {
		return strategy_name;
	}

	public void setStrategy_name(String strategy_name) {
		this.strategy_name = strategy_name;
	}

	public int getSub_queue() {
		return sub_queue;
	}

	public void setSub_queue(int sub_queue) {
		this.sub_queue = sub_queue;
	}

	public List<PrintGroup> getPrintGroups() {
		return printGroups;
	}

	public void setPrintGroups(List<PrintGroup> printGroups) {
		this.printGroups = printGroups;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}
    
}
