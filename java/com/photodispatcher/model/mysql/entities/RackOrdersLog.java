package com.photodispatcher.model.mysql.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "rack_orders_log")
public class RackOrdersLog extends AbstractEntity {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue()
    @Column(name="rowId")
    private int rowId;

    @Column(name="event_time")
    private Date event_time;
    @Column(name="order_id")
    private String order_id;
    @Column(name="rack_name")
    private String rack_name;
    @Column(name="name")
    private String name;
    
	public int getRowId() {
		return rowId;
	}
	public void setRowId(int rowId) {
		this.rowId = rowId;
	}
	public Date getEvent_time() {
		return event_time;
	}
	public void setEvent_time(Date event_time) {
		this.event_time = event_time;
	}
	public String getOrder_id() {
		return order_id;
	}
	public void setOrder_id(String order_id) {
		this.order_id = order_id;
	}
	public String getRack_name() {
		return rack_name;
	}
	public void setRack_name(String rack_name) {
		this.rack_name = rack_name;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
    	
}
