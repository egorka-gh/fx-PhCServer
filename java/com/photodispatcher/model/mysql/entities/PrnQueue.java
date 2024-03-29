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

    @Column(name="lab_type")
    private int lab_type;

    @Column(name="is_reprint")
    private boolean is_reprint;

    @Column(name="pg_date")
    private Date pg_date;

    
    //db drived
    
    @Column(name="priority", updatable=false, insertable=false)
    private int priority;

    @Column(name="sub_queue", updatable=false, insertable=false)
    private int sub_queue;

    @Column(name="strategy_type", updatable=false, insertable=false)
    private int strategy_type;

    @Column(name="strategy_type_name", updatable=false, insertable=false)
    private String strategy_type_name;

    @Column(name="lab_name", updatable=false, insertable=false)
    private String lab_name;
    
    @Column(name="lab_type_name", updatable=false, insertable=false)
    private String lab_type_name;

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

	public String getLab_name() {
		return lab_name;
	}

	public void setLab_name(String lab_name) {
		this.lab_name = lab_name;
	}

	public boolean isIs_reprint() {
		return is_reprint;
	}

	public void setIs_reprint(boolean is_reprint) {
		this.is_reprint = is_reprint;
	}

	public int getLab_type() {
		return lab_type;
	}

	public void setLab_type(int lab_type) {
		this.lab_type = lab_type;
	}

	public Date getPg_date() {
		return pg_date;
	}

	public void setPg_date(Date pg_date) {
		this.pg_date = pg_date;
	}

	public String getLab_type_name() {
		return lab_type_name;
	}

	public void setLab_type_name(String lab_type_name) {
		this.lab_type_name = lab_type_name;
	}
    
}
