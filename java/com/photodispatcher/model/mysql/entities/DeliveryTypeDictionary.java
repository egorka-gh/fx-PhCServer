package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "delivery_type_dictionary")
public class DeliveryTypeDictionary extends AbstractEntity {
	private static final long serialVersionUID = 1L;

    @Id
    @Column(name="source")
    private int source;
    @Id
    @Column(name="delivery_type")
    private int delivery_type;
    @Column(name="site_id")
    private int site_id;
    
    //ref cols
    @Column(name="source_name", updatable=false, insertable=false)
    private String source_name;
    @Column(name="delivery_type_name", updatable=false, insertable=false)
    private String delivery_type_name;
    
	public int getSource() {
		return source;
	}
	public void setSource(int source) {
		this.source = source;
	}
	public int getDelivery_type() {
		return delivery_type;
	}
	public void setDelivery_type(int delivery_type) {
		this.delivery_type = delivery_type;
	}
	public int getSite_id() {
		return site_id;
	}
	public void setSite_id(int site_id) {
		this.site_id = site_id;
	}
	public String getSource_name() {
		return source_name;
	}
	public void setSource_name(String source_name) {
		this.source_name = source_name;
	}
	public String getDelivery_type_name() {
		return delivery_type_name;
	}
	public void setDelivery_type_name(String delivery_type_name) {
		this.delivery_type_name = delivery_type_name;
	}


}
