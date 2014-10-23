package com.photodispatcher.model.mysql.entities.report;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.photodispatcher.model.mysql.entities.AbstractEntity;

@Table(name="report")
public class Report extends AbstractEntity {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name="id")
    private String id;

    @Column(name="src_type")
    private int src_type;

    @Column(name="rep_group")
    private int group;

    @Column(name="hidden")
    private boolean hidden;

    @Column(name="name")
    private String name;
    
    //ref
    /**
     * 
     */
    @Column(name="group_name", updatable=false, insertable=false)
    private String group_name;

    @Transient
    private Parameter[] parameters;
    
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;

	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public int getSrc_type() {
		return src_type;
	}
	public void setSrc_type(int src_type) {
		this.src_type = src_type;
	}
	public Parameter[] getParameters() {
		return parameters;
	}
	public void setParameters(Parameter[] parameters) {
		this.parameters = parameters;
	}
	public boolean isHidden() {
		return hidden;
	}
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	public int getGroup() {
		return group;
	}
	public void setGroup(int group) {
		this.group = group;
	}
	public String getGroup_name() {
		return group_name;
	}
	public void setGroup_name(String group_name) {
		this.group_name = group_name;
	}
	
	
}
