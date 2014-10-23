package com.photodispatcher.model.mysql.entities.report;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import com.photodispatcher.model.mysql.entities.AbstractEntity;

@Table(name="xrep_report_group")
public class ReportGroup extends AbstractEntity {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name="id")
    private int id;

    @Column(name="hidden")
    private boolean hidden;

    @Column(name="name")
    private String name;

    @Column(name="src_type")
    private int src_type;


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
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
}
