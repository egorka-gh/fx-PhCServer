package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.Id;

public class DeliveryTypePrintForm extends AbstractEntity {
	private static final long serialVersionUID = 1L;

    @Id
    @Column(name="delivery_type")
    private int delivery_type;
    @Id
    @Column(name="form")
    private int form;
    
    //ref
    @Column(name="delivery_type_name", updatable=false, insertable=false)
    private String delivery_type_name;
    @Column(name="form_name", updatable=false, insertable=false)
    private String form_name;
    @Column(name="report", updatable=false, insertable=false)
    private String report;
    
    
	public int getDelivery_type() {
		return delivery_type;
	}
	public void setDelivery_type(int delivery_type) {
		this.delivery_type = delivery_type;
	}
	public int getForm() {
		return form;
	}
	public void setForm(int form) {
		this.form = form;
	}
	public String getDelivery_type_name() {
		return delivery_type_name;
	}
	public void setDelivery_type_name(String delivery_type_name) {
		this.delivery_type_name = delivery_type_name;
	}
	public String getForm_name() {
		return form_name;
	}
	public void setForm_name(String form_name) {
		this.form_name = form_name;
	}
	public String getReport() {
		return report;
	}
	public void setReport(String report) {
		this.report = report;
	}


}
