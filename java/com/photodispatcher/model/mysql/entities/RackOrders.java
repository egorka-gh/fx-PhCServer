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
    @Column(name="rack_name", updatable=false, insertable=false)
    private String rack_name;
    @Column(name="space_name", updatable=false, insertable=false)
    private String space_name;
    
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

}
