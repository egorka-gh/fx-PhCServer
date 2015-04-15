package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "rack_orders")
public class RackOrders extends AbstractEntity {
	private static final long serialVersionUID = 1L;
	
	@Id
    @Column(name="order_id")
    private String order_id;
    @Column(name="space")
    private int space;

    //ref
    @Column(name="rack", updatable=false, insertable=false)
    private int rack;
    @Column(name="rack_name", updatable=false, insertable=false)
    private String rack_name;
    @Column(name="space_name", updatable=false, insertable=false)
    private String space_name;
    @Column(name="source", updatable=false, insertable=false)
    private int source;
    @Column(name="source_name", updatable=false, insertable=false)
    private String source_name;
    @Column(name="group_id", updatable=false, insertable=false)
    private int group_id;
    
	public String getOrder_id() {
		return order_id;
	}
	public void setOrder_id(String order_id) {
		this.order_id = order_id;
	}
	public int getSpace() {
		return space;
	}
	public void setSpace(int space) {
		this.space = space;
	}
	public String getRack_name() {
		return rack_name;
	}
	public void setRack_name(String rack_name) {
		this.rack_name = rack_name;
	}
	public String getSpace_name() {
		return space_name;
	}
	public void setSpace_name(String space_name) {
		this.space_name = space_name;
	}
	public int getSource() {
		return source;
	}
	public void setSource(int source) {
		this.source = source;
	}
	public String getSource_name() {
		return source_name;
	}
	public void setSource_name(String source_name) {
		this.source_name = source_name;
	}
	public int getGroup_id() {
		return group_id;
	}
	public void setGroup_id(int group_id) {
		this.group_id = group_id;
	}
	public int getRack() {
		return rack;
	}
	public void setRack(int rack) {
		this.rack = rack;
	}

}
